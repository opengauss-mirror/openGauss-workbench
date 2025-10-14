/**
 Copyright  starBlues.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
