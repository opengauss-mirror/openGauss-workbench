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
 *  AgentController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/AgentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.service.AgentService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AgentController
 *
 * @author luomeng
 * @since 2024/1/2
 */
@RestController
@RequestMapping("/observability/v1/agent")
public class AgentController {
    @Autowired
    private AgentService agentService;

    /**
     * Start agent
     *
     * @param id Unique ID
     * @param rootPwd Root password
     * @return AjaxResult
     */
    @PostMapping("/start")
    public AjaxResult start(String id, String rootPwd) {
        agentService.start(id, rootPwd);
        return AjaxResult.success();
    }

    /**
     * Stop agent
     *
     * @param id Unique ID
     * @param rootPwd Root password
     * @return AjaxResult
     */
    @PostMapping("/stop")
    public AjaxResult stop(String id, String rootPwd) {
        agentService.stop(id, rootPwd);
        return AjaxResult.success();
    }

    /**
     * Query agent status
     *
     * @return AjaxResult
     */
    @GetMapping("/status")
    public AjaxResult getAgentStatus() {
        return AjaxResult.success(agentService.getAgentStatus());
    }
}
