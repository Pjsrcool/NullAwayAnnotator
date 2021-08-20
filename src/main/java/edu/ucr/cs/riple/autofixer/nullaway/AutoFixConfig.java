package edu.ucr.cs.riple.autofixer.nullaway;

import com.google.common.base.Preconditions;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AutoFixConfig {

  public final boolean MAKE_METHOD_TREE_INHERITANCE_ENABLED;
  public final boolean MAKE_CALL_GRAPH_ENABLED;
  public final boolean MAKE_FIELD_GRAPH_ENABLED;
  public final boolean SUGGEST_ENABLED;
  public final boolean PARAM_TEST_ENABLED;
  public final boolean LOG_ERROR_ENABLED;
  public final boolean LOG_ERROR_DEEP;
  public final boolean OPTIMIZED;
  public final Integer PARAM_INDEX;
  public final AnnotationFactory ANNOTATION_FACTORY;
  public final Set<String> WORK_LIST;

  public AutoFixConfig() {
    MAKE_METHOD_TREE_INHERITANCE_ENABLED = false;
    SUGGEST_ENABLED = false;
    PARAM_TEST_ENABLED = false;
    LOG_ERROR_ENABLED = false;
    LOG_ERROR_DEEP = false;
    OPTIMIZED = false;
    MAKE_CALL_GRAPH_ENABLED = false;
    MAKE_FIELD_GRAPH_ENABLED = false;
    ANNOTATION_FACTORY = new AnnotationFactory();
    WORK_LIST = Collections.singleton("*");
    PARAM_INDEX = Integer.MAX_VALUE;
    Writer.reset(this);
  }

  public AutoFixConfig(boolean autofixEnabled, String filePath) {
    Preconditions.checkNotNull(filePath);
    JSONObject jsonObject;
    try {
      Object obj =
          new JSONParser()
              .parse(Files.newBufferedReader(Paths.get(filePath), Charset.defaultCharset()));
      ;
      jsonObject = (JSONObject) obj;
    } catch (Exception e) {
      throw new RuntimeException("Error in reading/parsing config at path: " + filePath);
    }
    MAKE_METHOD_TREE_INHERITANCE_ENABLED =
        getValueFromKey(jsonObject, "MAKE_METHOD_INHERITANCE_TREE", Boolean.class).orElse(false)
            && autofixEnabled;
    MAKE_CALL_GRAPH_ENABLED =
        getValueFromKey(jsonObject, "MAKE_CALL_GRAPH", Boolean.class).orElse(false)
            && autofixEnabled;
    MAKE_FIELD_GRAPH_ENABLED =
        getValueFromKey(jsonObject, "MAKE_FIELD_GRAPH", Boolean.class).orElse(false)
            && autofixEnabled;
    SUGGEST_ENABLED =
        getValueFromKey(jsonObject, "SUGGEST", Boolean.class).orElse(false) && autofixEnabled;
    PARAM_TEST_ENABLED =
            getValueFromKey(jsonObject, "METHOD_PARAM_TEST:ACTIVE", Boolean.class).orElse(false)
                    && autofixEnabled;
    PARAM_INDEX = getValueFromKey(jsonObject, "METHOD_PARAM_TEST:INDEX", Integer.class).orElse(Integer.MAX_VALUE);
    LOG_ERROR_ENABLED =
        getValueFromKey(jsonObject, "LOG_ERROR:ACTIVE", Boolean.class).orElse(false)
            && autofixEnabled;
    LOG_ERROR_DEEP =
        getValueFromKey(jsonObject, "LOG_ERROR:DEEP", Boolean.class).orElse(false)
            && autofixEnabled;
    OPTIMIZED =
        getValueFromKey(jsonObject, "OPTIMIZED", Boolean.class).orElse(false) && autofixEnabled;
    String nullableAnnot =
        getValueFromKey(jsonObject, "ANNOTATION:NULLABLE", String.class)
            .orElse("javax.annotation.Nullable");
    String nonnullAnnot =
        getValueFromKey(jsonObject, "ANNOTATION:NONNULL", String.class)
            .orElse("javax.annotation.Nonnull");
    this.ANNOTATION_FACTORY = new AnnotationFactory(nullableAnnot, nonnullAnnot);
    String WORK_LIST_VALUE = getValueFromKey(jsonObject, "WORK_LIST", String.class).orElse("*");
    if (!WORK_LIST_VALUE.equals("*")) {
      this.WORK_LIST = new HashSet<>(Arrays.asList(WORK_LIST_VALUE.split(",")));
    } else {
      this.WORK_LIST = Collections.singleton("*");
      ;
    }
    Writer.reset(this);
  }

  static class OrElse<T> {
    final Object value;
    final Class<T> klass;

    OrElse(Object value, Class<T> klass) {
      this.value = value;
      this.klass = klass;
    }

    T orElse(T other) {
      return value == null ? other : klass.cast(this.value);
    }
  }

  private <T> OrElse<T> getValueFromKey(JSONObject json, String key, Class<T> klass) {
    ArrayList<String> keys = new ArrayList<>(Arrays.asList(key.split(":")));
    while (keys.size() != 1) {
      if (json.containsKey(keys.get(0))) {
        json = (JSONObject) json.get(keys.get(0));
        keys.remove(0);
      } else {
        return new OrElse<>(null, klass);
      }
    }
    return json.containsKey(keys.get(0))
        ? new OrElse<>(json.get(keys.get(0)), klass)
        : new OrElse<>(null, klass);
  }

  public static class AutoFixConfigWriter {

    private boolean MAKE_METHOD_TREE_INHERITANCE_ENABLED;
    private boolean MAKE_CALL_GRAPH_ENABLED;
    private boolean MAKE_FIELD_GRAPH_ENABLED;
    private boolean SUGGEST_ENABLED;
    private boolean PARAM_TEST_ENABLED;
    private boolean LOG_ERROR_ENABLED;
    private boolean LOG_ERROR_DEEP;
    private boolean OPTIMIZED;
    private Integer PARAM_INDEX;
    private String NULLABLE;
    private String NONNULL;
    private Set<String> WORK_LIST;

    public AutoFixConfigWriter() {
      MAKE_METHOD_TREE_INHERITANCE_ENABLED = false;
      MAKE_CALL_GRAPH_ENABLED = false;
      MAKE_FIELD_GRAPH_ENABLED = false;
      SUGGEST_ENABLED = false;
      PARAM_TEST_ENABLED = false;
      LOG_ERROR_ENABLED = false;
      LOG_ERROR_DEEP = false;
      OPTIMIZED = false;
      PARAM_INDEX = Integer.MAX_VALUE;
      NULLABLE = "javax.annotation.Nullable";
      NONNULL = "javax.annotation.Nonnull";
      WORK_LIST = Collections.singleton("*");
    }

    private String workListDisplay() {
      if (WORK_LIST == null || WORK_LIST.size() == 0) {
        return "*";
      }
      String display = WORK_LIST.toString().replaceAll("\\s", "");
      return display.substring(1, display.length() - 1);
    }

    @SuppressWarnings("unchecked")
    public void writeInJson(String path) {
      JSONObject res = new JSONObject();
      res.put("SUGGEST", SUGGEST_ENABLED);
      res.put("MAKE_METHOD_INHERITANCE_TREE", MAKE_METHOD_TREE_INHERITANCE_ENABLED);
      res.put("OPTIMIZED", OPTIMIZED);
      JSONObject annotation = new JSONObject();
      annotation.put("NULLABLE", NULLABLE);
      annotation.put("NONNULL", NONNULL);
      res.put("ANNOTATION", annotation);
      JSONObject logError = new JSONObject();
      logError.put("ACTIVE", LOG_ERROR_ENABLED);
      logError.put("DEEP", LOG_ERROR_DEEP);
      res.put("LOG_ERROR", logError);
      JSONObject paramTest = new JSONObject();
      paramTest.put("ACTIVE", PARAM_TEST_ENABLED);
      paramTest.put("INDEX", PARAM_INDEX);
      res.put("METHOD_PARAM_TEST", paramTest);
      res.put("MAKE_CALL_GRAPH", MAKE_CALL_GRAPH_ENABLED);
      res.put("MAKE_FIELD_GRAPH", MAKE_FIELD_GRAPH_ENABLED);
      res.put("WORK_LIST", workListDisplay());
      try {
        BufferedWriter file = Files.newBufferedWriter(Paths.get(path), Charset.defaultCharset());
        file.write(res.toJSONString());
        file.flush();
      } catch (IOException e) {
        System.err.println("Error happened in writing config.");
      }
    }

    public AutoFixConfigWriter setSuggest(boolean value) {
      SUGGEST_ENABLED = value;
      return this;
    }

    public AutoFixConfigWriter setSuggest(boolean suggest, String NULLABLE, String NONNULL) {
      SUGGEST_ENABLED = suggest;
      if (!suggest) {
        throw new RuntimeException("SUGGEST must be activated");
      }
      this.NULLABLE = NULLABLE;
      this.NONNULL = NONNULL;
      return this;
    }

    public AutoFixConfigWriter setLogError(boolean value, boolean isDeep) {
      LOG_ERROR_ENABLED = value;
      if (!value && isDeep) {
        throw new RuntimeException("Log error must be enabled to activate deep log error");
      }
      LOG_ERROR_DEEP = isDeep;
      return this;
    }

    public AutoFixConfigWriter setMethodInheritanceTree(boolean value) {
      MAKE_METHOD_TREE_INHERITANCE_ENABLED = value;
      return this;
    }

    public AutoFixConfigWriter setMethodParamTest(boolean value, int index) {
      PARAM_TEST_ENABLED = value;
      if(value){
        PARAM_INDEX = index;
      }
      return this;
    }

    public AutoFixConfigWriter setOptimized(boolean value) {
      OPTIMIZED = value;
      return this;
    }

    public AutoFixConfigWriter setMakeCallGraph(boolean value) {
      MAKE_CALL_GRAPH_ENABLED = value;
      return this;
    }

    public AutoFixConfigWriter setMakeFieldGraph(boolean value) {
      MAKE_FIELD_GRAPH_ENABLED = value;
      return this;
    }

    public AutoFixConfigWriter setWorkList(String[] workList) {
      if (workList == null) {
        WORK_LIST = Collections.singleton("-");
        return this;
      }
      WORK_LIST = new HashSet<>(Arrays.asList(workList));
      return this;
    }

    public void write(String path) {
      writeInJson(path);
    }
  }
}
