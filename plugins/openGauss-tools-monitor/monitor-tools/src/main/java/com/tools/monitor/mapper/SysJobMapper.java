package com.tools.monitor.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.common.contant.Constants;
import com.tools.monitor.config.FileConfig;
import com.tools.monitor.entity.JsonTask;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.util.AssertUtil;
import com.tools.monitor.util.JsonUtilData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SysJobMapper
 */
@Service
public class SysJobMapper {
    /**
     * selectJobList
     *
     * @param job
     * @return list
     */
    public List<SysJob> selectJobList(SysJob job) {
        return null;
    }

    /**
     * selectJobAll
     *
     * @return list
     */
    public List<SysJob> selectJobAll() {
        JsonTask jsonTask = JsonUtilData.jsonFileToObject(FileConfig.taskConfig, JsonTask.class);
        if (jsonTask == null || CollectionUtil.isEmpty(jsonTask.getSysJobs())) {
            return new ArrayList<>();
        }
        return jsonTask.getSysJobs();
    }

    /**
     * selectJobById
     *
     * @param jobId
     * @return SysJob
     */
    public SysJob selectJobById(Long jobId) {
        List<SysJob> SysJobList = selectJobAll();
        SysJob sysJob = null;
        if (CollectionUtil.isNotEmpty(SysJobList)) {
            sysJob = SysJobList.stream().filter(s -> Objects.equals(s.getJobId(), jobId)).findFirst().orElse(null);
        }
        return sysJob;
    }

    /**
     * selectBatchJobByIds
     *
     * @param ids
     * @return list
     */
    public List<SysJob> selectBatchJobByIds(List<Long> ids) {
        List<SysJob> sysJobs = selectJobAll();
        List<SysJob> result = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                for (SysJob sysJob : sysJobs) {
                    if (id.equals(sysJob.getJobId())) {
                        result.add(sysJob);
                    }
                }
            }
        }
        return result;
    }


    /**
     * deleteJobByIds
     *
     * @param ids
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
        JsonUtilData.objectToJsonFile(FileConfig.taskConfig, jsonTask);
        return isDelete;
    }

    /**
     * updateJob
     *
     * @param job
     * @return int
     */
    public int updateJob(SysJob job) {
        List<SysJob> sysJobs = selectJobAll();
        AssertUtil.isTrue(CollectionUtil.isEmpty(sysJobs), "没有定时任务");
        SysJob sysJob = sysJobs.stream().filter(item -> ObjectUtil.isNotEmpty(item) && item.getJobId().equals(job.getJobId())).findFirst().orElse(null);
        AssertUtil.isTrue(sysJob == null, "定时任务不存在");
        for (int i = 0; i < sysJobs.size(); i++) {
            if (sysJobs.get(i).getJobId().equals(job.getJobId())) {
                sysJobs.get(i).setStatus(job.getStatus());
            }
        }
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSysJobs(sysJobs);
        JsonUtilData.objectToJsonFile(FileConfig.taskConfig, jsonTask);
        return 1;
    }

    /**
     * insertJob
     *
     * @param job
     * @return int
     */
    public int insertJob(SysJob job) {
        List<SysJob> sysJobs = selectJobAll();
        sysJobs.add(job);
        JsonTask jsonTask = new JsonTask();
        jsonTask.setSysJobs(sysJobs);
        JsonUtilData.objectToJsonFile(FileConfig.taskConfig, jsonTask);
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
        List<String> list = sysJobs.stream().map(SysJob::getTargetGroup).distinct().collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(list)){
            list.remove(Constants.SYSTEMTARGET);
        }
        return list;
    }
}
