package com.nctigba.observability.instance.constants;

public class JdbcConnectConstants {

    public static final String DEFAULT = "jdbc:postgresql://%s:%d/%s?connectTimeout=30&socketTimeout=30";

    public static final String OPENGAUSS = "jdbc:postgresql://%s:%d/%s?connectTimeout=30&socketTimeout=30";

    public static final String VASTBASE_G100 = "jdbc:vastbase://%s:%d/%s?connectTimeout=30&socketTimeout=30";

    public static final String MYSQL = "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&connetcTimeout=3000&socketTimeout=3600000";

    public static final String ORACLE = "jdbc:oracle:thin:@%s:%d:%s";
}
