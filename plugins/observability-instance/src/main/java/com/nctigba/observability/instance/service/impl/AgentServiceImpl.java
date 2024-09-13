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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.enums.AgentStatusEnum;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.service.AgentNodeRelationService;
import com.nctigba.observability.instance.service.AgentService;
import com.nctigba.observability.instance.service.PrometheusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Wrapper;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    @Autowired
    private AgentNodeRelationService relationService;

    @Override
    public Boolean isAgentAlive(String ip, String port) {
        String result = HttpUtil.get("http://" + ip + ":" + port + "/config/list", CommonConstants.HTTP_TIMEOUT);
        if (result == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Integer isExistAgentForInstance(String clusterNodeId) {
        List<AgentNodeRelationDO> relList = relationService.list(Wrappers.<AgentNodeRelationDO>lambdaQuery()
            .eq(AgentNodeRelationDO::getNodeId, clusterNodeId));
        if (CollectionUtil.isEmpty(relList)) {
            return 1;
        }
        NctigbaEnvDO agent = null;
        for (AgentNodeRelationDO agentNodeRel : relList) {
            agent  = envMapper.selectById(agentNodeRel.getEnvId());
            if (agent == null || StrUtil.isBlank(agent.getId())) {
                continue;
            }
            break;
        }
        if (agent == null || AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus().equalsIgnoreCase(agent.getStatus())
            || AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus().equalsIgnoreCase(agent.getStatus())) {
            return 2;
        }
        if (AgentStatusEnum.MANUAL_STOP.getStatus().equalsIgnoreCase(agent.getStatus())) {
            return 3;
        }
        return 0;
    }
}

