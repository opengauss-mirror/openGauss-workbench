/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.controller;

import com.tools.monitor.common.core.controller.BasicController;
import com.tools.monitor.entity.MonitorResult;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.TargetSource;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.service.ISysJobService;
import lombok.extern.slf4j.Slf4j;
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
public class SysJobController extends BasicController {
    @Autowired
    private ISysJobService jobService;

    /**
     * list
     *
     * @param page page
     * @param size size
     * @param job job
     * @return ResponseVO
     */
    @PostMapping("/list/{page}/{size}")
    public ResponseVO list(@PathVariable Integer page, @PathVariable Integer size, @RequestBody SysJob job) {
        return jobService.selectAllJob(page, size, job);
    }

    /**
     * getDefaultTarget
     *
     * @return MonitorResult
     */
    @GetMapping("/list/default/target")
    public MonitorResult getDefaultTarget() {
        return jobService.getDefaultTarget();
    }

    /**
     * getGroup
     *
     * @return ResponseVO
     */
    @GetMapping(value = "/group")
    public ResponseVO getGroup() {
        return jobService.selectGroup();
    }

    /**
     * add
     *
     * @param job job
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     * @throws TaskException TaskException
     */
    @PostMapping(value = "/create")
    public MonitorResult add(@Validated @RequestBody SysJob job) throws SchedulerException, TaskException {
        job.setIsCreate(true);
        return jobService.insertTask(job);
    }

    /**
     * edit
     *
     * @param job job
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     * @throws TaskException TaskException
     */
    @PostMapping(value = "/update")
    public MonitorResult edit(@Validated @RequestBody SysJob job) throws SchedulerException, TaskException {
        job.setIsCreate(false);
        return jobService.updateTask(job);
    }

    /**
     * batchPublish
     *
     * @param targetSource targetSource
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @PostMapping("/batch/publish")
    public MonitorResult batchPublish(@RequestBody TargetSource targetSource) throws SchedulerException {
        return jobService.batchPublish(targetSource);
    }

    /**
     * singlePublishPause
     *
     * @param sourceTarget sourceTarget
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @PostMapping("/single/publish/pause")
    public MonitorResult singlePublishPause(@Validated @RequestBody SysSourceTarget sourceTarget) {
        return jobService.singlePublishPause(sourceTarget);
    }

    /**
     * batchPublishPause
     *
     * @param sourceTargets sourceTargets
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @PostMapping("/batch/publish/pause")
    public MonitorResult batchPublishPause(@RequestBody TargetSource sourceTargets) throws SchedulerException {
        return jobService.batchPublishPause(sourceTargets);
    }

    /**
     * run
     *
     * @param job job
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @PutMapping("/run")
    public MonitorResult run(@Validated @RequestBody SysJob job) throws SchedulerException {
        jobService.start(job);
        return MonitorResult.success();
    }

    /**
     * remove
     *
     * @param jobIds jobIds
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     * @throws TaskException TaskException
     */
    @PostMapping("/delete")
   public MonitorResult remove(@RequestBody Long[] jobIds) throws SchedulerException, TaskException {
        jobService.deleteTaskByIds(jobIds);
        return MonitorResult.success("删除成功");
    }

    /**
     * check
     *
     * @param jobIds jobIds
     * @return ResponseVO
     * @throws SchedulerException SchedulerException
     * @throws TaskException TaskException
     */
    @PostMapping("/check")
    public ResponseVO check(@RequestBody Long[] jobIds) throws SchedulerException, TaskException {
        return jobService.checkJobIds(jobIds);
    }
}
