package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPluginConfig;

/**
 * @author xielibo
 */
public interface ISysPluginConfigService extends IService<SysPluginConfig> {

    public void savePluginConfig(String pluginId, String configJson);

    void deleteByPluginId(String pluginId);
}
