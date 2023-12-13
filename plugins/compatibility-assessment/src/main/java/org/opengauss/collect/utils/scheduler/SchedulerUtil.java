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

package org.opengauss.collect.utils.scheduler;

import java.util.Map;
import org.opengauss.collect.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * SchedulerUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class SchedulerUtil {
    /**
     * getJobDetail
     *
     * @param jobName    jobName
     * @param group      group
     * @param jobDataMap jobDataMap
     * @param jobClass   jobClass
     * @return JobDetail
     */
    public static JobDetail getJobDetail(String jobName, String group, Map<String, Object> jobDataMap,
                                         Class<? extends Job> jobClass) {
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, group)
                .usingJobData(new JobDataMap(jobDataMap));
        return jobBuilder.build();
    }

    /**
     * getTrigger
     *
     * @param name          name
     * @param group         group
     * @param cronStartTime cronStartTime
     * @param cronEndTime   cronEndTime
     * @param interval      interval
     * @return Trigger
     */
    public static Trigger getTrigger(String name, String group, String cronStartTime, String cronEndTime,
                                     Integer interval) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .startAt(DateUtil.stringToDate(cronStartTime));
        if (cronEndTime != null && !cronEndTime.isEmpty()) {
            triggerBuilder.endAt(DateUtil.stringToDate(cronEndTime));
        }
        if (interval != null && interval > 0) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(interval));
        }
        return triggerBuilder.build();
    }
}

