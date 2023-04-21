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
 * SysSettingServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysSettingServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.system.mapper.SysSettingMapper;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * System Setting Service
 *
 * @author wangyl
 */
@Service
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSettingEntity> implements ISysSettingService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSetting(SysSettingEntity setting) {
        // create folder if not exist
        String uploadPath = setting.getUploadPath();
        File file = new File(uploadPath);
        // if create failed
        if (!(!file.exists() && file.mkdirs())) {
            log.error(String.format("System setting update failed: folder %s create failed", uploadPath));
        }
        return saveOrUpdate(setting);
    }

    @Override
    public SysSettingEntity getSetting(Integer userId) {
        LambdaQueryWrapper<SysSettingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSettingEntity::getUserId, SysUser.getAdminUserId());
        SysSettingEntity adminResult = getOne(queryWrapper);
        if (SysUser.isAdmin(userId)) {
            return adminResult;
        }
        LambdaQueryWrapper<SysSettingEntity> userQueryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSettingEntity::getUserId, userId);
        SysSettingEntity userResult = getOne(userQueryWrapper);
        // if current user is not admin and there is no setting data
        // return admin setting to user for save
        if (ObjectUtil.isNull(userResult)) {
            adminResult.setUserId(userId);
            return adminResult;
        }
        return userResult;
    }

    @Override
    public boolean hasUploadPath(String path, Integer userId) {
        LambdaQueryWrapper<SysSettingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysSettingEntity::getUploadPath, path);
        List<SysSettingEntity> result = list(queryWrapper);
        if (CollUtil.isNotEmpty(result)) {
            for (SysSettingEntity entity: result) {
                if (!entity.getUserId().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
