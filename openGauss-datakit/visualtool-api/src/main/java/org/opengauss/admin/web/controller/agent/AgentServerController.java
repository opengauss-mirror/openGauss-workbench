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

package org.opengauss.admin.web.controller.agent;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.agent.HeartbeatReport;
import org.opengauss.agent.service.IAgentHeartbeatService;
import org.opengauss.agent.service.IAgentServerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * AgentServerController
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:27
 * @Description: AgentInstallController
 * @since 7.0.0-RC2
 **/
@RestController
@RequestMapping("/agent")
public class AgentServerController {
    @Resource
    private IAgentHeartbeatService agentHeartbeatService;
    @Resource
    private IAgentServerService agentServerService;

    /**
     * receive heartbeat
     *
     * @param heart heart
     * @return result
     */
    @PostMapping("/heartbeat")
    public AjaxResult heartbeat(@RequestBody HeartbeatReport heart) {
        agentHeartbeatService.receiveHeartbeat(heart);
        return AjaxResult.success();
    }

    /**
     * deregister
     *
     * @param requestDown requestDown
     * @return result
     */
    @PostMapping("/deregister")
    public AjaxResult deregister(@RequestBody HeartbeatReport requestDown) {
        agentHeartbeatService.deregister(requestDown);
        return AjaxResult.success();
    }

    /**
     * anomaly checking
     *
     * @param agentId agentId
     * @return result
     */
    @PostMapping("/server/anomaly/checking")
    public AjaxResult anomalyChecking(@RequestParam("agentId") String agentId) {
        return AjaxResult.success(agentServerService.anomalyChecking(agentId));
    }

    /**
     * start task callback
     *
     * @param agentId agentId
     * @return result
     */
    @PostMapping("/task/callback/start")
    public AjaxResult taskCallbackStarted(@RequestParam("agentId") String agentId) {
        agentServerService.taskCallbackStarted(agentId);
        return AjaxResult.success();
    }
}
