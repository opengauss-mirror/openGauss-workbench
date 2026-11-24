/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
