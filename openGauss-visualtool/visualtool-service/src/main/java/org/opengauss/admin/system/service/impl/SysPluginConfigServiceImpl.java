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
