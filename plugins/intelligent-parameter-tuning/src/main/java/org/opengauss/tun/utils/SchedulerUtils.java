/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.utils;

import cn.hutool.core.util.StrUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * SchedulerUtils
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
@Component
public class SchedulerUtils {
    @Autowired
    private Scheduler scheduler;

    /**
     * createScheduleJob
     *
     * @param jobClass jobClass
     * @param jobName jobName
     * @param groupName groupName
     * @param cronExpression cronExpression
     * @param map map
     */
    public void createScheduleJob(Class<? extends Job> jobClass, String jobName, String groupName,
                                  String cronExpression, Map<String, Object> map) {
        log.info("Start creating training tasks");
        try {
            JobDetail job = buildJobDetail(jobClass, jobName, groupName, map);
            Trigger trigger = buildTrigger(jobName, groupName, cronExpression);
            scheduler.scheduleJob(job, trigger);
            log.info("Job scheduled successfully with name:{}, group: {}", jobName, groupName);
        } catch (SchedulerException exception) {
            log.error("Error scheduling job with name: {}, group: {}, message: {}",
                    jobName, groupName, exception.getMessage());
        }
    }

    private JobDetail buildJobDetail(Class<? extends Job> jobClass, String jobName, String groupName,
                                     Map<String, Object> map) {
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName, groupName)
                .usingJobData(new JobDataMap(map))
                .build();
    }

    private Trigger buildTrigger(String jobName, String groupName, String cronExpression) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", groupName);
        if (StrUtil.isNotEmpty(cronExpression)) {
            return builder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
        } else {
            return builder.startNow().build();
        }
    }

    /**
     * deleteJob
     *
     * @param jobName   jobName
     * @param groupName groupName
     */
    public void deleteJob(String jobName, String groupName) {
        try {
            JobKey jobKey = new JobKey(jobName, groupName);
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                scheduler.deleteJob(jobKey);
                log.info("Job deleted successfully with name: " + jobName + ", group: " + groupName);
            } else {
                log.info("Job does not exist with name: " + jobName + ", group: " + groupName);
            }
        } catch (SchedulerException exception) {
            log.error("Error deleting job with name: " + jobName + ", group: " + groupName + ", message: "
                    + exception.getMessage());
        }
    }

    /**
     * shutdownScheduler
     */
    @PreDestroy
    public void shutdownScheduler() {
        try {
            scheduler.shutdown();
            log.info("Scheduler has been shutdown successfully");
        } catch (SchedulerException exception) {
            log.error("Error shutting down scheduler: " + exception.getMessage());
        }
    }
}