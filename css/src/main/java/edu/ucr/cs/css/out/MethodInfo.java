package edu.ucr.cs.css.out;

import com.google.errorprone.VisitorState;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.code.Symbol;
import edu.ucr.cs.css.SymbolUtil;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MethodInfo {
    final int id;
    final String method;
    final String clazz;
    String uri;
    String[] nonnullFields = {};
    Boolean[] annotFlags;
    int paramNumber = 0;
    int parent = -1;

    private static int LAST_ID = 0;
    static final Set<MethodInfo> discovered = new HashSet<>();
    private static final MethodInfo top = new MethodInfo("null", "null");

    private MethodInfo(String method, String clazz) {
        this.id = LAST_ID++;
        this.method = method;
        this.clazz = clazz;
    }

    public static MethodInfo findOrCreate(String method, String clazz) {
        Optional<MethodInfo> optionalMethodInfo =
                discovered
                        .stream()
                        .filter(
                                methodInfo -> methodInfo.method.equals(method) && methodInfo.clazz.equals(clazz))
                        .findAny();
        if (optionalMethodInfo.isPresent()) {
            return optionalMethodInfo.get();
        }
        MethodInfo methodInfo = new MethodInfo(method, clazz);
        discovered.add(methodInfo);
        return methodInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodInfo)) return false;
        MethodInfo that = (MethodInfo) o;
        return method.equals(that.method) && clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, clazz);
    }

    public void setNonnullFieldsElements(Set<Element> nonnullFieldsAtExit) {
        if (nonnullFieldsAtExit == null) {
            nonnullFieldsAtExit = Collections.emptySet();
        }
        List<String> fields = new ArrayList<>();
        for (Element element : nonnullFieldsAtExit) {
            fields.add(element.getSimpleName().toString());
        }
        this.nonnullFields = fields.toArray(new String[0]);
    }

    public void setParent(Symbol.MethodSymbol methodSymbol, VisitorState state) {
        Symbol.MethodSymbol superMethod =
                SymbolUtil.getClosestOverriddenMethod(methodSymbol, state.getTypes());
        if (superMethod == null || superMethod.toString().equals("null")) {
            this.parent = top.id;
            return;
        }
        Symbol.ClassSymbol enclosingClass = ASTHelpers.enclosingClass(superMethod);
        MethodInfo superMethodInfo = findOrCreate(superMethod.toString(), enclosingClass.toString());
        this.parent = superMethodInfo.id;
    }

    public void setUri(CompilationUnitTree c) {
        this.uri = c.getSourceFile().toUri().toASCIIString();
    }

    @Override
    public String toString() {
        return id
                + "\t"
                + clazz
                + "\t"
                + method
                + "\t"
                + parent
                + "\t"
                + uri
                + "\t"
                + paramNumber
                + "\t"
                + Arrays.toString(annotFlags);
    }

    public static String header() {
        return "id" + "\t" + "class" + "\t" + "method" + "\t" + "parent" + "\t" + "uri" + "\t" + "size" + "\t" + "flags";
    }

    public void setParamNumber(int size) {
        this.paramNumber = size;
    }

    public void setParamAnnotations(List<Boolean> annotFlags) {
        if (annotFlags == null) {
            annotFlags = Collections.emptyList();
        }
        this.annotFlags = new Boolean[annotFlags.size()];
        this.annotFlags = annotFlags.toArray(this.annotFlags);
    }
}

