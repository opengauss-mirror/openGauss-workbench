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
 * SysUserController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/SysUserController.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.utils.SecurityUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.service.ISysRoleService;
import org.opengauss.admin.system.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Controller
 *
 * @author xielibo
 */
@RestController
@RequestMapping("/system/user")
@Api(tags = "user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    /**
     * Query User Page List
     */
    @ApiOperation(value = "page list", notes = "page list")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        IPage<SysUser> result = userService.selectUserList(user,startPage());
        return getDataTable(result);
    }


    /**
     * Get By Id
     */
    @ApiOperation(value = "get by id", notes = "get by id")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Integer userId) {
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        if (StringUtils.isNotNull(userId)) {
            ajax.put(AjaxResult.DATA_TAG, userService.selectUserById(userId));
            ajax.put("roleIds", roleService.selectRoleListByUserId(userId));
        }
        return ajax;
    }

    /**
     * Save User
     */
    @Log(title = "users", businessType = BusinessType.INSERT)
    @ApiOperation(value = "save", notes = "save")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
            return AjaxResult.error(ResponseCode.USER_ACCOUNT_EXISTS_ERROR.code());
        } else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error(ResponseCode.USER_PHONE_EXISTS_ERROR.code());
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error(ResponseCode.USER_EMAIL_EXISTS_ERROR.code());
        }
        if (user.getPhonenumber().length() > 11) {
            return AjaxResult.error(ResponseCode.USER_TELEPHONE_MAX_LENGTH_ERROR.code());
        }
        if (user.getUserName().length() > 30) {
            return AjaxResult.error(ResponseCode.USER_NAME_MAX_LENGTH_ERROR.code());
        }
        if (user.getNickName().length() > 30) {
            return AjaxResult.error(ResponseCode.USER_NICKNAME_MAX_LENGTH_ERROR.code());
        }
        if (StringUtils.isNotEmpty(user.getRemark()) && user.getRemark().length() > 200) {
            return AjaxResult.error(ResponseCode.USER_REMARK_MAX_LENGTH_ERROR.code());
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * update user
     */
    @Log(title = "users", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "update", notes = "update")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user))) {
            return AjaxResult.error(ResponseCode.USER_PHONE_EXISTS_ERROR.code());
        } else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user))) {
            return AjaxResult.error(ResponseCode.USER_EMAIL_EXISTS_ERROR.code());
        }
        if (user.getPhonenumber().length() > 11) {
            return AjaxResult.error(ResponseCode.USER_TELEPHONE_MAX_LENGTH_ERROR.code());
        }
        if (user.getUserName().length() > 30) {
            return AjaxResult.error(ResponseCode.USER_NAME_MAX_LENGTH_ERROR.code());
        }
        if (user.getNickName().length() > 30) {
            return AjaxResult.error(ResponseCode.USER_NICKNAME_MAX_LENGTH_ERROR.code());
        }
        if (StringUtils.isNotEmpty(user.getRemark()) && user.getRemark().length() > 200) {
            return AjaxResult.error(ResponseCode.USER_REMARK_MAX_LENGTH_ERROR.code());
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * delete user
     */
    @Log(title = "users", businessType = BusinessType.DELETE)
    @ApiOperation(value = "delete", notes = "delete")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Integer[] userIds) {
        if (ArrayUtils.contains(userIds, getUserId())) {
            return error();
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * reset password
     */
    @Log(title = "users", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "reset pwd", notes = "reset pwd")
    @ApiImplicitParams({
    })
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * update status
     */
    @Log(title = "users", businessType = BusinessType.UPDATE)
    @ApiOperation(value = "update status", notes = "update status")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

}
