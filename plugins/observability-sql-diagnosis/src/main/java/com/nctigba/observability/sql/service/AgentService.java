/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.service;

import com.nctigba.observability.sql.model.vo.AgentStatusVO;

import java.util.List;

/**
 * AgentService
 *
 * @author luomeng
 * @since 2024/1/2
 */
public interface AgentService {
    /**
     * Start agent
     *
     * @param id      Unique ID
     * @param rootPwd Root password
     */
    void start(String id, String rootPwd);

    /**
     * Stop agent
     *
     * @param id      Unique ID
     * @param rootPwd Root password
     */
    void stop(String id, String rootPwd);

    /**
     * Query agent status
     *
     * @return List
     */
    List<AgentStatusVO> getAgentStatus();

    /**
     * Get install user
     *
     * @return AjaxResult
     */
    String getInstallUser(String nodeId);
}
