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

package org.opengauss.agent.service.task.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.opengauss.agent.client.HostFixedMetricsClient;
import org.opengauss.agent.client.ServerClientFactory;
import org.opengauss.agent.entity.HostBaseInfo;
import org.opengauss.agent.entity.TaskDefinition;
import org.opengauss.agent.exception.TaskExecutionException;
import org.opengauss.agent.service.task.TaskExecutor;

/**
 * HostStaticExecutor
 *
 * @author: wangchao
 * @Date: 2025/4/9 10:35
 * @Description: HostStaticCollector
 * @since 7.0.0-RC2
 **/
@ApplicationScoped
public class HostStaticExecutor implements TaskExecutor {
    HostFixedMetricsClient hostFixedMetricsClient;
    @Inject
    ServerClientFactory clientFactory;

    @Override
    public void initialize(TaskDefinition taskDefinition) throws TaskExecutionException {
        hostFixedMetricsClient = clientFactory.createHostFixedMetricsClient();
    }

    @Override
    public void execute() throws TaskExecutionException {
        HostBaseInfo hostBaseInfo = OshiServerCollector.fixedCollect();
        hostFixedMetricsClient.sendHostBaseInfo("", hostBaseInfo);
    }

    @Override
    public void cancel(Long taskId) {
    }

    @Override
    public boolean hasTaskRunning() {
        return false;
    }
}
