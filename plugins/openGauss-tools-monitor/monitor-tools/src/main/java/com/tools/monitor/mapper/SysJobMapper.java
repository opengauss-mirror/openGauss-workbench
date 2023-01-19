/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.common.contant.ConmmonShare;
import com.tools.monitor.config.FileConfig;
import com.tools.monitor.entity.JsonTask;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.util.AssertUtil;
import com.tools.monitor.util.JsonUtilData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * SysJobMapper
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class SysJobMapper {
    /**
     * selectJobAll
     *
     * @return list
     */
    public List<SysJob> selectJobAll() {
        JsonTask jsonTask = JsonUtilData.jsonFileToObject(FileConfig.getTaskConfig(), JsonTask.class);
        if (jsonTask == null || CollectionUtil.isEmpty(jsonTask.getSysJobs())) {
            return new ArrayList<>();
        }
        return jsonTask.getSysJobs();
    }

    /**
     * Query scheduling task information by scheduling ID
     *
     * @param jobId jobId
     * @return SysJob
     */
    public SysJob selectJobById(Long jobId) {
        List<SysJob> sysJobList = selectJobAll();
        SysJob sysJob = null;
        if (CollectionUtil.isNotEmpty(sysJobList)) {
            sysJob = sysJobList.stream().filter(s -> Objects.equals(s.getJobId(), jobId)).findFirst().orElse(null);
        }
        return sysJob;
    }

    /**
     * Batch Query Scheduling Task Information by Scheduling IDS
     *
     * @param ids ids
     * @return list
     */
    public List<SysJob> selectBatchJobByIds(List<Long> ids) {
        List<SysJob> sysJobs = selectJobAll();
        List<SysJob> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        for (Long id : ids) {
            for (SysJob sysJob : sysJobs) {
                if (id.equals(sysJob.getJobId())) {
                    result.add(sysJob);
                }
            }
        }
        return result;
    }


    /**
     * deleteJobByIds
     *
     * @param ids ids
     * @return Boolean
     */
    public Boolean deleteJobByIds(List<Long> ids) {
        List<SysJob> sysJobs = selectJobAll();
        List<SysJob> result = new ArrayList<>();
        for (SysJob sysJob : sysJobs) {
            for (Long id : ids) {
                if (id.equals(sysJob.getJobId())) {
                    result.add(sysJob);
                }
            }
        }
        Boolean isDelete = sysJobs.removeAll(result);
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSysJobs(sysJobs);
        JsonUtilData.objectToJsonFile(FileConfig.getTaskConfig(), jsonTask);
        return isDelete;
    }

    /**
     * Modify Scheduled Task Status
     *
     * @param job job
     * @return int
     */
    public int updateJob(SysJob job) {
        List<SysJob> sysJobs = selectJobAll();
        AssertUtil.isTrue(CollectionUtil.isEmpty(sysJobs), "No timed tasks");
        SysJob sysJob = sysJobs.stream()
                .filter(item -> ObjectUtil.isNotEmpty(item) && item.getJobId().equals(job.getJobId()))
                .findFirst().orElse(null);
        if (sysJob == null) {
            return 0;
        }
        for (SysJob item : sysJobs) {
            if (item.getJobId().equals(job.getJobId())) {
                item.setStatus(job.getStatus());
            }
        }
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSysJobs(sysJobs);
        JsonUtilData.objectToJsonFile(FileConfig.getTaskConfig(), jsonTask);
        return 1;
    }

    /**
     * Add scheduling task information
     *
     * @param job job
     * @return int
     */
    public int insertJob(SysJob job) {
        List<SysJob> sysJobs = selectJobAll();
        sysJobs.add(job);
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSysJobs(sysJobs);
        JsonUtilData.objectToJsonFile(FileConfig.getTaskConfig(), jsonTask);
        return 1;
    }

    /**
     * getGroup
     *
     * @return list
     */
    public List<String> getGroup() {
        List<SysJob> sysJobs = selectJobAll();
        if (CollectionUtil.isEmpty(sysJobs)) {
            return new ArrayList<>();
        }
        List<String> list = sysJobs.stream().filter(item -> !item.getTargetGroup().equals(ConmmonShare.SYSTEMTARGET))
                .map(SysJob::getTargetGroup).distinct().collect(Collectors.toList());
        return list;
    }
}
