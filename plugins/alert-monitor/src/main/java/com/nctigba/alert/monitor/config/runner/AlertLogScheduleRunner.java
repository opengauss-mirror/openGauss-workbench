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
 *  AlertLogScheduleRunner.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/config/runner/AlertLogScheduleRunner.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.config.runner;

import cn.hutool.core.collection.CollectionUtil;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.schedule.AlertLogSchedulingRunnable;
import com.nctigba.alert.monitor.schedule.TaskRegistrar;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * AlertScheduleRunner
 *
 * @since 2023/8/1 18:45
 */
@Component
@Slf4j
public class AlertLogScheduleRunner implements ApplicationRunner {
    @Autowired
    private AlertScheduleService alertScheduleService;
    @Autowired
    private TaskRegistrar taskRegistrar;

    @Override
    public void run(ApplicationArguments args) {
        log.info("startup all tasks");
        List<AlertScheduleDO> list = alertScheduleService.list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (AlertScheduleDO alertScheduleDO : list) {
            String type = alertScheduleDO.getType();
            if (!type.equals(CommonConstants.LOG_RULE)) {
                continue;
            }
            LocalDateTime lastTime = alertScheduleDO.getLastTime();
            LocalDateTime now = LocalDateTime.now();
            Date startTime = null;
            if (lastTime == null) {
                startTime = Date.from(now.plus(alertScheduleDO.getInitialDelay(), ChronoUnit.MILLIS).atZone(
                    ZoneId.systemDefault()).toInstant());
            }
            if (lastTime != null && lastTime.plus(alertScheduleDO.getFixedDelay(), ChronoUnit.MILLIS).isAfter(now)) {
                startTime = Date.from(lastTime.plus(alertScheduleDO.getFixedDelay(), ChronoUnit.MILLIS)
                    .atZone(ZoneId.systemDefault()).toInstant());
            }
            Runnable task = new AlertLogSchedulingRunnable(alertScheduleDO.getJobName());
            String jobType = alertScheduleDO.getJobType();
            if (jobType.equals(CommonConstants.FIXED_DELAY)) {
                taskRegistrar.addFixedDelay(task, alertScheduleDO.getFixedDelay(), startTime);
            } else if (jobType.equals(CommonConstants.FIXED_RATE)) {
                taskRegistrar.addFixedRateTask(task, alertScheduleDO.getFixedRate(), startTime);
            } else {
                taskRegistrar.addCronTask(task, alertScheduleDO.getCron());
            }
        }
    }
}
