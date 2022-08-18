package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPlugin;

/**
 * @author xielibo
 */
public interface ISysPluginService extends IService<SysPlugin> {
    IPage<SysPlugin> selectList(IPage<SysPlugin> page, SysPlugin sysPlugin);

    void stopByPluginId(String pluginId);

    void startByPluginId(String pluginId);

    void uninstallPluginByPluginId(String pluginId);

    SysPlugin getByPluginId(String pluginId);
}
