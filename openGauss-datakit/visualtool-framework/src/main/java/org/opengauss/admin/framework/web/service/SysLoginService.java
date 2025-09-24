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
 * SysLoginService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/web/service/SysLoginService
 * .java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.framework.web.service;

import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.common.exception.user.UserPasswordNotMatchException;
import org.opengauss.admin.common.utils.RsaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Login Service
 *
 * @author xielibo
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;
    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * Login
     *
     * @param username username
     * @param password password
     * @param code code
     * @return result
     * @throws UserPasswordNotMatchException UserPasswordNotMatchException
     * @throws ServiceException ServiceException
     */
    public String login(String username, String password, String code)
        throws UserPasswordNotMatchException, ServiceException {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, RsaUtils.decrypt(password)));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                throw new UserPasswordNotMatchException();
            } else {
                throw new ServiceException(e.getMessage());
            }
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }
}
