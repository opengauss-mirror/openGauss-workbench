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

import java.util.Map;

/**
 * IAgentServerService
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:30
 * @Description: IAgentInstallService
 * @since 7.0.0-RC2
 **/
public interface IAgentServerService {
    /**
     * <pre>
     * agent anomaly checking , check items:
     * 1. check agent server_install_record
     * 2. check agent server_status
     * 3. check agent java_version
     * 4. check agent agent_port
     * 5. check agent install_path
     * 6. check agent install_path is empty
     * 7. check agent install_path is writeable
     * 8. check agent agent_status
     * </pre>
     *
     * @param agentId agentId
     * @return result
     */
    Map<String, String> anomalyChecking(String agentId);

    /**
     * task callback start
     *
     * @param agentId agentId
     */
    void taskCallbackStarted(String agentId);

    /**
     * start task by taskId, send task to agent proxy
     *
     * @param id taskId
     */
    void startAgentTaskByTaskId(Long id);

    /**
     * <pre>
     *     check agent install and online
     * </pre>
     *
     * @param hostId hostId
     * @return true: install and online, false: not install or not online
     */
    boolean checkClusterNodeAgentInstallAndOnline(String hostId);
}
