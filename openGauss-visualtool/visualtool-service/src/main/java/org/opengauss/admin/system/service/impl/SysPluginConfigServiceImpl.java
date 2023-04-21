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
 * SysPluginConfigServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysPluginConfigServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.domain.SysPluginConfig;
import org.opengauss.admin.system.mapper.SysPluginConfigMapper;
import org.opengauss.admin.system.service.ISysPluginConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 */
@Service
public class SysPluginConfigServiceImpl extends ServiceImpl<SysPluginConfigMapper, SysPluginConfig> implements ISysPluginConfigService {

    @Autowired
    private SysPluginConfigMapper sysPluginConfigMapper;

    @Override
    public void savePluginConfig(String pluginId, String configJson) {
        SysPluginConfig config = new SysPluginConfig();
        config.setConfigJson(configJson);
        config.setPluginId(pluginId);
        sysPluginConfigMapper.insert(config);
    }

    @Override
    public void deleteByPluginId(String pluginId) {
        if (StringUtils.isNotNull(pluginId)) {
            LambdaQueryWrapper<SysPluginConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysPluginConfig::getPluginId, pluginId);
            sysPluginConfigMapper.delete(queryWrapper);
        }
    }
}
