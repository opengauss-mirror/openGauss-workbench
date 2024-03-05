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
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/controller/AgentController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.controller;

import com.nctigba.observability.log.service.AgentService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AgentController
 *
 * @author luomeng
 * @since 2024/1/5
 */
@RestController
@RequestMapping("/logSearch/v1")
public class AgentController {
    @Autowired
    @Qualifier("ElasticsearchService")
    private AgentService elasticService;

    @Autowired
    @Qualifier("FilebeatService")
    private AgentService filebeatService;

    /**
     * Start elasticsearch
     *
     * @param id Unique ID
     * @return AjaxResult
     */
    @PostMapping("/elastic/start")
    public AjaxResult startElastic(String id) {
        elasticService.start(id);
        return AjaxResult.success();
    }

    /**
     * stop elasticsearch
     *
     * @param id Unique ID
     * @return AjaxResult
     */
    @PostMapping("/elastic/stop")
    public AjaxResult stopElastic(String id) {
        elasticService.stop(id);
        return AjaxResult.success();
    }

    /**
     * Query elasticsearch status
     *
     * @return AjaxResult
     */
    @GetMapping("/elastic/status")
    public AjaxResult getElasticStatus() {
        return AjaxResult.success(elasticService.getAgentStatus());
    }

    /**
     * Start filebeat
     *
     * @param id Unique ID
     * @return AjaxResult
     */
    @PostMapping("/filebeat/start")
    public AjaxResult startFilebeat(String id) {
        filebeatService.start(id);
        return AjaxResult.success();
    }

    /**
     * stop filebeat
     *
     * @param id Unique ID
     * @return AjaxResult
     */
    @PostMapping("/filebeat/stop")
    public AjaxResult stopFilebeat(String id) {
        filebeatService.stop(id);
        return AjaxResult.success();
    }

    /**
     * Query filebeat status
     *
     * @return AjaxResult
     */
    @GetMapping("/filebeat/status")
    public AjaxResult getFilebeatStatus() {
        return AjaxResult.success(filebeatService.getAgentStatus());
    }
}
