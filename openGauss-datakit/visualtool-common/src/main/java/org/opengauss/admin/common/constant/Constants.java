/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * Constants.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/constant/Constants.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.constant;


/**
 * Common Constant Information
 *
 * @author xielibo
 */
public class Constants {

    /**
     * UTF-8 encoding
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK encoding
     */
    public static final String GBK = "GBK";

    /**
     * http request
     */
    public static final String HTTP = "http://";

    /**
     * https request
     */
    public static final String HTTPS = "https://";

    /**
     * Login user cache key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * duplicate submission cache key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * limit cache key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";


    /**
     * token
     */
    public static final String TOKEN = "token";

    /**
     * token header
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * token expire time
     */
    public static final int TOKEN_EXPIRE_TIME = 60;

    /**
     * token prefix
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * login user key
     */
    public static final String LOGIN_USER_KEY = "login_user_key";


    /**
     * Resource Mapping Path Prefix
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * Plugin Web Resource Prefix
     */
    public static final String PLUGIN_PATH_PREFIX = "/static-plugin";

}
