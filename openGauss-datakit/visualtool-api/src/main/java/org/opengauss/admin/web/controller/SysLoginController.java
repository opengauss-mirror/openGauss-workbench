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
 * SysLoginController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysLoginController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.controller;

import com.google.common.collect.Maps;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.core.domain.model.LoginBody;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.enums.SysMenuRouteOpenPosition;
import org.opengauss.admin.common.utils.RsaUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.framework.web.service.SysLoginService;
import org.opengauss.admin.framework.web.service.SysPermissionService;
import org.opengauss.admin.framework.web.service.TokenService;
import org.opengauss.admin.system.service.ISysMenuService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * common
 *
 * @author xielibo
 */
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * get login public key
     *
     * @return public key
     */
    @GetMapping("/pubKey")
    public AjaxResult publicKey() {
        return AjaxResult.success(RsaUtils.publicKey());
    }

    /**
     * get user info
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * get routers
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getUserId());
        String language = ServletUtils.getRequest().getHeader("language");
        return AjaxResult.success(menuService.buildMenus(menus, language));
    }

    /**
     * get index instance router
     */
    @GetMapping("getIndexInstanceRouters")
    public AjaxResult getIndexStanceRoute(){
        List<SysMenu> sysMenus = menuService.selectSpecialRouteList(SysMenuRouteOpenPosition.INDEX_INSTANCE_DATA.getCode());
        if (sysMenus.size() == 0) {
            return AjaxResult.success();
        }
        SysMenu m = sysMenus.get(0);
        if (StringUtils.isNotBlank(m.getPluginTheme())) {
            Map<String, Object> routeQuery = Maps.newHashMap();
            routeQuery.put("theme", m.getPluginTheme());
            m.setQuery(routeQuery);
        }
        return AjaxResult.success(m);
    }

}
