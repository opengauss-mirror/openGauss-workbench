package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.system.domain.SysPluginConfigData;
import org.opengauss.admin.system.mapper.SysPluginConfigDataMapper;
import org.opengauss.admin.system.service.ISysPluginConfigDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xielibo
 */
@Service
public class SysPluginConfigDataServiceImpl extends ServiceImpl<SysPluginConfigDataMapper, SysPluginConfigData> implements ISysPluginConfigDataService {

    @Autowired
    private SysPluginConfigDataMapper sysPluginConfigDataMapper;

    @Override
    public void savePluginConfigData(String pluginId, String data) {
        SysPluginConfigData configData = getByPluginId(pluginId);
        if (configData == null) {
            configData = new SysPluginConfigData();
        }
        configData.setConfigData(data);
        configData.setPluginId(pluginId);
        this.saveOrUpdate(configData);
    }


    public SysPluginConfigData getByPluginId(String pluginId) {
        LambdaQueryWrapper<SysPluginConfigData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPluginConfigData::getPluginId, pluginId);
        List<SysPluginConfigData> list = sysPluginConfigDataMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public String getDataByPluginId(String pluginId) {
        SysPluginConfigData configData = getByPluginId(pluginId);
        if (configData == null) {
            return null;
        }
        return configData.getConfigData();
    }

}
