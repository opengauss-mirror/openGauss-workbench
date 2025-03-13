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
 * ISysPluginService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysPluginService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.system.domain.SysPlugin;

import java.util.List;

/**
 * @author xielibo
 */
public interface ISysPluginService extends IService<SysPlugin> {
    IPage<SysPlugin> selectList(IPage<SysPlugin> page, SysPlugin sysPlugin);

    void stopByPluginId(String pluginId);

    void startByPluginId(String pluginId);

    void uninstallPluginByPluginId(String pluginId);

    SysPlugin getByPluginId(String pluginId);

    /**
     * getPluginIds
     *
     * @return pluginIds
     */
    List<String> getPluginList();
}
