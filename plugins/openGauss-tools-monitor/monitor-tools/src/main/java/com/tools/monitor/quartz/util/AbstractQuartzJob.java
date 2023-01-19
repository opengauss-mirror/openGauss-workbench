package com.tools.monitor.quartz.util;

import com.tools.monitor.common.contant.Constants;
import com.tools.monitor.common.contant.ScheduleConstants;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.domain.SysJobLog;
import com.tools.monitor.quartz.util.bean.BeanUtils;
import com.tools.monitor.quartz.util.spring.SpringUtils;
import com.tools.monitor.service.ISysJobLogService;
import com.tools.monitor.util.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * AbstractQuartzJob
 *
 * @author liu
 * @since 2022-10-01
 */
public abstract class AbstractQuartzJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = new SysJob();
        BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
        try {
            before(context, sysJob);
            if (sysJob != null) {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("AbstractQuartzJob fail  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * before
     *
     * @param context
     * @param sysJob
     */
    protected void before(JobExecutionContext context, SysJob sysJob) {
        threadLocal.set(new Date());
    }

    /**
     * after
     *
     * @param context
     * @param sysJob
     * @param e
     */
    protected void after(JobExecutionContext context, SysJob sysJob, Exception e) {
        Date startTime = threadLocal.get();
        threadLocal.remove();
        final SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setStartTime(startTime);
        sysJobLog.setStopTime(new Date());
        long runMs = sysJobLog.getStopTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null) {
            sysJobLog.setStatus(Constants.FAIL);
            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
            sysJobLog.setExceptionInfo(errorMsg);
        } else {
            sysJobLog.setStatus(Constants.SUCCESS);
        }
        SpringUtils.getBean(ISysJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     * doExecute
     *
     * @param context
     * @param sysJob
     * @throws Exception
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
