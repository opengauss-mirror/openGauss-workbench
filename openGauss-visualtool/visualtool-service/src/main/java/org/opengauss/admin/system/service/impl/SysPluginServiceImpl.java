package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.common.enums.SysPluginStatus;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.system.domain.SysPlugin;
import org.opengauss.admin.system.mapper.SysPluginMapper;
import org.opengauss.admin.system.service.ISysMenuService;
import org.opengauss.admin.system.service.ISysPluginConfigService;
import org.opengauss.admin.system.service.ISysPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 */
@Service
public class SysPluginServiceImpl extends ServiceImpl<SysPluginMapper, SysPlugin> implements ISysPluginService {

    @Autowired
    private SysPluginMapper sysPluginMapper;
    @Autowired
    private ISysPluginConfigService sysPluginConfigService;
    @Autowired
    private ISysMenuService sysMenuService;

    /**
     * Paging query plugin list
     *
     * @param sysPlugin
     * @return SysPlugin
     */
    @Override
    public IPage<SysPlugin> selectList(IPage<SysPlugin> page, SysPlugin sysPlugin) {
        return sysPluginMapper.selectSysPluginListPage(page, sysPlugin);
    }

    public void updateByPluginId(String pluginId,Integer pluginStatus) {
        LambdaQueryWrapper<SysPlugin> queryWrapper = new LambdaQueryWrapper<SysPlugin>();
        queryWrapper.eq(pluginId != null, SysPlugin::getPluginId, pluginId);
        SysPlugin update = new SysPlugin();
        update.setPluginStatus(pluginStatus);
        sysPluginMapper.update(update, queryWrapper);
    }

    /**
     * Stop a plugin by plugin ID
     * @param pluginId
     */
    @Override
    public void stopByPluginId(String pluginId) {
        this.updateByPluginId(pluginId, SysPluginStatus.DISABLE.getCode());
    }

    /**
     * Start the plugin according to the plugin ID
     * @param pluginId
     */
    @Override
    public void startByPluginId(String pluginId) {
        this.updateByPluginId(pluginId, SysPluginStatus.START.getCode());
    }

    /**
     * Delete plugins by plugin ID
     * @param pluginId
     */
    @Override
    public void uninstallPluginByPluginId(String pluginId) {
        if (StringUtils.isNotNull(pluginId)) {
            LambdaQueryWrapper<SysPlugin> queryWrapper = new LambdaQueryWrapper<SysPlugin>();
            queryWrapper.eq(pluginId != null, SysPlugin::getPluginId, pluginId);
            sysPluginMapper.delete(queryWrapper);
            sysPluginConfigService.deleteByPluginId(pluginId);
            sysMenuService.deleteByPluginId(pluginId);
        }
    }

    /**
     * get one by pluginId
     * @param pluginId
     * @return
     */
    @Override
    public SysPlugin getByPluginId(String pluginId) {
        LambdaQueryWrapper<SysPlugin> queryWrapper = new LambdaQueryWrapper<SysPlugin>();
        queryWrapper.eq(pluginId != null, SysPlugin::getPluginId, pluginId);
        return sysPluginMapper.selectOne(queryWrapper);
    }


}
