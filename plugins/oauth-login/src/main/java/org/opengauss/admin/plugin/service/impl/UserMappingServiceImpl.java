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
 * UserMappingServiceImpl.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/impl/UserMappingServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.UserMapping;
import org.opengauss.admin.plugin.mapper.UserMappingMapper;
import org.opengauss.admin.plugin.service.UserMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @date 2024/6/7 11:44
 * @since 0.0
 */
@Service
public class UserMappingServiceImpl extends ServiceImpl<UserMappingMapper, UserMapping> implements UserMappingService {
    @Autowired
    private UserMappingMapper userMappingMapper;

    /**
     * getOneBySsoUserId
     *
     * @param ssoUserId ssoUserId
     * @return UserMapping
     */
    @Override
    public UserMapping getOneBySsoUserId(Integer ssoUserId) {
        LambdaQueryWrapper<UserMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserMapping::getSsoUserId, ssoUserId);
        return userMappingMapper.selectOne(queryWrapper);
    }

    /**
     * getListBySsoUserIds
     *
     * @param ssoUserIds ssoUserIds
     * @return List<UserMapping>
     */
    @Override
    public List<UserMapping> getListBySsoUserIds(List<Integer> ssoUserIds) {
        LambdaQueryWrapper<UserMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserMapping::getSsoUserId, ssoUserIds);
        return userMappingMapper.selectList(queryWrapper);
    }

    /**
     * getListBySysUserIds
     *
     * @param sysUserIds sysUserIds
     * @return List<UserMapping>
     */
    @Override
    public List<UserMapping> getListBySysUserIds(List<Integer> sysUserIds) {
        LambdaQueryWrapper<UserMapping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserMapping::getSysUserId, sysUserIds);
        return userMappingMapper.selectList(queryWrapper);
    }

    /**
     * deleteUserMappings
     *
     * @param userMappings List<UserMapping>
     */
    @Override
    public void deleteUserMappings(List<UserMapping> userMappings) {
        if (!userMappings.isEmpty()) {
            List<Integer> userMappingIds = userMappings.stream()
                    .map(UserMapping::getId).collect(Collectors.toList());
            userMappingMapper.deleteBatchIds(userMappingIds);
        }
    }

    /**
     * deleteUserMappingBySsoUserIds
     *
     * @param ssoUserIds ssoUserIds
     * @return List<UserMapping>
     */
    @Override
    public List<UserMapping> deleteUserMappingBySsoUserIds(List<Integer> ssoUserIds) {
        List<UserMapping> userMappings = getListBySsoUserIds(ssoUserIds);
        deleteUserMappings(userMappings);
        return userMappings;
    }

    /**
     * deleteUserMappingBySysUserIds
     *
     * @param sysUserIds sysUserIds
     * @return List<UserMapping>
     */
    @Override
    public List<UserMapping> deleteUserMappingBySysUserIds(List<Integer> sysUserIds) {
        List<UserMapping> userMappings = getListBySysUserIds(sysUserIds);
        deleteUserMappings(userMappings);
        return userMappings;
    }


}
