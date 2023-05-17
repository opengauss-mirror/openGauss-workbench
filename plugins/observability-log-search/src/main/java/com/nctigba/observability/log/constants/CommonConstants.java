package com.nctigba.observability.log.constants;

public class CommonConstants {
    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }
    public static final String HYPHEN = "-";
    public static final String FIELDS = "fields";
    public static final String LOG_TYPE = "log_type";
    public static final String LOG_LEVEL ="log_level";
    public static final String MESSAGE ="message";
    public static final String CLUSTER_ID = "clusterId";
    public static final String NODE_ID = "nodeId";
    public static final String TIMESTAMP = "@timestamp";

}
