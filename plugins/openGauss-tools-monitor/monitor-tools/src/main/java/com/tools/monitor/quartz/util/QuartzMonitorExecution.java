/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import lombok.extern.slf4j.Slf4j;
import com.tools.monitor.quartz.domain.SysJob;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * QuartzMonitorExecution
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@DisallowConcurrentExecution
public class QuartzMonitorExecution extends SummaryMonitorJob {
    @Override
    public void doExecute(JobExecutionContext context, SysJob sysJob) {
        log.info("QuartzMonitorExecution-->begin");
        MonitorInvokeUtil.invokeMethod(sysJob);
        log.info("QuartzMonitorExecution-->end");
    }
}
