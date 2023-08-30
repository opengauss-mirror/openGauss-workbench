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
 * SysRoleMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/mapper/SysRoleMapper.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Role Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * Query role data by pagination according to conditions
     *
     * @param page    page
     * @param sysRole sysRole
     */
    public IPage<SysRole> selectRoleList(IPage<SysRole> page, @Param("entity") SysRole sysRole);

    /**
     * Query role data based on pagination
     *
     * @param sysRole sysRole
     */
    public List<SysRole> selectRoleList(@Param("entity") SysRole sysRole);

    /**
     * Get the role selection box list according to the user ID
     *
     * @param userId userId
     */
    public List<String> selectRoleListByUserId(Integer userId);

    /**
     * Query roles based on user ID
     *
     * @param userId userId
     */
    public List<SysRole> selectRolePermissionByUserId(Integer userId);

    /**
     * Query role by condition
     *
     * @param queryWrapper queryWrapper
     */
    public SysRole selectRole(@Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper);

    /**
     * Query role by condition
     *
     * @param queryWrapper queryWrapper
     */
    public List<SysRole> selectRoles(@Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper);
}
