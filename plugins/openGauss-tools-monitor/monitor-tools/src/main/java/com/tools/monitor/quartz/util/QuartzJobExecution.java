package com.tools.monitor.quartz.util;

import org.quartz.JobExecutionContext;
import com.tools.monitor.quartz.domain.SysJob;

/**
 * QuartzJobExecution
 *
 * @author liu
 * @since 2022-10-01
 */
public class QuartzJobExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
