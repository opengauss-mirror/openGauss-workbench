package com.nctigba.observability.sql.constants;

public class CommonConstants {
    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String EQUAL = "=";
    public static final String BLANK = " ";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String INSERT_INTO_PARAM_INFO = "insert into param_info(paramType,paramName,paramDetail,suggestValue,defaultValue,unit,suggestExplain,diagnosisRule) \n";
    public static final String HASH_COND = "Hash Cond";
    public static final String COST = "(cost=";
    public static final String BR = "<br/>";
    public static final String TITLE = "title";
    public static final String SAMPLES = "samples";
    public static final String PARTITION_RESULT_ARRAY = "partitionResultArray";
    public static final String IS_HAS_PART_KEY = "isHasPartKey";

    public static final String GET_INDEX_ADVICE_FAIL = "get index advice fail:{}";
    public static final String TABLENAME = "TABLENAME";
    public static final String DATA_FAIL = "data fail:{}";
    public static final String DIAGNOSIS_RULE = "diagnosisRule";

    /**
     * Max run node num
     */
    public static final int MAX_RUN_NODE_NUM = 5;

    /**
     * Param database name
     */
    public static final String PARAM_DATABASE_NAME = "paramDiagnosisInfoV1";

}
