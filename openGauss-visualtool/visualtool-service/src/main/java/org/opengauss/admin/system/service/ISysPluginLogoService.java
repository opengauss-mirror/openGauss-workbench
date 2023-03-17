package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPluginLogo;

/**
 * @author xielibo
 */
public interface ISysPluginLogoService extends IService<SysPluginLogo> {

    public void savePluginConfig(String pluginId, String logoPath);

    SysPluginLogo getByPluginId(String pluginId);
}
