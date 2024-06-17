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
 * SysLoginService.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/sys/SysLoginService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.sys;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.framework.web.service.SysPermissionService;
import org.opengauss.admin.framework.web.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @date 2024/5/31 21:50
 * @since 0.0
 */
@Service
public class SysLoginService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysPermissionService permissionService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private TokenService tokenService;

    /**
     * login with mapping user
     *
     * @param sysUser sysUser
     * @return String token
     */
    public String login(SysUser sysUser) {
        return getToken(createLoginUser(sysUser));
    }

    /**
     * create loginUser
     *
     * @param user sysUser
     * @return LoginUser
     */
    public LoginUser createLoginUser(SysUser user) {
        return new LoginUser(user, permissionService.getMenuPermission(user));
    }

    /**
     * generate token
     *
     * @param loginUser loginUser
     * @return String token
     */
    private String getToken(LoginUser loginUser) {
        return tokenService.createToken(loginUser);
    }
}
