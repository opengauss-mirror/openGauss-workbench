package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.service.ISysPluginConfigDataService;
import org.opengauss.admin.system.service.ISysPluginConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @className: PluginFacade
 * @description: Plugin service provided to plugins.
 * @author: xielibo
 * @date: 2022-09-05 6:59 PM
 **/
@Service
public class PluginFacade {

    @Autowired
    private ISysPluginConfigService sysPluginConfigService;
    @Autowired
    private ISysPluginConfigDataService sysPluginConfigDataService;

    /**
     * Get plugin configuration data
     * @param pluginId
     * @return
     */
    public String getPluginConfigData(String pluginId){
        if (StringUtils.isNotNull(pluginId)) {
            return sysPluginConfigDataService.getDataByPluginId(pluginId);
        }
        return null;
    }
}
