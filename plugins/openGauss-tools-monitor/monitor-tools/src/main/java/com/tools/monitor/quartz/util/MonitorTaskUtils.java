/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import com.tools.monitor.common.contant.ConmmonShare;
import com.tools.monitor.common.contant.ScheduleCommon;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.util.spring.MonitSpringUtils;
import com.tools.monitor.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * MonitorTaskUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorTaskUtils {
    /**
     * getMonitorClass
     *
     * @param job job
     * @return Class
     */
    private static Class<? extends Job> getMonitorClass(SysJob job) {
        boolean isSameTime = "0".equals(job.getConcurrent());
        if (isSameTime) {
            return MonitorJobExecution.class;
        } else {
            return QuartzMonitorExecution.class;
        }
    }

    /**
     * getTriggerKey
     *
     * @param num num
     * @param str str
     * @return TriggerKey
     */
    public static TriggerKey getMonitorKey(Long num, String str) {
        return TriggerKey.triggerKey(ScheduleCommon.MONITOR_CLASS_NAME + num, str);
    }

    /**
     * getMonitorWorkKey
     *
     * @param jobId    jobId
     * @param jobGroup jobGroup
     * @return JobKey
     */
    public static JobKey getMonitorWorkKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(ScheduleCommon.MONITOR_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * createMonitorJob
     *
     * @param sch sch
     * @param task task
     * @throws SchedulerException SchedulerException
     * @throws TaskException      TaskException
     */
    public static void createMonitorJob(Scheduler sch, SysJob task) {
        try {
            Class<? extends Job> jobClass = getMonitorClass(task);
            Long num = task.getJobId();
            String str = task.getJobGroup();
            JobDetail monitorDetail = JobBuilder.newJob(jobClass).withIdentity(getMonitorWorkKey(num, str)).build();
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(task.getCronExpression());
            cronScheduleBuilder = handleMonitorMisfirePolicy(task, cronScheduleBuilder);
            CronTrigger cron = TriggerBuilder.newTrigger().withIdentity(getMonitorKey(num, str))
                    .withSchedule(cronScheduleBuilder).build();
            monitorDetail.getJobDataMap().put(ScheduleCommon.MONITOR_PROPERTIES, task);
            if (sch.checkExists(getMonitorWorkKey(num, str))) {
                sch.deleteJob(getMonitorWorkKey(num, str));
            }
            sch.scheduleJob(monitorDetail, cron);
            if (task.getStatus().equals(ScheduleCommon.Status.PAUSE.getValue())) {
                sch.pauseJob(MonitorTaskUtils.getMonitorWorkKey(num, str));
            }
        } catch (TaskException | SchedulerException exception) {
            log.error("createMonitorJob fail-->{}", exception.getMessage());
        }
    }

    /**
     * handleMonitorMisfirePolicy
     *
     * @param task task
     * @param builder  builder
     * @return CronScheduleBuilder
     * @throws TaskException TaskException
     */
    public static CronScheduleBuilder handleMonitorMisfirePolicy(SysJob task, CronScheduleBuilder builder)
            throws TaskException {
        switch (task.getMisfirePolicy()) {
            case ScheduleCommon.MONITOR_DEFAULT:
                return builder;
            case ScheduleCommon.MONITOR_IGNORE_MISFIRES:
                return builder.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleCommon.MONITOR_FIRE_AND_PROCEED:
                return builder.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleCommon.MONITOR_DO_NOTHING:
                return builder.withMisfireHandlingInstructionDoNothing();
            default:
                throw new TaskException("The task misfire policy '" + task.getMisfirePolicy()
                        + "' cannot be used in cron schedule tasks", TaskException.Code.CONFIG_ERROR);
        }
    }

    /**
     * legalList
     *
     * @param str str
     * @return boolean
     */
    public static boolean legalList(String str) {
        String name = StringUtils.substringBefore(str, "(");
        int num = StringUtils.countMatches(name, ".");
        if (num > 1) {
            return StringUtils.includeAnyCases(str, ConmmonShare.JOB_WHITELIST_STR);
        }
        Object obj = MonitSpringUtils.getStr(StringUtils.split(str, ".")[0]);
        return StringUtils.includeAnyCases(obj.getClass().getPackage().getName(), ConmmonShare.JOB_WHITELIST_STR);
    }
}
