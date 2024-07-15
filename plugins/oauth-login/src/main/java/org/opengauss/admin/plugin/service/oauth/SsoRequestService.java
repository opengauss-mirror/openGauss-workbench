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
 * SsoRequestService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/oauth/SsoRequestService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.oauth;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.constants.MyClientConfigConstants;
import org.opengauss.admin.plugin.constants.MyConstants;
import org.opengauss.admin.plugin.domain.PkceBody;
import org.opengauss.admin.plugin.domain.SsoInfo;
import org.opengauss.admin.plugin.utils.HttpRequestUtils;
import org.opengauss.admin.plugin.vo.AccessTokenResponseBody;
import org.opengauss.admin.plugin.vo.CancelTokenResponseBody;
import org.opengauss.admin.plugin.vo.UserinfoResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @date 2024/6/13 14:40
 * @since 0.0
 */
@Slf4j
@Service
public class SsoRequestService {

    @Autowired
    private Cache<String, PkceBody> statePkceCache;

    /**
     * request sso to get access_token
     *
     * @param code code
     * @param state state
     * @return AccessTokenResponseBody
     */
    public AccessTokenResponseBody accessToken(String code, String state) {
        // Build the url to get access_token.
        String accessTokenUrl = MyClientConfigConstants.devKitUrl + MyConstants.ACCESS_TOKEN_URL;

        // Remove state to get Pkce entity information.
        PkceBody pkceBody = statePkceCache.asMap().remove(state);

        // Construct request body.
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", MyConstants.ACCESS_TOKEN_GRANT_TYPE);
        requestBody.put("code", code);
        requestBody.put("client_id", MyClientConfigConstants.clientId);
        requestBody.put("client_secret", MyClientConfigConstants.clientSecret);
        requestBody.put("redirect_uri", MyClientConfigConstants.dataKitUrl + MyConstants.REDIRECT_URI);
        requestBody.put("code_verifier", pkceBody.getCode_verifier());

        // Send Post request
        String responseResult = HttpRequestUtils.postRequest(accessTokenUrl, requestBody);
        if (StringUtils.isEmpty(responseResult)) {
            return null;
        }
        // Return accessToken.
        return JSON.parseObject(responseResult, AccessTokenResponseBody.class);
    }

    /**
     * request sso to get userinfo
     *
     * @param ssoInfo ssoInfo
     * @return UserinfoResponseBody
     */
    public UserinfoResponseBody userinfo(SsoInfo ssoInfo) {
        // Build the url to get userinfo.
        String userinfoUrl = MyClientConfigConstants.devKitUrl + MyConstants.USER_INFO_URL;

        // Construct request body.
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("id_token", ssoInfo.getId_token());

        // Send Post request.
        String authorization = ssoInfo.getToken_type() + " " + ssoInfo.getAccess_token();
        String responseResult = HttpRequestUtils.postRequest(userinfoUrl, authorization, requestBody);
        if (StringUtils.isEmpty(responseResult)) {
            return null;
        }
        // Return userinfo.
        return JSON.parseObject(responseResult, UserinfoResponseBody.class);
    }

    /**
     * request sso to refresh token
     *
     * @param ssoInfo ssoInfo
     * @return AccessTokenResponseBody
     */
    public AccessTokenResponseBody refreshToken(SsoInfo ssoInfo) {
        // Build the url to refresh token.
        String refreshTokenUrl = MyClientConfigConstants.devKitUrl + MyConstants.REFRESH_TOKEN_URL;

        // Construct request body.
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", MyConstants.REFRESH_TOKEN_GRANT_TYPE);
        requestBody.put("access_token", ssoInfo.getAccess_token());
        requestBody.put("id_token", ssoInfo.getId_token());
        requestBody.put("scope", ssoInfo.getScope());

        // Send Post request.
        String authorization = ssoInfo.getToken_type() + " " + ssoInfo.getRefresh_token();
        String responseResult = HttpRequestUtils.postRequest(refreshTokenUrl, authorization, requestBody);
        if (StringUtils.isEmpty(responseResult)) {
            return null;
        }
        // Return accessToken.
        return JSON.parseObject(responseResult, AccessTokenResponseBody.class);
    }

    /**
     * request sso to cancel token
     *
     * @param ssoInfo ssoInfo
     * @return CancelTokenResponseBody
     */
    public CancelTokenResponseBody cancelToken(@NotNull SsoInfo ssoInfo) {
        // Build the url to cancel token.
        String cancelTokenUrl = MyClientConfigConstants.devKitUrl + MyConstants.CANCEL_TOKEN_URL;

        // Construct request body.
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("id_token", ssoInfo.getId_token());

        // Send Delete request.
        String authorization = ssoInfo.getToken_type() + " " + ssoInfo.getAccess_token();
        String responseResult = HttpRequestUtils.deleteRequest(cancelTokenUrl, authorization, requestBody);
        if (StringUtils.isEmpty(responseResult)) {
            return null;
        }
        return JSON.parseObject(responseResult, CancelTokenResponseBody.class);
    }

}
