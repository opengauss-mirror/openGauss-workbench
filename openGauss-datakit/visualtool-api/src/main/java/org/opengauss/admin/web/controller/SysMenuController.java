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
 * SysMenuController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysMenuController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.TreeSelect;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.enums.SysMenuVisible;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.framework.web.service.TokenService;
import org.opengauss.admin.system.service.ISysMenuService;
import org.opengauss.admin.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu Controller
 *
 * @author xielibo
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController extends BaseController {

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysRoleService roleService;

    /**
     * get Menu list
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Integer userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return AjaxResult.success(menus);
    }

    /**
     * get info by ID
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @GetMapping(value = "/{menuId}")
    public AjaxResult getInfo(@PathVariable Integer menuId) {
        return AjaxResult.success(menuService.selectMenuById(menuId));
    }

    /**
     * Get treeselect menu list
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Integer userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return AjaxResult.success(menuService.buildMenuTreeSelect(menus));
    }

    /**
     *  Get treeselect menu list by role
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable("roleId") Integer roleId) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuList(loginUser.getUser().getUserId());
        menus = menus.stream().filter(m -> m.getVisible().equals(SysMenuVisible.SHOW.getCode())).collect(Collectors.toList());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        List<TreeSelect> trees = menuService.buildMenuTreeSelect(menus);
        SysRole role = roleService.selectRoleById(roleId);
        if (!role.isAdmin()) {
            trees = trees.stream().filter(t -> !t.getId().equals(6)).collect(Collectors.toList());
        }
        ajax.put("menus", trees);
        return ajax;
    }

    /**
     * save menu
     */
    @Log(title = "menus", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return AjaxResult.error(ResponseCode.MENU_NAME_IS_EXISTS_ERROR.code());
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        return toAjax(menuService.insertMenu(menu));
    }

    /**
     * update menu
     */
    @Log(title = "menus", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return AjaxResult.error(ResponseCode.MENU_NAME_IS_EXISTS_ERROR.code());
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return AjaxResult.error(ResponseCode.MENU_NOT_ADD_SELF_AS_SUBMENU_ERROR.code());
        }
        return toAjax(menuService.updateMenu(menu));
    }

    /**
     * delete menu
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "menus", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    public AjaxResult remove(@PathVariable("menuId") Integer menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return AjaxResult.error(ResponseCode.MENU_HAS_SUBMENU_NOT_DELETE.code());
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return AjaxResult.error(ResponseCode.MENU_ASSIGNED_NOT_DELETE.code());
        }
        return toAjax(menuService.deleteMenuById(menuId));
    }
}
