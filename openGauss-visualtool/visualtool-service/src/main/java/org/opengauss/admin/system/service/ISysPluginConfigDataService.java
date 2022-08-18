package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPluginConfigData;

/**
 * @author xielibo
 */
public interface ISysPluginConfigDataService extends IService<SysPluginConfigData> {

    public void savePluginConfigData(String pluginId, String data);

    String getDataByPluginId(String pluginId);
}
