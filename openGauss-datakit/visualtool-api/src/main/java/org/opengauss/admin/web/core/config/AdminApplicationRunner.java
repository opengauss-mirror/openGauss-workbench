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
 * AdminApplicationRunner.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/core/config/AdminApplicationRunner.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.core.config;

import com.gitee.starblues.core.PluginInfo;
import com.gitee.starblues.integration.operator.PluginOperator;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.system.service.ISysPluginService;
import org.opengauss.admin.system.service.ISysSettingService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.List;

/**
 * @className: AdminApplicationRunner
 * @description: AdminApplicationRunner
 * @author: xielibo
 * @date: 2022-12-17 15:43
 **/
@Slf4j
@Component
public class AdminApplicationRunner implements ApplicationRunner {
    @Resource
    private EncryptionUtils encryptionUtils;
    @Resource
    private IAgentInstallService agentInstallService;
    @Resource
    private ISysSettingService sysSettingService;
    @Autowired
    private PluginOperator pluginOperator;
    @Autowired
    private ISysPluginService sysPluginService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        clearDirtyDataOfPlugins();
        this.encryptionUtils.updateKeyPairSecret();
        this.encryptionUtils.refreshKeyPair(false);
        sysSettingService.initHttpProxy();
        agentInstallService.startAllOfAgent();
    }

    private void clearDirtyDataOfPlugins() {
        List<String> pluginList = sysPluginService.getPluginList();
        if (CollUtil.isEmpty(pluginList)) {
            return;
        }
        for (String pluginId : pluginList) {
            PluginInfo pluginInfo = pluginOperator.getPluginInfo(pluginId);
            if (pluginInfo == null) {
                sysPluginService.uninstallPluginByPluginId(pluginId);
            }
        }
    }
}
