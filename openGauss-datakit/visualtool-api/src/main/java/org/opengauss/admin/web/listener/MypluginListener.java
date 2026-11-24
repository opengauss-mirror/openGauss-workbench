/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.web.listener;

import com.gitee.starblues.core.PluginInfo;
import com.gitee.starblues.integration.listener.PluginListener;
import com.gitee.starblues.integration.operator.PluginOperator;
import org.opengauss.admin.system.service.ISysPluginService;
import org.opengauss.admin.web.controller.SystemPluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;


/**
 * Plugin Listener
 */
@Component
public class MypluginListener implements PluginListener {
    private static final Logger log = LoggerFactory.getLogger(MypluginListener.class);

    @Autowired
    private ISysPluginService sysPluginService;
    
    @Autowired
    private SystemPluginController systemPluginController;
    
    @Autowired
    private PluginOperator pluginOperator;

    @Override
    public void loadSuccess(PluginInfo pluginInfo) {
        log.info("plugin [{}] load success.", pluginInfo.getPluginId());
    }

    @Override
    public void loadFailure(Path path, Throwable throwable) {
        log.error("plugin[{}] load fail. {}", path, throwable);
    }

    @Override
    public void unLoadSuccess(PluginInfo pluginInfo) {
        log.info("plugin[{}] uninstall success.", pluginInfo.getPluginId());
        sysPluginService.uninstallPluginByPluginId(pluginInfo.getPluginId());
    }

    @Override
    public void unLoadFailure(PluginInfo pluginInfo, Throwable throwable) {
        log.error("plugin[{}] uninstall fail. {}", pluginInfo.getPluginId(), throwable);
    }

    @Override
    public void startSuccess(PluginInfo pluginInfo) {
        log.info("plugin[{}] start success", pluginInfo.getPluginId());
        if (pluginInfo.getExtensionInfo() == null || pluginInfo.getExtensionInfo().isEmpty()) {
            pluginInfo = pluginOperator.getPluginInfo(pluginInfo.getPluginId());
        }
        Map<String, Object> result = systemPluginController.updateSystemByPluginInfo(pluginInfo);
        log.info("plugin[{}] update logo {}.", pluginInfo.getPluginId(), result == null ? "failed" : "success");
    }

    @Override
    public void startFailure(PluginInfo pluginInfo, Throwable throwable) {
        log.error("plugin[{}] start fail. {}", pluginInfo.getPluginId(), throwable);

        pluginOperator.unload(pluginInfo.getPluginId());
    }

    @Override
    public void stopSuccess(PluginInfo pluginInfo) {
        log.info("plugin[{}] stop success.", pluginInfo.getPluginId());
    }

    @Override
    public void stopFailure(PluginInfo pluginInfo, Throwable throwable) {
        log.error("plugin[{}] stop fail. {}", pluginInfo.getPluginId(), throwable);
    }
}
