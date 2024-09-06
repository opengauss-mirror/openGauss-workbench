/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * OpenGaussOperatorTask.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/task/OpenGaussOperatorTask.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.task;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.container.service.OpenGaussOperatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ops集群的operator更新任务
 *
 * @since 2024-06-19
 */
@Slf4j
@Component
public class OpenGaussOperatorTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGaussOperatorTask.class);

    @Autowired
    ScheduleTaskLock scheduleTaskLock;
    @Autowired
    OpenGaussOperatorService openGaussOperatorService;

    @Value("${myProps.lock.opengaussOperatorRefresh:OpengaussOperatorRefreshLockKey}")
    private String opengaussOperatorRegisterLock;

    /**
     * 每日5:50定时更新operator
     */
    @Async
    @Scheduled(cron = "0 50 5 * * ?")
    public void refreshOpengaussOperator() {
        boolean canLock = scheduleTaskLock.tryGeneralTaskLock(opengaussOperatorRegisterLock);
        if (!canLock) {
            return;
        }
        LOGGER.info("---Start executing the operator update task for the ops cluster---");
        long start = System.currentTimeMillis();
        openGaussOperatorService.syncOperatorFromK8s();
        long end = System.currentTimeMillis();
        LOGGER.info("---The operator update task for the ops cluster has ended,use ({})s---",
                (end - start) / 1000);
    }
}
