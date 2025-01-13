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
 * ISysSettingService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ISysSettingService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;

import java.net.Proxy;

/**
 * System Setting Interface
 *
 * @author wangyl
 */
public interface ISysSettingService extends IService<SysSettingEntity> {
    boolean updateSetting(SysSettingEntity setting);

    SysSettingEntity getSetting(Integer userId);

    /**
     * init http proxy
     */
    void initHttpProxy();

    /**
     * create http proxy by system config: server.proxy.hostname server.proxy.port
     *
     * @return proxy
     */
    Proxy getSysNetProxy();

    boolean hasUploadPath(String path, Integer userId);
}
