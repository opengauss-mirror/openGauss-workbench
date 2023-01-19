/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import com.tools.monitor.quartz.domain.SysJob;
import org.quartz.JobExecutionContext;

/**
 * MonitorJobExecution
 *
 * @author liu
 * @since 2022-10-01
 */
public class MonitorJobExecution extends SummaryMonitorJob {
    @Override
    public void doExecute(JobExecutionContext context, SysJob sysJob) {
        MonitorInvokeUtil.invokeMethod(sysJob);
    }
}
