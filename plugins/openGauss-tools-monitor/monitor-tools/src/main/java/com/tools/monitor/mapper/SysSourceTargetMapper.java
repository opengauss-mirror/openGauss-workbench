package com.tools.monitor.mapper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tools.monitor.config.FileConfig;
import com.tools.monitor.entity.JsonSourceTarget;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.util.JsonUtilData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * SysSourceTargetMapper
 *
 * @author liu
 * @since 2022-10-01
 */
@Service
public class SysSourceTargetMapper {

    /**
     * save
     *
     * @param sysSourceTarget
     */
    public void save(SysSourceTarget sysSourceTarget) {
        List<SysSourceTarget> sysSourceTargets = deleteBySourceId(sysSourceTarget.getDataSourceId());
        sysSourceTarget.setLastReleaseTime(new Date());
        sysSourceTargets.add(sysSourceTarget);
        JsonSourceTarget jsonSourceTarget = new JsonSourceTarget();
        jsonSourceTarget.setSysSourceTarget(sysSourceTargets);
        JsonUtilData.objectToJsonFile(FileConfig.relationConfig, jsonSourceTarget);
    }

    /**
     * getAll
     *
     * @return list
     */
    public List<SysSourceTarget> getAll() {
        JsonSourceTarget jsonSourceTarget = JsonUtilData.jsonFileToObject(FileConfig.relationConfig, JsonSourceTarget.class);
        if (jsonSourceTarget == null || CollectionUtil.isEmpty(jsonSourceTarget.getSysSourceTarget())) {
            return new ArrayList<>();
        }
        return jsonSourceTarget.getSysSourceTarget();
    }

    /**
     * getPublishJobIds
     *
     * @return list
     */
    public List<Long> getPublishJobIds() {
        List<SysSourceTarget> list = getAll();
        List<Long> jobIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (SysSourceTarget sysSourceTarget : list) {
                if (ObjectUtil.isNotEmpty(sysSourceTarget) & CollectionUtil.isNotEmpty(sysSourceTarget.getJobIds())) {
                    jobIds.addAll(sysSourceTarget.getJobIds());
                }
            }
        }
        return jobIds;
    }

    /**
     * getSourceIdByJobId
     *
     * @param jobId
     * @return list
     */
    public List<Long> getSourceIdByJobId(Long jobId) {
        List<SysSourceTarget> sysSourceTargets = getAll();
        List<Long> sourceId = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(sysSourceTargets)) {
            for (SysSourceTarget sysSourceTarget : sysSourceTargets) {
                List<Long> jobIds = sysSourceTarget.getJobIds();
                if (CollectionUtil.isNotEmpty(jobIds)) {
                    for (Long id : jobIds) {
                        if (jobId.equals(id)) {
                            sourceId.add(sysSourceTarget.getDataSourceId());
                        }
                    }
                }
            }
        }
        return sourceId;
    }

    /**
     * getJobIdBySourceId
     *
     * @param dataSourceId
     * @return list
     */
    public List<Long> getJobIdBySourceId(Long dataSourceId) {
        return getAll().stream().filter(item -> dataSourceId.equals(item.getDataSourceId())).findFirst().orElse(new SysSourceTarget()).getJobIds();
    }

    /**
     * sysSourceTargetById
     *
     * @param id
     * @return SysSourceTarget
     */
    public SysSourceTarget sysSourceTargetById(Long id) {
        SysSourceTarget sourceTarget = new SysSourceTarget();
        List<SysSourceTarget> all = getAll();
        if (CollectionUtil.isNotEmpty(all)) {
            for (SysSourceTarget sysSourceTarget : all) {
                if (id.equals(sysSourceTarget.getDataSourceId())) {
                    sourceTarget = sysSourceTarget;
                }
            }
        }
        return sourceTarget;
    }

    /**
     * deleteBySourceId
     *
     * @param sourceId
     * @return list
     */
    public List<SysSourceTarget> deleteBySourceId(Long sourceId) {
        List<SysSourceTarget> sysSourceTargets = getAll();
        List<SysSourceTarget> result = new ArrayList<>();
        for (SysSourceTarget sourceTarget : sysSourceTargets) {
            if (sourceTarget.getDataSourceId().equals(sourceId)) {
                result.add(sourceTarget);
            }
        }
        sysSourceTargets.removeAll(result);
        JsonSourceTarget jsonSourceTarget = new JsonSourceTarget();
        jsonSourceTarget.setSysSourceTarget(sysSourceTargets);
        JsonUtilData.objectToJsonFile(FileConfig.relationConfig, jsonSourceTarget);
        return sysSourceTargets;
    }

    /**
     * getMoreThanOneSource
     *
     * @param oldSysJob oldSysJob
     * @return list
     */
    public List<SysJob> getMoreThanOneSource(List<SysJob> oldSysJob) {
        List<SysJob> sysJobs = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(oldSysJob)) {
            for (SysJob sysJob : oldSysJob) {
                List<Long> sourceId = getSourceIdByJobId(sysJob.getJobId());
                if (sourceId.size() > 1) {
                    sysJobs.add(sysJob);
                }
            }
        }
        return sysJobs;
    }

    /**
     * removeJobids
     *
     * @param jobId
     */
    public void removeJobids(Long jobId) {
        List<Long> sourceId = getSourceIdByJobId(jobId);
        if (CollectionUtil.isNotEmpty(sourceId)) {
            for (Long id : sourceId) {
                SysSourceTarget sourceTarget = sysSourceTargetById(id);
                if (sourceTarget != null && CollectionUtil.isNotEmpty(sourceTarget.getJobIds())) {
                    List<Long> newId = sourceTarget.getJobIds();
                    newId.remove(jobId);
                    sourceTarget.setJobIds(newId);
                    save(sourceTarget);
                }
            }
        }
    }
}
