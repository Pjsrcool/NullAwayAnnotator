package edu.ucr.cs.riple.autofixer.nullaway;

import org.json.simple.JSONObject;

import java.util.Objects;

@SuppressWarnings("ALL")
public class FixDisplay {
    public final String annotation;
    public final String method;
    public final String param;
    public final String location;
    public final String className;
    public final String pkg;
    public final String inject;
    public final String compulsory;
    public String uri;

    public FixDisplay(
            String annotation,
            String method,
            String param,
            String location,
            String className,
            String pkg,
            String uri,
            String inject,
            String compulsory) {
        this.annotation = annotation;
        this.method = method;
        this.param = param;
        this.location = location;
        this.className = className;
        this.pkg = pkg;
        this.uri = uri;
        this.inject = inject;
        this.compulsory = compulsory;
    }

    @Override
    public String toString() {
        return "\n  {"
                + "\n\tannotation='"
                + annotation
                + '\''
                + ", \n\tmethod='"
                + method
                + '\''
                + ", \n\tparam='"
                + param
                + '\''
                + ", \n\tlocation='"
                + location
                + '\''
                + ", \n\tclassName='"
                + className
                + '\''
                + ", \n\tpkg='"
                + pkg
                + '\''
                + ", \n\tinject='"
                + inject
                + '\''
                + ", \n\turi='"
                + uri
                + '\''
                + ", \n\tcompulsory='"
                + compulsory
                + '\''
                + "\n  }\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixDisplay)) return false;
        FixDisplay fix = (FixDisplay) o;
        return Objects.equals(annotation, fix.annotation)
                && Objects.equals(method, fix.method)
                && Objects.equals(param, fix.param)
                && Objects.equals(location, fix.location)
                && Objects.equals(className, fix.className)
                && Objects.equals(pkg, fix.pkg)
                && Objects.equals(inject, fix.inject)
                && Objects.equals(uri, fix.uri)
                && Objects.equals(compulsory, fix.compulsory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                annotation, method, param, location, className, pkg, inject, uri, compulsory);
    }

    public static FixDisplay fromCSVLine(String line) {
        String[] infos = line.split(Writer.getDelimiterRegex());
        return new FixDisplay(
                infos[7], infos[3], infos[4], infos[0], infos[2], infos[1], infos[5], infos[9], infos[8]);
    }

    public JSONObject getJson(){
        JSONObject res = new JSONObject();
        res.put("annotation", this.annotation);
        res.put("method", this.method);
        res.put("param", this.param);
        res.put("location", this.location);
        res.put("class", this.className);
        res.put("pkg", this.pkg);
        res.put("inject", this.inject);
        res.put("uri", this.uri);
        res.put("compulsory", this.compulsory);
        return res;
    }
}
