/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.SysSettingExtEntity;
import org.opengauss.admin.system.mapper.SysSettingExtMapper;
import org.opengauss.admin.system.service.ISysSettingExtService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * SysSettingExtServiceImpl
 *
 * @author: wangchao
 * @Date: 2025/9/8 15:48
 * @since 7.0.0-RC2
 **/
@Service
public class SysSettingExtServiceImpl extends ServiceImpl<SysSettingExtMapper, SysSettingExtEntity>
    implements ISysSettingExtService {
    @Override
    public List<SysSettingExtEntity> queryCurrentUserExtParam(Integer userId) {
        LambdaQueryWrapper<SysSettingExtEntity> query = Wrappers.lambdaQuery(SysSettingExtEntity.class);
        query.eq(SysSettingExtEntity::getUserId, userId);
        return list(query);
    }

    @Override
    public void updateExtSettingDataKitServerHost(SysSettingEntity setting) {
        LambdaUpdateWrapper<SysSettingExtEntity> updateWrapper = Wrappers.lambdaUpdate(SysSettingExtEntity.class);
        updateWrapper.eq(SysSettingExtEntity::getId, setting.getId())
            .eq(SysSettingExtEntity::getUserId, setting.getUserId())
            .eq(SysSettingExtEntity::getParamName, CommonConstants.SETTING_SERVER_HOST_NAME)
            .set(SysSettingExtEntity::getParamValue, setting.getServerHost());
        update(updateWrapper);
    }

    @Override
    public String queryAdminServerHost() {
        LambdaQueryWrapper<SysSettingExtEntity> queryWrapper = Wrappers.lambdaQuery(SysSettingExtEntity.class);
        queryWrapper.eq(SysSettingExtEntity::getParamName, CommonConstants.SETTING_SERVER_HOST_NAME);
        queryWrapper.eq(SysSettingExtEntity::getUserId, CommonConstants.ADMIN_USER_ID);
        SysSettingExtEntity sysExtParam = getOne(queryWrapper);
        return Objects.isNull(sysExtParam) ? "" : sysExtParam.getParamValue();
    }
}
