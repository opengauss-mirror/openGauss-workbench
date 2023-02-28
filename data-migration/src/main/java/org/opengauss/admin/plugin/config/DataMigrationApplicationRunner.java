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
                e.printStackTrace();
                log.error("OffLineTaskRunScheduler error", e);
            }
        });

        threadPoolTaskExecutor.submit(() -> {
            try {
                migrationMainTaskService.doRefreshMainTaskStatus();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("RefreshMainTaskStatus error", e);
            }
        });
    }
}
