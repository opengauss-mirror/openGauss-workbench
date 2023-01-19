package com.tools.monitor.controller;

import com.tools.monitor.common.core.controller.BaseController;
import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.TargetSource;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.service.ISysJobService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SysJobController
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@RestController
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController {
    @Autowired
    private ISysJobService jobService;

    @PostMapping("/list/{page}/{size}")
    public ResponseVO list(@PathVariable Integer page, @PathVariable Integer size, @RequestBody SysJob job) {
        return jobService.selectAllJob(page, size, job);
    }

    @GetMapping("/list/default/target")
    public AjaxResult getDefaultTarget() {
        return jobService.getDefaultTarget();
    }

    @GetMapping(value = "/group")
    public ResponseVO getGroup() {
        return jobService.selectGroup();
    }

    @PostMapping(value = "/create")
    @Log(title = "monitor-tools-job", businessType = BusinessType.INSERT, operatorType = OperatorType.PLUGIN)
    public AjaxResult add(@Validated @RequestBody SysJob job) throws SchedulerException, TaskException {
        job.setIsCreate(true);
        return jobService.insertJob(job);
    }

    @PostMapping(value = "/update")
    @Log(title = "monitor-tools-job", businessType = BusinessType.UPDATE, operatorType = OperatorType.PLUGIN)
    public AjaxResult edit(@Validated @RequestBody SysJob job) throws SchedulerException, TaskException {
        job.setIsCreate(false);
        return jobService.updateJob(job);
    }

    @PostMapping("/batch/publish")
    @Log(title = "monitor-tools-job", businessType = BusinessType.OTHER, operatorType = OperatorType.PLUGIN)
    public AjaxResult batchPublish(@RequestBody TargetSource targetSource) throws SchedulerException {
        return jobService.batchPublish(targetSource);
    }

    @PostMapping("/single/publish/pause")
    @Log(title = "monitor-tools-job", businessType = BusinessType.STOP, operatorType = OperatorType.PLUGIN)
    public AjaxResult singlePublishPause(@Validated @RequestBody SysSourceTarget sourceTarget) throws SchedulerException {
        return jobService.singlePublishPause(sourceTarget);
    }

    @PostMapping("/batch/publish/pause")
    @Log(title = "monitor-tools-job", businessType = BusinessType.STOP, operatorType = OperatorType.PLUGIN)
    public AjaxResult batchPublishPause(@RequestBody TargetSource sourceTargets) throws SchedulerException {
        return jobService.batchPublishPause(sourceTargets);
    }

    @PutMapping("/run")
    public AjaxResult run(@Validated @RequestBody SysJob job) throws SchedulerException {
        jobService.run(job);
        return AjaxResult.success();
    }

    @PostMapping("/delete")
    @Log(title = "monitor-tools-job", businessType = BusinessType.DELETE, operatorType = OperatorType.PLUGIN)
    public AjaxResult remove(@RequestBody Long[] jobIds) throws SchedulerException, TaskException {
        jobService.deleteJobByIds(jobIds);
        return AjaxResult.success("删除成功");
    }

    @PostMapping("/check")
    public ResponseVO check(@RequestBody Long[] jobIds) throws SchedulerException, TaskException {
        return jobService.checkJobIds(jobIds);
    }
}
