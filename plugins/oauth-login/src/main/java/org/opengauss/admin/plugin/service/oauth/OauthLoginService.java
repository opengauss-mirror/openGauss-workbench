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
 * OauthLoginService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/oauth/OauthLoginService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.oauth;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.constants.MyClientConfigConstants;
import org.opengauss.admin.plugin.constants.MyConstants;
import org.opengauss.admin.plugin.domain.PkceBody;
import org.opengauss.admin.plugin.domain.SsoInfo;
import org.opengauss.admin.plugin.domain.entity.SsoUser;
import org.opengauss.admin.plugin.exception.OauthLoginException;
import org.opengauss.admin.plugin.monitor.LogoutMonitor;
import org.opengauss.admin.plugin.monitor.RefreshTokenMonitor;
import org.opengauss.admin.plugin.service.sys.SysLoginService;
import org.opengauss.admin.plugin.utils.PKCEGenerator;
import org.opengauss.admin.plugin.vo.AccessTokenResponseBody;
import org.opengauss.admin.plugin.vo.UserinfoResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @date 2024/6/13 16:37
 * @since 0.0
 */
@Slf4j
@Service
public class OauthLoginService {

    @Autowired
    private Cache<String, PkceBody> statePkceCache;

    @Autowired
    private Cache<String, SsoInfo> ssoInfoCache;

    @Autowired
    private SsoRequestService ssoRequestService;

    @Autowired
    private OauthLoginUserService oauthLoginUserService;

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private Set<String> tokenCacheSet;

    @Autowired
    private LogoutMonitor logoutMonitor;

    @Autowired
    private RefreshTokenMonitor refreshTokenMonitor;

    /**
     * generate the url for obtaining the authorize code
     *
     * @param idToken id_token
     * @param ssoState sso_state
     * @return String authorizeUrl
     */
    public String getAuthorizeUrl(String idToken, String ssoState) {
        String response_type = MyConstants.AUTHORIZE_RESPONSE_TYPE;
        String client_id = MyClientConfigConstants.clientId;
        String redirect_uri = MyClientConfigConstants.dataKitUrl + MyConstants.REDIRECT_URI;
        String scope = MyConstants.AUTHORIZE_SCOPE;

        String state = String.valueOf(UUID.randomUUID());
        String code_verifier = PKCEGenerator.generateCodeVerifier();
        String code_challenge = "";
        try {
            code_challenge = PKCEGenerator.generateCodeChallenge(code_verifier);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String code_challenge_method = MyConstants.PKCE_CODE_CHALLENGE_METHOD;
        statePkceCache.put(state, new PkceBody(code_verifier, code_challenge, code_challenge_method));
        ssoInfoCache.put(state, new SsoInfo(idToken, MyClientConfigConstants.devKitUrl));

        String devkit_url = MyClientConfigConstants.devKitUrl;
        String authorize_url = MyConstants.AUTHORIZE_URL;
        String baseUrl = devkit_url + authorize_url;

        Map<String, String> params = new HashMap<>();
        params.put("response_type", response_type);
        params.put("client_id", client_id);
        params.put("redirect_uri", redirect_uri);
        params.put("scope", scope);
        params.put("state", state);
        params.put("code_challenge", code_challenge);
        params.put("code_challenge_method", code_challenge_method);
        params.put("id_token", idToken);
        params.put("sso_state", ssoState);

        return getUrl(params, baseUrl);
    }

    /**
     * login as the sso mapping user
     *
     * @param code code
     * @param state state
     * @return String token
     */
    public String getToken(String code, String state) {
        // Verify that the code is not empty.
        if (StringUtils.isEmpty(code)) {
            throw new OauthLoginException("Verifying authorization information failed. Code is empty!");
        }
        // Verify that state is not empty and valid.
        if (StringUtils.isEmpty(state)) {
            throw new OauthLoginException("Verifying authorization information failed. State is empty!");
        } else if (!statePkceCache.asMap().containsKey(state)) {
            throw new OauthLoginException("Verifying authorization information failed. State is invalid!");
        }

        // Get historical sso information.
        SsoInfo ssoInfo = ssoInfoCache.asMap().remove(state);

        // Obtain access_token.
        AccessTokenResponseBody accessTokenResponseBody = ssoRequestService.accessToken(code, state);
        if (ObjectUtils.isEmpty(accessTokenResponseBody) || accessTokenResponseBody.getAccessToken().isEmpty()) {
            log.error("Get sso access token failed!");
            throw new OauthLoginException("Get sso access token failed!");
        }

        // Stores access_token information.
        ssoInfo.setAccessTokenResponse(accessTokenResponseBody);

        // Get userinfo.
        UserinfoResponseBody userinfoResponseBody = ssoRequestService.userinfo(ssoInfo);
        if (ObjectUtils.isEmpty(userinfoResponseBody) || userinfoResponseBody.getUiid().isEmpty()) {
            log.error("Get sso user info failed!");
            throw new OauthLoginException("Get sso user info failed!");
        }

        // Store uiid information.
        ssoInfo.setUiid(userinfoResponseBody.getUiid());

        // Construct the ssoUser user object from the obtained userinfo.
        SsoUser ssoUser = new SsoUser(userinfoResponseBody, MyClientConfigConstants.devKitUrl);
        if (!oauthLoginUserService.isOauthLoginUserExists(ssoUser)) {
            oauthLoginUserService.addOauthLoginUser(ssoUser);
        }

        // Detects whether the user has logged in through sso.
        for (String tokenExists : tokenCacheSet) {
            SsoInfo ssoInfoExists = ssoInfoCache.asMap().get(tokenExists);
            if (ssoInfoExists.getUiid().equals(ssoInfo.getUiid())) {
                ssoInfoCache.put(tokenExists, ssoInfo);
                return tokenExists;
            }
        }

        String token = sysLoginService.login(oauthLoginUserService.getSysUser(ssoUser));

        // Store tokens in the cache.
        tokenCacheSet.add(token);

        // Start a scheduled task to detect the logout status and refresh the token.
        logoutMonitor.startTask();
        refreshTokenMonitor.startTask();

        // Stores sso login user information in the cache.
        ssoInfoCache.put(token, ssoInfo);

        return token;
    }

    /**
     * refresh access token
     *
     * @param token token
     */
    public void refreshToken(String token) {
        SsoInfo ssoInfo = ssoInfoCache.asMap().get(token);
        AccessTokenResponseBody responseBody = ssoRequestService.refreshToken(ssoInfo);
        ssoInfo.setAccessTokenResponse(responseBody);
        log.info("refresh token");
    }

    /**
     * This method is used to add the given parameter to the specified URL.
     *
     * @param params params
     * @param location location
     * @return String url
     */
    private String getUrl(Map<String, String> params, String location) {
        StringBuilder redirectUrl = new StringBuilder(location);
        if (!params.isEmpty()) {
            redirectUrl.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                try {
                    redirectUrl.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // Remove the trailing "&".
            redirectUrl.deleteCharAt(redirectUrl.length() - 1);
        }
        return redirectUrl.toString();
    }
}
