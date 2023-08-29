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
 * SysPluginLogoServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysPluginLogoServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.system.domain.SysPluginLogo;
import org.opengauss.admin.system.mapper.SysPluginLogoMapper;
import org.opengauss.admin.system.service.ISysPluginLogoService;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 */
@Service
public class SysPluginLogoServiceImpl extends ServiceImpl<SysPluginLogoMapper, SysPluginLogo> implements ISysPluginLogoService {


    @Override
    public void savePluginConfig(String pluginId, String logoPath) {
        LambdaQueryWrapper<SysPluginLogo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPluginLogo::getPluginId, pluginId);
        SysPluginLogo one = this.getOne(queryWrapper);
        if (one == null) {
            one = new SysPluginLogo();
            one.setPluginId(pluginId);
        }
        one.setLogoPath(logoPath);
        this.saveOrUpdate(one);
    }

    @Override
    public SysPluginLogo getByPluginId(String pluginId) {
        LambdaQueryWrapper<SysPluginLogo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPluginLogo::getPluginId, pluginId).last("limit 1");
        SysPluginLogo one = this.getOne(queryWrapper);
        return one;
    }
}
