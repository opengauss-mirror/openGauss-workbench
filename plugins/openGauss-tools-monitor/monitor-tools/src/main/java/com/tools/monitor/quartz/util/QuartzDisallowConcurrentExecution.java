package com.tools.monitor.quartz.util;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import com.tools.monitor.quartz.domain.SysJob;

/**
 * QuartzDisallowConcurrentExecution
 *
 * @author liu
 * @since 2022-10-01
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
