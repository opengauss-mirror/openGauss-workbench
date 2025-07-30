/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * AdminApplicationRunner.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/core/config/AdminApplicationRunner.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.core.config;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.agent.TaskMetricsDefinition;
import org.opengauss.admin.system.service.HostMonitorCacheService;
import org.opengauss.admin.system.service.ISysSettingService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.agent.service.AgentTaskManager;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Resource;

/**
 * @className: AdminApplicationRunner
 * @description: AdminApplicationRunner
 * @author: xielibo
 * @date: 2022-12-17 15:43
 **/
@Slf4j
@Component
public class AdminApplicationRunner implements ApplicationRunner {
    @Autowired
    private EncryptionUtils encryptionUtils;
    @Resource
    private IAgentInstallService agentInstallService;
    @Resource
    private HostMonitorCacheService hostMonitorCacheService;
    @Resource
    private AgentTaskManager agentTaskManager;
    @Resource
    private ISysSettingService sysSettingService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.encryptionUtils.updateKeyPairSecret();
        this.encryptionUtils.refreshKeyPair(false);
        sysSettingService.initHttpProxy();
        List<TaskMetricsDefinition> metricsDefinitionList = agentTaskManager.queryHostMetricsDefinition();
        hostMonitorCacheService.initHostMonitorCacheEnvironment(metricsDefinitionList);
        threadPoolTaskExecutor.submit(() -> {
            hostMonitorCacheService.startHostMonitorScheduled();
            agentInstallService.startAllOfAgent();
            log.info("start to activate all of agents and their tasks");
        });
    }
}
