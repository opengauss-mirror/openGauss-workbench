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
 * SysRoleController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysRoleController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.core.domain.model.LoginUser;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.framework.web.service.SysPermissionService;
import org.opengauss.admin.framework.web.service.TokenService;
import org.opengauss.admin.system.service.ISysRoleService;
import org.opengauss.admin.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * roles
 *
 * @author xielibo
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService userService;

    @GetMapping("/list")
    public TableDataInfo list(SysRole role) {
        IPage<SysRole> list = roleService.selectRoleList(role,startPage());
        return getDataTable(list);
    }


    /**
     * get role by roleI
     */
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Integer roleId) {
        return AjaxResult.success(roleService.selectRoleById(roleId));
    }

    /**
     * save
     */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role) {
        if (StringUtils.isBlank(role.getRoleName())) {
            return AjaxResult.error(ResponseCode.ROLE_NAME_IS_NOT_EMPTY_ERROR.code());
        }
        if (role.getRoleName().length() > 25) {
            return AjaxResult.error(ResponseCode.ROLE_NAME_MAX_LENGTH_ERROR.code());
        }
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return AjaxResult.error(ResponseCode.ROLE_EXISTS_ERROR.code());
        }
        if (StringUtils.isNotEmpty(role.getRemark()) && role.getRemark().length() > 200) {
            return AjaxResult.error(ResponseCode.ROLE_REMARK_MAX_LENGTH_ERROR.code());
        }
        role.setCreateBy(SecurityUtils.getUsername());
        return toAjax(roleService.insertRole(role));

    }

    /**
     * update
     */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        if (StringUtils.isBlank(role.getRoleName())) {
            return AjaxResult.error(ResponseCode.ROLE_NAME_IS_NOT_EMPTY_ERROR.code());
        }
        if (role.getRoleName().length() > 25) {
            return AjaxResult.error(ResponseCode.ROLE_NAME_MAX_LENGTH_ERROR.code());
        }
        if (UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))) {
            return AjaxResult.error(ResponseCode.ROLE_EXISTS_ERROR.code());
        }
        if (StringUtils.isNotEmpty(role.getRemark()) && role.getRemark().length() > 200) {
            return AjaxResult.error(ResponseCode.ROLE_REMARK_MAX_LENGTH_ERROR.code());
        }
        if (roleService.updateRole(role) > 0) {
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.getOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, loginUser.getUser().getUserName())));
                tokenService.setLoginUser(loginUser);
            }
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    /**
     * update data scope
     */
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * update status
     */
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * delete
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Integer[] roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * optionselect
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        return AjaxResult.success(roleService.selectRoleAll());
    }
}

