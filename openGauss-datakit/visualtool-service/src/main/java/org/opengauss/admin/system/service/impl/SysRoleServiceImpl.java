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
 * SysRoleServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysRoleServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.constant.UserConstants;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.domain.SysRoleMenu;
import org.opengauss.admin.system.domain.SysUserRole;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.mapper.SysRoleMenuMapper;
import org.opengauss.admin.system.mapper.SysUserRoleMapper;
import org.opengauss.admin.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Role Service
 *
 * @author xielibo
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    /**
     * Query role data by pagination according to conditions
     *
     * @param role role
     */
    @Override
    public IPage<SysRole> selectRoleList(SysRole role, IPage<SysRole> page) {
        return roleMapper.selectRoleList(page, role);
    }

    /**
     * Query roles based on user ID
     *
     * @param userId userId
     */
    @Override
    public List<SysRole> selectRolesByUserId(Integer userId) {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles) {
            for (SysRole userRole : userRoles) {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue()) {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * Query role data based on conditions
     *
     * @param role role
     */
    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        return roleMapper.selectRoleList(role);
    }

    /**
     * Query permissions based on user ID
     *
     * @param userId userId
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Integer userId) {
        List<SysRole> perms = roleMapper.selectRoles(new QueryWrapper<SysRole>().eq("ur.user_id", userId).eq("r.del_flag", UserConstants.UN_DELETE));
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (perm != null) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * Query All Role
     *
     */
    @Override
    public List<SysRole> selectRoleAll() {
        return this.selectRoleList(new SysRole());
    }

    /**
     * Get the role selection box list according to the user ID
     *
     * @param userId userId
     */
    @Override
    public List<String> selectRoleListByUserId(Integer userId) {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * Query roles by role ID
     *
     */
    @Override
    public SysRole selectRoleById(Integer roleId) {

        SysRole role = roleMapper.selectRole(new QueryWrapper<SysRole>().eq("r.role_id", roleId));
        return role;
    }

    /**
     * Check if the role name is unique
     *
     * @param role role
     */
    @Override
    public String checkRoleNameUnique(SysRole role) {
        Integer roleId = role.getRoleId() == null ? 1 : role.getRoleId();
        SysRole info = roleMapper.selectRole(new QueryWrapper<SysRole>()
                .eq("r.role_name", role.getRoleName()).eq("r.del_flag", 0));
        if (info != null && !info.getRoleId().equals(roleId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * Check if the role allows the operation
     *
     */
    @Override
    public void checkRoleAllowed(SysRole role) {
        if (role.getRoleId() != null && role.isAdmin()) {
            throw new RuntimeException("operation not allowed");
        }
    }

    /**
     * Query the number of roles used by the role ID
     *
     */
    @Override
    public int countUserRoleByRoleId(Integer roleId) {
        return userRoleMapper.selectCount(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getRoleId, roleId)).intValue();
    }

    /**
     * Save Role
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role) {
        role.setRoleId(null);
        roleMapper.insert(role);
        return insertRoleMenu(role);
    }

    /**
     * Update Role
     *
     * @param role role
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role) {
        roleMapper.updateById(role);
        roleMenuMapper.delete(new QueryWrapper<SysRoleMenu>().lambda().eq(SysRoleMenu::getRoleId, role.getRoleId()));
        return insertRoleMenu(role);
    }

    /**
     * Update Status
     *
     * @param role role
     */
    @Override
    public int updateRoleStatus(SysRole role) {
        return roleMapper.updateById(role);
    }

    /**
     * Modify data permission information
     *
     * @param role role
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRole role) {
        return roleMapper.updateById(role);
    }

    /**
     * Added role menu relationship information
     *
     */
    public int insertRoleMenu(SysRole role) {
        int rows = 1;
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        Set<Integer> menuIds = Arrays.stream(role.getMenuIds()).collect(Collectors.toSet());
        menuIds.add(6);
        menuIds.add(605);
        for (Integer menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0) {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }


    /**
     * Batch delete role information
     *
     * @param roleIds roleIds
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(Integer[] roleIds) {
        for (Integer roleId : roleIds) {
            checkRoleAllowed(new SysRole(roleId));
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new CustomException(String.format("%1$s allocated, cannot be deleted", role.getRoleName()));
            }
        }
        roleMenuMapper.delete(new QueryWrapper<SysRoleMenu>().lambda().in(SysRoleMenu::getRoleId, roleIds));
        return roleMapper.deleteBatchIds(Arrays.asList(roleIds));
    }
}
