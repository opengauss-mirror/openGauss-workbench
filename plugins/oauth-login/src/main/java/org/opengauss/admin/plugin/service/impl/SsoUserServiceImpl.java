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
 * SsoUserServiceImpl.java
 *
 * IDENTIFICATION
 * oauth-login/src/main/java/org/opengauss/admin/plugin/service/impl/SsoUserServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.plugin.domain.entity.SsoUser;
import org.opengauss.admin.plugin.mapper.SsoUserMapping;
import org.opengauss.admin.plugin.service.SsoUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @date 2024/6/7 11:40
 * @since 0.0
 */
@Service
public class SsoUserServiceImpl extends ServiceImpl<SsoUserMapping, SsoUser> implements SsoUserService {
    @Autowired
    private SsoUserMapping ssoUserMapping;

    /**
     * getOneByUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return SsoUser
     */
    @Override
    public SsoUser getOneByUiidAndSsoServerUrl(SsoUser ssoUser) {
        return getOneByUiidAndSsoServerUrl(ssoUser.getUiid(), ssoUser.getSsoServerUrl());
    }

    /**
     * getOneByUiidAndSsoServerUrl
     *
     * @param uuid uuid
     * @param ssoServerUrl ssoServerUrl
     * @return SsoUser
     */
    @Override
    public SsoUser getOneByUiidAndSsoServerUrl(String uuid, String ssoServerUrl) {
        LambdaQueryWrapper<SsoUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SsoUser::getUiid, uuid)
                .eq(SsoUser::getSsoServerUrl, ssoServerUrl);
        return ssoUserMapping.selectOne(queryWrapper);
    }

    /**
     * getListByUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    @Override
    public List<SsoUser> getListByUiidAndSsoServerUrl(SsoUser ssoUser) {
        LambdaQueryWrapper<SsoUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SsoUser::getUiid, ssoUser.getUiid())
                .eq(SsoUser::getSsoServerUrl, ssoUser.getSsoServerUrl());
        return ssoUserMapping.selectList(queryWrapper);
    }

    /**
     * getListByUserNameAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    @Override
    public List<SsoUser> getListByUserNameAndSsoServerUrl(SsoUser ssoUser) {
        LambdaQueryWrapper<SsoUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SsoUser::getName, ssoUser.getName())
                .eq(SsoUser::getSsoServerUrl, ssoUser.getSsoServerUrl());
        return ssoUserMapping.selectList(queryWrapper);
    }

    /**
     * deleteSsoUsers
     *
     * @param ssoUsers List<SsoUser>
     */
    @Override
    public void deleteSsoUsers(List<SsoUser> ssoUsers) {
        if (!ssoUsers.isEmpty()) {
            List<Integer> ssoUserIds = ssoUsers.stream()
                    .map(SsoUser::getId).collect(Collectors.toList());
            ssoUserMapping.deleteBatchIds(ssoUserIds);
        }
    }

    /**
     * deleteSsoUserBySsoUserUiidAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    @Override
    public List<SsoUser> deleteSsoUserBySsoUserUiidAndSsoServerUrl(SsoUser ssoUser) {
        List<SsoUser> ssoUsers = getListByUiidAndSsoServerUrl(ssoUser);
        deleteSsoUsers(ssoUsers);
        return ssoUsers;
    }

    /**
     * deleteSsoUserByUserNameAndSsoServerUrl
     *
     * @param ssoUser ssoUser
     * @return List<SsoUser>
     */
    @Override
    public List<SsoUser> deleteSsoUserByUserNameAndSsoServerUrl(SsoUser ssoUser) {
        List<SsoUser> ssoUsers = getListByUserNameAndSsoServerUrl(ssoUser);
        deleteSsoUsers(ssoUsers);
        return ssoUsers;
    }
}
