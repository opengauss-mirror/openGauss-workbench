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

package org.opengauss.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentStartResult;
import org.opengauss.admin.common.enums.agent.AgentStatus;

import java.util.List;
import java.util.Map;

/**
 * IAgentInstallService
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:30
 * @Description: IAgentInstallService
 * @since 7.0.0-RC2
 **/
public interface IAgentInstallService extends IService<AgentInstallEntity> {
    /**
     * get agent by agentId
     *
     * @param agentId agentId
     * @return AgentInstallEntity
     */
    AgentInstallEntity getByAgentId(String agentId);

    /**
     * get agent by userId ,used for Host User delete checked
     *
     * @param userId userId
     * @return List<AgentInstallEntity>
     */
    List<AgentInstallEntity> listByUserId(String userId);

    /**
     * delete agent by agentId
     *
     * @param agentId agentId
     */
    void removeByAgentId(String agentId);

    /**
     * install agent
     *
     * @param agent agent
     */
    void installAgent(AgentInstallEntity agent);

    /**
     * uninstall agent
     *
     * @param agentId agentId
     */
    void uninstallAgent(String agentId);

    /**
     * start agent
     *
     * @param agentId agentId
     * @return agent start result
     */
    AgentStartResult startAgent(String agentId);

    /**
     * update agent port
     *
     * @param agentId agentId
     * @param port port
     */
    void updateAgentPort(String agentId, Integer port);

    /**
     * stop agent
     *
     * @param agentId agentId
     */
    void stopAgent(String agentId);

    /**
     * update agent
     *
     * @param agentId agentId
     */
    void updateAgent(String agentId);

    /**
     * refresh agent status
     *
     * @param agentId agent id
     * @param status agnet status
     */
    void refreshAgentStatus(String agentId, AgentStatus status);

    /**
     * start all of agents
     *
     * @return List<AgentInstallEntity>
     */
    List<AgentInstallEntity> startAllOfAgent();

    /**
     * query host agent status
     *
     * @param hostId hostId
     * @return String
     */
    AgentStatus getAgentStatus(String hostId);

    /**
     * query agent status
     *
     * @return Map<Long, String>
     */
    Map<String, AgentStatus> queryAgentStatus();

    /**
     * update agent status
     *
     * @param agentId agentId
     * @param status status
     */
    void updateAgentStatus(String agentId, AgentStatus status);

    /**
     * query agent install info
     *
     * @return Map<String, AgentInstallEntity>
     */
    Map<String, AgentInstallEntity> queryAgentInstallInfo();

    /**
     * check agent install info
     *
     * @param hostId hostId
     * @return boolean
     */
    boolean hasInstall(String hostId);

    /**
     * count agent install info by hostUserId
     *
     * @param hostUserId hostUserId
     * @return int
     */
    long countByHostUserId(String hostUserId);
}
