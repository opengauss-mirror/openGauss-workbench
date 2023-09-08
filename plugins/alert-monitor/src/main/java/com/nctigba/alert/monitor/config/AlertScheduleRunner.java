/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config;

import cn.hutool.core.collection.CollectionUtil;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertSchedule;
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
public class AlertScheduleRunner implements ApplicationRunner {
    @Autowired
    private AlertScheduleService alertScheduleService;
    @Autowired
    private TaskRegistrar taskRegistrar;

    @Override
    public void run(ApplicationArguments args) {
        log.info("startup all tasks");
        List<AlertSchedule> list = alertScheduleService.list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        for (AlertSchedule alertSchedule : list) {
            String type = alertSchedule.getType();
            if (!type.equals(CommonConstants.LOG_RULE)) {
                continue;
            }
            LocalDateTime lastTime = alertSchedule.getLastTime();
            LocalDateTime now = LocalDateTime.now();
            Date startTime = null;
            if (lastTime == null) {
                startTime = Date.from(now.plus(alertSchedule.getInitialDelay(), ChronoUnit.MILLIS).atZone(
                    ZoneId.systemDefault()).toInstant());
            }
            if (lastTime != null && lastTime.plus(alertSchedule.getFixedDelay(), ChronoUnit.MILLIS).isAfter(now)) {
                startTime = Date.from(lastTime.plus(alertSchedule.getFixedDelay(), ChronoUnit.MILLIS)
                    .atZone(ZoneId.systemDefault()).toInstant());
            }
            Runnable task = new AlertLogSchedulingRunnable(alertSchedule.getJobName());
            String jobType = alertSchedule.getJobType();
            if (jobType.equals(CommonConstants.FIXED_DELAY)) {
                taskRegistrar.addFixedDelay(task, alertSchedule.getFixedDelay(), startTime);
            } else if (jobType.equals(CommonConstants.FIXED_RATE)) {
                taskRegistrar.addFixedRateTask(task, alertSchedule.getFixedRate(), startTime);
            } else {
                taskRegistrar.addCronTask(task, alertSchedule.getCron());
            }
        }
    }
}
