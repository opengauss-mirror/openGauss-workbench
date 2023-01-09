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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "roles")
public class SysRoleController extends BaseController {
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService userService;

    @ApiOperation(value = "role list", notes = "role list")
    @ApiImplicitParams({
    })
    @GetMapping("/list")
    public TableDataInfo list(SysRole role) {
        IPage<SysRole> list = roleService.selectRoleList(role,startPage());
        return getDataTable(list);
    }


    /**
     * get role by roleI
     */
    @ApiOperation(value = "getByRoleId", notes = "getByRoleId")
    @ApiImplicitParams({
    })
    @GetMapping(value = "/{roleId}")
    public AjaxResult getInfo(@PathVariable Integer roleId) {
        return AjaxResult.success(roleService.selectRoleById(roleId));
    }

    /**
     * save
     */
    @ApiOperation(value = "save", notes = "save")
    @ApiImplicitParams({
    })
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
    @ApiOperation(value = "update", notes = "update")
    @ApiImplicitParams({
    })
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
    @ApiOperation(value = "update data scope", notes = "update data scope")
    @ApiImplicitParams({
    })
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * update status
     */
    @ApiOperation(value = "update status", notes = "update status")
    @ApiImplicitParams({
    })
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role) {
        roleService.checkRoleAllowed(role);
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * delete
     */
    @ApiOperation(value = "delete", notes = "delete")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public AjaxResult remove(@PathVariable Integer[] roleIds) {
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    /**
     * optionselect
     */
    @ApiOperation(value = "optionselect", notes = "optionselect")
    @ApiImplicitParams({
    })
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        return AjaxResult.success(roleService.selectRoleAll());
    }
}

