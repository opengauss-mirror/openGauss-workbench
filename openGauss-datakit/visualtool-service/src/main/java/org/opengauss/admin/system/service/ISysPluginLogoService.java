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
 * ISysPluginLogoService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysPluginLogoService.java
 *
 * -------------------------------------------------------------------------
 */


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
