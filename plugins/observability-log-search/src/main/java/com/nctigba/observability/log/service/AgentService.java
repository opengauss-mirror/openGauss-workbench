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
 *  AgentService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/service/AgentService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.service;

import com.nctigba.observability.log.model.vo.AgentStatusVO;

import java.util.List;

/**
 * AgentService
 *
 * @author luomeng
 * @since 2024/1/3
 */
public interface AgentService {
    /**
     * Start
     *
     * @param id Unique ID
     */
    void start(String id);

    /**
     * Stop
     *
     * @param id Unique ID
     */
    void stop(String id);

    /**
     * Get status
     *
     * @return List
     */
    List<AgentStatusVO> getAgentStatus();
}
