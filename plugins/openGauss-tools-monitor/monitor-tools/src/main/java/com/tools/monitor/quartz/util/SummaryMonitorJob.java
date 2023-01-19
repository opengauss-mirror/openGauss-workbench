/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import com.tools.monitor.common.contant.ScheduleCommon;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.util.bean.MonitorClassUtils;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * SummaryMonitorJob
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public abstract class SummaryMonitorJob implements Job {
    /**
     * monitorDate
     */
    public static ThreadLocal<Date> monitorDate = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) {
        SysJob monitorJob = new SysJob();
        MonitorClassUtils.attributeCopy(monitorJob, context.getMergedJobDataMap().get(ScheduleCommon.MONITOR_PROPERTIES));
        beginStart();
        if (jdege(monitorJob)) {
            doExecute(context, monitorJob);
        }
    }

    private Boolean jdege(SysJob sysJob){
        if(sysJob != null){
            return true;
        }
        return false;
    }

    /**
     * beginStart
     */
    public void beginStart() {
        monitorDate.set(new Date());
    }

    /**
     * doExecute
     *
     * @param context context
     * @param sysJob  sysJob
     * @throws Exception Exception
     */
    public abstract void doExecute(JobExecutionContext context, SysJob sysJob);
}
