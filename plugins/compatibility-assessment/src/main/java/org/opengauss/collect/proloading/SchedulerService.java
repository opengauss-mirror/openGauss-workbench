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

package org.opengauss.collect.proloading;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * SchedulerService
 *
 * @author liu
 * @since 2025-02-12
 */
@Slf4j
@Service
public class SchedulerService {
    private Scheduler scheduler;

    /**
     * init
     */
    @PostConstruct
    public void init() {
        try {
            // init SchedulerFactory and Scheduler
            StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            log.info("Scheduler initialized and started");
        } catch (SchedulerException e) {
            log.error("Failed to initialize the scheduler: " + e.getMessage());
        }
    }

    /**
     * getJobDetail
     *
     * @param jobName    jobName
     * @param jobDataMap jobDataMap
     * @param jobClass   jobClass
     * @return JobDetail
     */
    public static JobDetail getJobDetail(String jobName, Map<String, Object> jobDataMap,
                                         Class<? extends Job> jobClass) {
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, Constant.SCHEDULER_GROUP)
                .usingJobData(new JobDataMap(jobDataMap));
        return jobBuilder.build();
    }

    /**
     * getTrigger
     *
     * @param name          name
     * @param cronStartTime cronStartTime
     * @param cronEndTime   cronEndTime
     * @param interval      interval
     * @return Trigger
     */
    public static Trigger getTrigger(String name, String cronStartTime, String cronEndTime,
                                     Integer interval) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(name, Constant.SCHEDULER_GROUP)
                .startAt(DateUtil.stringToDate(cronStartTime));
        if (cronEndTime != null && !cronEndTime.isEmpty()) {
            triggerBuilder.endAt(DateUtil.stringToDate(cronEndTime));
        }
        if (interval != null && interval > 0) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(interval));
        }
        return triggerBuilder.build();
    }

    /**
     * startJob
     *
     * @param jobDetail jobDetail
     * @param trigger   trigger
     */
    public void startJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Failed to startJob the scheduler: " + e.getMessage());
        }
    }

    /**
     * removeJob
     *
     * @param jobName jobName
     */
    public void removeJob(String jobName) {
        try {
            JobKey jobKey = new JobKey(jobName, Constant.SCHEDULER_GROUP);
            if (scheduler.checkExists(jobKey)) {
                boolean isDelete = scheduler.deleteJob(jobKey);
                if (!isDelete) {
                    log.error("Failed to delete job: {} in group: {}", jobName, Constant.SCHEDULER_GROUP);
                } else {
                    log.info("job key: {} has been removed", jobKey);
                }
            } else {
                log.warn("Job does not exist: {} in group: {}", jobName, Constant.SCHEDULER_GROUP);
            }
        } catch (SchedulerException e) {
            log.error("Failed to start the scheduler: {}", e.getMessage());
        }
    }

    /**
     * checkDetail
     *
     * @param detail detail
     */
    public void checkDetail(JobDetail detail) {
        try {
            if (scheduler.checkExists(detail.getKey())) {
                scheduler.deleteJob(detail.getKey());
            }
        } catch (SchedulerException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * stopScheduler
     *
     * @throws SchedulerException SchedulerException
     */
    @PreDestroy
    public void stopScheduler() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
