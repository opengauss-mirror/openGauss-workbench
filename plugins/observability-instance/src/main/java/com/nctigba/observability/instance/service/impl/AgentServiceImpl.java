/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  AgentServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/impl/AgentServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import cn.hutool.http.HttpUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.AgentService;
import com.nctigba.observability.instance.service.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Agent Service implements
 *
 * @since 2023-11-24
 */
@Service
@Slf4j
public class AgentServiceImpl implements AgentService {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private PrometheusService prometheusService;

    @Override
    public Boolean isAgentAlive(String ip, String port) {
        String result = HttpUtil.get("http://" + ip + ":" + port + "/config/list");
        if (result == null) {
            return false;
        } else {
            return true;
        }
    }
}
