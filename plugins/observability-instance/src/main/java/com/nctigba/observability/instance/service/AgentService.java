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

package com.nctigba.observability.instance.service;

/**
 * Agent Service
 *
 * @since 2023-11-24
 */
public interface AgentService {
    /**
     * Check is agent alive
     *
     * @param ip Agent ip
     * @param port Agent port
     * @return Is alive
     */
    Boolean isAgentAlive(String ip, String port);

    /**
     * Check is agent exist
     *
     * @param clusterNodeId String
     * @return Integer
     */
    Integer isExistAgentForInstance(String clusterNodeId);
}
