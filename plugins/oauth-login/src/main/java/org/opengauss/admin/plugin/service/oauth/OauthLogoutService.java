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
 * OauthLogoutService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/oauth/OauthLogoutService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.oauth;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.SsoInfo;
import org.opengauss.admin.plugin.domain.entity.SsoUser;
import org.opengauss.admin.plugin.exception.OauthLoginException;
import org.opengauss.admin.plugin.service.SsoUserService;
import org.opengauss.admin.plugin.vo.CancelTokenResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @date 2024/6/13 17:17
 * @since 0.0
 */
@Slf4j
@Service
public class OauthLogoutService {
    @Autowired
    private Cache<String, SsoInfo> ssoInfoCache;

    @Autowired
    private SsoRequestService ssoRequestService;

    @Autowired
    private SsoUserService ssoUserService;

    /**
     * logout oauth login
     *
     * @param token token
     */
    public void logout(String token) {
        SsoInfo ssoInfo = ssoInfoCache.asMap().remove(token);

        CancelTokenResponseBody cancelTokenResponseBody = ssoRequestService.cancelToken(ssoInfo);
        // Get response information.
        if (cancelTokenResponseBody == null) {
            log.error("Sso logout failed!");
            throw new OauthLoginException("Sso logout failed!");
        }
        String description = cancelTokenResponseBody.getDescription();

        // Verify response information and print SSO logout results.
        String successInfo = "TOKEN CANCELED SUCCESSFULLY";
        if (description.contains(successInfo)) {
            log.info("Sso logout success!");
        } else {
            log.error("Sso logout failed!");
        }

        // The printed log indicates that the sso user is logged out.
        SsoUser ssoUser = ssoUserService.getOneByUiidAndSsoServerUrl(ssoInfo.getUiid(), ssoInfo.getSsoServerUrl());
        log.info("DevKit url : " + ssoUser.getSsoServerUrl() + " DevKit user : " + ssoUser.getName() + " logout success!");
    }
}
