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
 * DataMigrationApplicationRunner.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/config/DataMigrationApplicationRunner.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.config;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.service.MigrationMainTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @className: DataMigrationApplicationRunner
 * @description: DataMigrationApplicationRunner
 * @author: xielibo
 * @date: 2022-12-17 15:43
 **/
@Slf4j
@Component
public class DataMigrationApplicationRunner implements ApplicationRunner {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MigrationTaskService migrationTaskService;
    @Autowired
    private MigrationMainTaskService migrationMainTaskService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        threadPoolTaskExecutor.submit(() -> {
            try {
                migrationTaskService.doOfflineTaskRunScheduler();
            } catch (Exception e) {
                log.error("OffLineTaskRunScheduler error", e.getMessage());
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                migrationMainTaskService.doRefreshMainTaskStatus();
            } catch (Exception e) {
                log.error("RefreshMainTaskStatus error", e.getMessage());
            }
        });
    }
}
