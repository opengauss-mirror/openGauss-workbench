package org.opengauss.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.opengauss.admin.system.domain.SysPluginLogo;
import org.opengauss.admin.system.mapper.SysPluginLogoMapper;
import org.opengauss.admin.system.service.ISysPluginLogoService;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 */
@Service
public class SysPluginLogoServiceImpl extends ServiceImpl<SysPluginLogoMapper, SysPluginLogo> implements ISysPluginLogoService {


    @Override
    public void savePluginConfig(String pluginId, String logoPath) {
        LambdaQueryWrapper<SysPluginLogo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPluginLogo::getPluginId, pluginId);
        SysPluginLogo one = this.getOne(queryWrapper);
        if (one == null) {
            one = new SysPluginLogo();
            one.setPluginId(pluginId);
            one.setLogoPath(logoPath);
        }
        this.saveOrUpdate(one);
    }

    @Override
    public SysPluginLogo getByPluginId(String pluginId) {
        LambdaQueryWrapper<SysPluginLogo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPluginLogo::getPluginId, pluginId).last("limit 1");
        SysPluginLogo one = this.getOne(queryWrapper);
        return one;
    }
}
