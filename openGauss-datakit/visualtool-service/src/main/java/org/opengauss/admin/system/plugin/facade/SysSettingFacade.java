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
 * SysSettingFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/SysSettingFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.system.service.ISysSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Proxy;

/**
 * @author wangyl
 * @date 2023/3/8 09:22
 **/
@Service
public class SysSettingFacade {
    @Autowired
    private ISysSettingService sysSettingService;

    public SysSettingEntity getSysSetting(Integer userId) {
        return sysSettingService.getSetting(userId);
    }

    /**
     * create http proxy by system config: server.proxy.hostname server.proxy.port
     *
     * @return proxy
     */
    public Proxy getSysNetProxy() {
        return sysSettingService.getSysNetProxy();
    }

    public boolean checkUploadPath(String path, Integer userId) {
        return sysSettingService.hasUploadPath(path, userId);
    }
}
