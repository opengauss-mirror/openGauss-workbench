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

package org.opengauss.admin.web.core.config;

import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.system.service.HostMonitorCacheService;
import org.opengauss.agent.service.AgentTaskManager;
import org.springframework.stereotype.Component;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * HostMonitorCacheInitialize
 *
 * @author: wangchao
 * @Date: 2025/8/1 16:40
 * @since 7.0.0-RC2
 **/
@Slf4j
@Component
public class HostMonitorCacheInitialize {
    @Resource
    private HostMonitorCacheService hostMonitorCacheService;
    @Resource
    private AgentTaskManager agentTaskManager;

    /**
     * initialize host monitor cache
     */
    @PostConstruct
    public void initialize() {
        List<TaskMetricsDefinition> metricsDefinitionList = agentTaskManager.queryHostMetricsDefinition();
        hostMonitorCacheService.initHostMonitorCacheEnvironment(metricsDefinitionList);
        hostMonitorCacheService.startHostMonitorScheduled();
        log.info("host monitor cache initialize success");
    }
}
