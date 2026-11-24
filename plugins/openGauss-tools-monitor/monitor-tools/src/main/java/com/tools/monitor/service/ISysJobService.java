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

package com.tools.monitor.service;

import com.tools.monitor.entity.MonitorResult;
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
    /**
     * selectAllJob
     *
     * @param page page
     * @param size size
     * @param job  job
     * @return ResponseVO
     */
    ResponseVO selectAllJob(Integer page, Integer size, SysJob job);

    /**
     * pauseJob
     *
     * @param job job
     * @return int
     * @throws SchedulerException SchedulerException
     */
    int pauseJob(SysJob job) throws SchedulerException;

    /**
     * resumeJob
     *
     * @param job job
     * @return int
     * @throws SchedulerException SchedulerException
     */
    int resumeJob(SysJob job) throws SchedulerException;

    /**
     * deleteTask
     *
     * @param task task
     * @return Boolean
     */
    Boolean deleteTask(SysJob task);

    /**
     * deleteTaskByIds
     *
     * @param ids ids
     */
    void deleteTaskByIds(Long[] ids);

    /**
     * start
     *
     * @param job job
     */
    void start(SysJob job);

    /**
     * insertTask
     *
     * @param task task
     * @return MonitorResult
     */
    MonitorResult insertTask(SysJob task);

    /**
     * updateTask
     *
     * @param task task
     * @return MonitorResult
     * @throws TaskException TaskException
     * @throws SchedulerException SchedulerException
     */
    MonitorResult updateTask(SysJob task);

    /**
     * batchPublish
     *
     * @param targetSource targetSource
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    MonitorResult batchPublish(TargetSource targetSource) throws SchedulerException;

    /**
     * singlePublishPause
     *
     * @param sourceTarget sourceTarget
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    MonitorResult singlePublishPause(SysSourceTarget sourceTarget);

    /**
     * batchPublishPause
     *
     * @param targetSource targetSource
     * @return MonitorResult
     * @throws SchedulerException SchedulerException
     */
    MonitorResult batchPublishPause(TargetSource targetSource) throws SchedulerException;

    /**
     * selectGroup
     *
     * @return ResponseVO
     */
    ResponseVO selectGroup();

    /**
     * checkJobIds
     *
     * @param jobIds jobIds
     * @return ResponseVO
     */
    ResponseVO checkJobIds(Long[] jobIds);

    /**
     * getDefaultTarget
     *
     * @return MonitorResult
     */
    MonitorResult getDefaultTarget();
}
