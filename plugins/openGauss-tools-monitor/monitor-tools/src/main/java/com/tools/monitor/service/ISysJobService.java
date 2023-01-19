package com.tools.monitor.service;

import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.TargetSource;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.quartz.domain.SysJob;
import org.quartz.SchedulerException;

/**
 * ISysJobService
 *
 * @author liu
 * @since 2022-10-01
 */
public interface ISysJobService {

    ResponseVO selectAllJob(Integer page, Integer size, SysJob job);

    int pauseJob(SysJob job) throws SchedulerException;

    int resumeJob(SysJob job) throws SchedulerException;

    Boolean deleteJob(SysJob job) throws SchedulerException;

    void deleteJobByIds(Long[] jobIds) throws SchedulerException;

    void run(SysJob job) throws SchedulerException;

    AjaxResult insertJob(SysJob job) throws SchedulerException, TaskException, TaskException;

    AjaxResult updateJob(SysJob job) throws SchedulerException, TaskException, TaskException;

    boolean checkCronExpressionIsValid(String cronExpression);

    AjaxResult batchPublish(TargetSource TargetSource) throws SchedulerException;

    AjaxResult singlePublishPause(SysSourceTarget sourceTarget) throws SchedulerException;

    AjaxResult batchPublishPause(TargetSource targetSource) throws SchedulerException;

    ResponseVO selectGroup();

    ResponseVO checkJobIds(Long[] jobIds);

    ResponseVO getResulter();

    AjaxResult getDefaultTarget();
}
