package util;


public class Constants {
    //IDs FOR ELEMENT AGGREGATION, the name of the attribute that is used as element ID
    static final String METRIC_ID = "metric";
    static final String FACTOR_ID = "factor";
    static final String STRATEGIC_INDICATOR_ID = "strategic_indicator";

    //content of the field _type of the index
    static final String METRIC_TYPE = "metrics";
    public static final String FACTOR_TYPE = "factors";
    public static final String STRATEGIC_INDICATOR_TYPE = "strategic_indicators";
    static final String RELATIONS_TYPE = "relations";

    //FIELDS//
    // related to the element
    public static final String NAME = "name";
    static final String DESCRIPTION = "description";
    // related to the evaluation
    public static final String EVALUATION_DATE = "evaluationDate";
    public static final String VALUE = "value";
    static final String DATA_SOURCE = "datasource";
    static final String PROJECT = "project";                    // project being evaluated         (new v1.0)
    static final String RATIONALE = "info";                     // explanaiton of the element value  (new v1.0)
    static final String MISSING_FACTORS = "missing_factors";    // factors without assessment        (new v1.0)
    static final String DATES_MISMATCH = "dates_mismatch_days"; // new (v1.0
                                                                // maximum difference (in days) when there is
                                                                // difference evaluation dates between the SI and Factor
    // related to the relations
    public static final String RELATION = "relation";
    public static final String SOURCEID = "sourceId";
    public static final String SOURCETYPE = "sourceType";
    public static final String TARGETID = "targetId";
    public static final String TARGETTPYE = "targetType";
    public static final String WEIGHT = "weight";
    public static final String TARGETVALUE = "targetValue";
    public static final String SOURCELABEL = "sourceLabel";

    //INDEXES//
    public static String PATH = "";             // public, it is accessed from outside of the package
    public static String INDEX_PREFIX = "poc."; // public, it is accessed from outside of the package
    public static final String INDEX_STRATEGIC_INDICATORS = "strategic_indicators";
    public static final String INDEX_FACTORS = "factors";
    public static final String INDEX_METRICS = "metrics";
    public static final String INDEX_RELATIONS = "relations";

    //ARRAYS//
    static final String ARRAY_FACTORS = "factors";
    static final String ARRAY_STRATEGIC_INDICATORS = "indicators";  // strategic_indicators versions previous to v1.0

    //ESTIMATIONS//
    static final String ESTIMATION = "estimation";
    static final String ESTIMATION_ID = "id";
    static final String ESTIMATION_VALUE = "value";
    static final String ESTIMATION_LABEL = "label";
    static final String ESTIMATION_UPPER_THRESHOLD = "upperThreshold";

    //OTHERS//
    public enum QMLevel {metrics, factors, strategic_indicators, relations}

    //MAPPINGS//
    static final String STRATEGIC_INDICATORS_MAPPING =
            "{\n" +
                "\"mappings\" : {\n" +
                    "\"" + STRATEGIC_INDICATOR_TYPE + "\" : {\n" +
                        "\"properties\" : {\n" +
                            "\"datasource\" : {\"type\" : \"keyword\"},\n" +
                            "\"dates_mismatch_days\" : {\"type\" : \"long\"},\n" +
                            "\"description\" : {\"type\" : \"text\"},\n" +
                            "\"evaluationDate\" : {\"type\" : \"date\"},\n" +
                            "\"missing_factors\" : {\"type\" : \"text\",\n" +
                            "\"fields\" : {\"keyword\" : {\"type\" : \"keyword\",\n" +
                            "\"ignore_above\" : 256}}},\n" +
                            "\"name\" : {\"type\" : \"keyword\"},\n" +
                            "\"project\" : {\"type\" : \"keyword\"},\n" +
                            "\"type\": { \"type\": \"keyword\"},\n" +
                            "\"strategic_indicator\" : {\"type\" : \"keyword\"},\n" +
                            "\"value\" : {\"type\" : \"double\"}\n" +
                        "}\n" +
                    "}\n" +
                "}\n" +
            "}";
}
