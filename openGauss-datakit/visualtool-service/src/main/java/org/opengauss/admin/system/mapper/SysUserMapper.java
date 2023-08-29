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
 * SysUserMapper.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/mapper/SysUserMapper.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * User Mapper
 *
 * @author xielibo
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * Query users by condition
     *
     * @param queryWrapper queryWrapper
     */
    public SysUser selectUserVo(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * Query user list by pagination according to conditions
     *
     * @param page    page
     * @param sysUser sysUser
     */
    public IPage<SysUser> selectUserList(IPage<SysUser> page, @Param("entity") SysUser sysUser);

}
