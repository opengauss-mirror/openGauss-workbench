/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.impl;

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * AgentSshLoginService
 *
 * @author: wangchao
 * @Date: 2025/4/12 17:54
 * @Description: AgentLoginService
 * @since 7.0.0-RC2
 **/
@Service
public class AgentSshLoginService {
    private static final String AGENT_INVALID_MSG = "host user or username can not be exist [host:%s user:%s]";

    @Resource
    private EncryptionUtils encryptionUtils;
    @Resource
    private IHostService hostService;
    @Resource
    private IHostUserService hostUserService;

    /**
     * create agent ssh login
     *
     * @param agentInstall agentInstall
     * @return SshLogin
     */
    public SshLogin getSshLogin(AgentInstallEntity agentInstall) {
        OpsAssert.nonNull(agentInstall, "agent can not be null");
        String agentId = agentInstall.getAgentId();
        OpsHostEntity host = hostService.getById(agentId);
        OpsAssert.nonNull(host, "host or agentId can not be exist [" + agentId + "]");
        String installUser = agentInstall.getInstallUser();
        String hostId = String.valueOf(agentInstall.getAgentId());
        OpsHostUserEntity user = hostUserService.getHostUserByUsername(hostId, installUser);
        OpsAssert.nonNull(user, String.format(AGENT_INVALID_MSG, hostId, installUser));
        return new SshLogin(host.getPublicIp(), host.getPort(), user.getUsername(),
            encryptionUtils.decrypt(user.getPassword()));
    }
}
