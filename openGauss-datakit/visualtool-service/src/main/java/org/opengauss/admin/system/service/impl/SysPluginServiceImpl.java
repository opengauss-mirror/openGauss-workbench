/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SysPluginServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/SysPluginServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


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

import java.util.List;

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

    @Override
    public List<String> getPluginList() {
        return sysPluginMapper.getPluginIds();
    }
}
