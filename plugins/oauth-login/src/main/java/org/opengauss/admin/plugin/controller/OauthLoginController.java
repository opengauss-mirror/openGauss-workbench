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
 * OauthLoginController.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/controller/OauthLoginController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.annotation.RateLimit;
import org.opengauss.admin.plugin.service.oauth.OauthLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @date 2024/5/30 10:34
 * @since 0.0
 */
@RestController
@RequestMapping("/oauth")
public class OauthLoginController {
    @Autowired
    private OauthLoginService oauthLoginService;

    /**
     * generate the url for obtaining the authorize code
     *
     * @param idToken id_token
     * @param ssoState sso_state
     * @return AjaxResult authorize url
     */
    @RateLimit
    @GetMapping("/authorize")
    public AjaxResult getAuthorizeUrl(
            @RequestParam(name = "id_token") String idToken, @RequestParam(name = "sso_state") String ssoState ) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("Location", oauthLoginService.getAuthorizeUrl(idToken, ssoState));
        return AjaxResult.success(resultMap);
    }

    /**
     * login as the sso mapping user
     *
     * @param code code
     * @param state state
     * @return org.opengauss.admin.common.core.domain.AjaxResult
     */
    @RateLimit
    @GetMapping("/token")
    public AjaxResult getToken(@RequestParam(required = true) String code, @RequestParam(required = true) String state) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", oauthLoginService.getToken(code, state));

        return AjaxResult.success(resultMap);
    }
}
