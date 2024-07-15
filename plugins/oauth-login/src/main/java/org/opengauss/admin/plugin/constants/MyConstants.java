/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * MyConstants.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/constants/MyConstants.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.constants;

import org.springframework.http.MediaType;

/**
 * @date 2024/5/31 15:13
 * @since 0.0
 */
public class MyConstants {

    public static final int ACCESS_TOKEN_EXPIRES_TOME = 30;

    public static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_UTF8_VALUE;

    public static final String AUTHORIZATION = "Authorization";

    public static final String REDIRECT_URI = "/static-plugin/oauth-login/oauth/login";;

    public static final String AUTHORIZE_RESPONSE_TYPE = "code";

    public static final String AUTHORIZE_SCOPE = "all";

    public static final String AUTHORIZE_URL = "/oauth/login";

    public static final String PKCE_CODE_CHALLENGE_METHOD = "SHA-256";

    public static final String ACCESS_TOKEN_GRANT_TYPE = "authorization_code";

    public static final String ACCESS_TOKEN_URL = "/oauth/access_token";

    public static final String USER_INFO_URL = "/oauth/userinfo";

    public static final String REFRESH_TOKEN_URL = "/oauth/refresh_token";

    public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";

    public static final String CANCEL_TOKEN_URL = "/oauth/cancel_token";
}
