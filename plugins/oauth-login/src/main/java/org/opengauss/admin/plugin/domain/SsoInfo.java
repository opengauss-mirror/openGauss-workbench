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
 * SsoInfo.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/domain/SsoInfo.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.plugin.vo.AccessTokenResponseBody;

/**
 * @date 2024/5/31 19:39
 * @since 0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoInfo {
    private String devkit_id_token;
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String id_token;
    private String scope;
    private String uiid;
    private String ssoServerUrl;

    /**
     * constructor
     *
     * @param devkitIdToken devkitIdToken
     * @param ssoServerUrl ssoServerUrl
     */
    public SsoInfo(String devkitIdToken, String ssoServerUrl) {
        this.devkit_id_token = devkitIdToken;
        this.ssoServerUrl = ssoServerUrl;
    }

    /**
     * set the information carried by the accessTokenResponseBody
     *
     * @param responseBody responseBody
     */
    public void setAccessTokenResponse(AccessTokenResponseBody responseBody) {
        this.access_token = responseBody.getAccessToken();
        this.token_type = responseBody.getTokenType();
        this.refresh_token = responseBody.getRefreshToken();
        this.expires_in = responseBody.getExpiresIn();
        this.id_token = responseBody.getIdToken();
        this.scope = responseBody.getScope();
    }
}
