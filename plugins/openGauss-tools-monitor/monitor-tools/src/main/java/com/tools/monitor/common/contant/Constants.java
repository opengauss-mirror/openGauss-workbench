package com.tools.monitor.common.contant;

/**
 * Constants
 *
 * @author liu
 * @since 2022-10-01
 */
public class Constants {
    /**
     * PROM
     */
    public static final String PROM = "Prometheus";

    /**
     * ZABBIX
     */
    public static final String ZABBIX = "Zabbix";

    /**
     *NAGIOS
     */
    public static final String NAGIOS = "Nagios";

    /**
     * SYSTEMTARGET
     */
    public static final String SYSTEMTARGET = "system_default";

    /**
     * DEFAULTERNUM
     */
    public static final long DEFAULTERNUM = 1669780925627L;

    /**
     * UTF-8
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK
     */
    public static final String GBK = "GBK";

    /**
     * http
     */
    public static final String HTTP = "http://";

    /**
     * https
     */
    public static final String HTTPS = "https://";

    /**
     * SUCCESS
     */
    public static final String SUCCESS = "0";

    /**
     * FAIL
     */
    public static final String FAIL = "1";

    /**
     * LOGIN_SUCCESS
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * LOGOUT
     */
    public static final String LOGOUT = "Logout";

    /**
     * REGISTER
     */
    public static final String REGISTER = "Register";

    /**
     * LOGIN_FAIL
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * CAPTCHA_EXPIRATION
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;

    /**
     * TOKEN
     */
    public static final String TOKEN = "token";

    /**
     * TOKEN_PREFIX
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * LOGIN_USER_KEY
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * JWT_USERID
     */
    public static final String JWT_USERID = "userid";


    /**
     * JWT_AVATAR
     */
    public static final String JWT_AVATAR = "avatar";

    /**
     * JWT_CREATED
     */
    public static final String JWT_CREATED = "created";

    /**
     * JWT_AUTHORITIES
     */
    public static final String JWT_AUTHORITIES = "authorities";

    /**
     * RESOURCE_PREFIX
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * LOOKUP_RMI
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LOOKUP_LDAP
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LOOKUP_LDAPS
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * JOB_WHITELIST_STR
     */
    public static final String[] JOB_WHITELIST_STR = {"com.tools.monitor"};

    /**
     * second
     */
    public static final String SECOND = "second";

    /**
     * MINUTE
     */
    public static final String MINUTE = "minute";

    /**
     * HOUR
     */
    public static final String HOUR = "hour";

    /**
     * DAY
     */
    public static final String DAY = "day";

    /**
     * WEEK
     */
    public static final String WEEK = "week";

    /**
     * MONTH
     */
    public static final String MONTH = "month";

    /**
     * YEAR
     */
    public static final String YEAR = "year";

    /**
     * JOB_ERROR_STR
     */
    public static final String[] JOB_ERROR_STR = {"java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.tools.monitor.common.utils.file"};
}
