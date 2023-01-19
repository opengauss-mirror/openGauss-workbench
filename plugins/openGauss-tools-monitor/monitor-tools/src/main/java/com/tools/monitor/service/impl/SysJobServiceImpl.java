/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.tools.monitor.common.contant.ConmmonShare;
import com.tools.monitor.common.contant.ScheduleCommon;
import com.tools.monitor.config.NagiosConfig;
import com.tools.monitor.entity.MFilter;
import com.tools.monitor.entity.MonitorResult;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.TargetSource;
import com.tools.monitor.entity.zabbix.ZabbixMessge;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.manager.MonitorManager;
import com.tools.monitor.manager.factory.AsyncFactory;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.mapper.SysJobMapper;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.util.MonitorTaskUtils;
import com.tools.monitor.service.ISysJobService;
import com.tools.monitor.service.MonitorFlake;
import com.tools.monitor.service.MonitorService;
import com.tools.monitor.util.AssertUtil;
import com.tools.monitor.util.Base64;
import com.tools.monitor.util.HandleUtils;
import com.tools.monitor.util.SqlUtil;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * SysJobServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@DependsOn("generatorFile")
@Service
public class SysJobServiceImpl implements ISysJobService {
    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String FITE = StrUtil.LF;

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SysJobMapper jobMapper;

    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private SysSourceTargetMapper sourceTargetMapper;

    @Autowired
    private ZabbixServiceImpl zabbixService;

    @Autowired
    private CommonServiceImpl commonService;

    @Autowired
    private NagiosServiceImpl nagiosService;

    @Value("${date.pattern}")
    private String dataPattern;

    private MonitorFlake MonitorFlake = new MonitorFlake(11, 11);

    private JdbcTemplate jdbcTemplate;


    /**
     * init
     *
     * @throws SchedulerException SchedulerException
     * @throws TaskException      TaskException
     */
    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList) {
            MonitorTaskUtils.createMonitorJob(scheduler, job);
        }
    }

    /**
     * selectAllJob
     *
     * @param page page
     * @param size size
     * @param job  job
     * @return ResponseVO responseVO
     */
    @Override
    public ResponseVO selectAllJob(Integer page, Integer size, SysJob job) {
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        // 添加 发布信息
        addPublish(sysJobs, job);
        sortJob(sysJobs);
        List<String> platForm = sysJobs.stream().map(SysJob::getPlatform).distinct().collect(Collectors.toList());
        List<String> targetGroup = sysJobs.stream().map(SysJob::getTargetGroup).distinct().collect(Collectors.toList());
        List<MFilter> form = new ArrayList<>();
        List<MFilter> group = new ArrayList<>();
        dealMap(platForm, targetGroup, form, group);
        Map<String, Object> myResMap = new HashMap<>();
        myResMap.put("platForm", form);
        myResMap.put("targetGroup", group);
        sysJobs = filter(sysJobs, job);
        List<SysJob> resulte = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(job) && ObjectUtil.isNotEmpty(job.getIsManagement()) && job.getIsManagement()) {
            resulte = sysJobs;
        } else {
            resulte = sysJobs.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList());
        }
        addIsCanUpdate(resulte);
        myResMap.put("tableData", resulte);
        return ResponseVO.pageResponseVO(sysJobs.size(), myResMap);
    }

    @Override
    public MonitorResult getDefaultTarget() {
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        if (CollectionUtil.isNotEmpty(sysJobs)) {
            sysJobs = sysJobs.stream()
                    .filter(item -> item.getTargetGroup().equals(ConmmonShare.SYSTEMTARGET)).collect(Collectors.toList());
            return MonitorResult.success(sysJobs);
        }
        return MonitorResult.success(new ArrayList<>());
    }

    private void addIsCanUpdate(List<SysJob> resulte) {
        List<Long> publishJobIds = sourceTargetMapper.getPublishJobIds();
        List<SysConfig> sysConfigs = configMapper.getAllConfig();
        List<Long> ids = sysConfigs.stream().map(SysConfig::getDataSourceId).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(resulte)) {
            return;
        }
        for (SysJob sysJob : resulte) {
            if (ObjectUtil.isNotEmpty(sysJob)
                    && ObjectUtil.isNotEmpty(sysJob.getDataSourceId())
                    && !ids.contains(sysJob.getDataSourceId())) {
                sysJob.setDataSourceId(null);
            }
            if (CollectionUtil.isNotEmpty(publishJobIds)) {
                Long id = publishJobIds.stream()
                        .filter(item -> ObjectUtil.isNotEmpty(item) && item.equals(sysJob.getJobId())).findFirst()
                        .orElse(null);
                if (id == null) {
                    sysJob.setIsCanUpdate(true);
                } else {
                    sysJob.setIsCanUpdate(false);
                }
            } else {
                sysJob.setIsCanUpdate(true);
            }
        }
    }

    /**
     * addPublish
     *
     * @param sysJobs sysJobs
     * @param job     job
     */
    private void addPublish(List<SysJob> sysJobs, SysJob job) {
        // Get published metrics for this host based on job
        if (ObjectUtil.isNotEmpty(job) && job.getDataSourceId() != null) {
            List<Long> jobIds = sourceTargetMapper.getJobIdBySourceId(job.getDataSourceId());
            for (SysJob sysJob : sysJobs) {
                if (ObjectUtil.isNotEmpty(sysJob)
                        && CollectionUtil.isNotEmpty(jobIds)
                        && jobIds.contains(sysJob.getJobId())) {
                    sysJob.setIsPbulish(true);
                } else {
                    sysJob.setIsPbulish(false);
                }
            }
        }
    }

    /**
     * filter
     *
     * @param sysJobs sysJobs
     * @param job     job
     * @return list
     */
    private List<SysJob> filter(List<SysJob> sysJobs, SysJob job) {
        List<SysJob> result = new ArrayList<>();
        if (job != null) {
            result = sysJobs;
            if (StrUtil.isNotEmpty(job.getPlatform())) {
                List<String> platForm = Arrays.asList(job.getPlatform().split(","));
                result = sysJobs.stream()
                        .filter(item -> platForm.contains(item.getPlatform()))
                        .collect(Collectors.toList());
            }
            if (StrUtil.isNotEmpty(job.getTargetGroup())) {
                List<String> targetGroup = Arrays.asList(job.getTargetGroup().split(","));
                result = sysJobs.stream()
                        .filter(item -> targetGroup.contains(item.getTargetGroup()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtil.isNotEmpty(job.getTimeInterval()) && job.getTimeInterval().size() == 2) {
                List<String> strings = job.getTimeInterval();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date start = simpleDateFormat.parse(strings.get(0));
                    Date end = simpleDateFormat.parse(strings.get(1).substring(0, 10) + " " + "23:59:59");
                    result = sysJobs.stream()
                            .filter(item -> !item.getCreateTime().before(start) && !item.getCreateTime().after(end))
                            .collect(Collectors.toList());
                } catch (ParseException e) {
                    log.error("simpleDateFormat_parse,{}", e.getMessage());
                }
            }
            return result;
        } else {
            return sysJobs;
        }
    }

    /**
     * dealMap
     *
     * @param platForm    platForm
     * @param targetGroup targetGroup
     * @param form        form
     * @param group       group
     */
    private void dealMap(List<String> platForm, List<String> targetGroup, List<MFilter> form, List<MFilter> group) {
        for (String str : platForm) {
            MFilter mFilter = new MFilter();
            mFilter.setText(str);
            mFilter.setValue(str);
            form.add(mFilter);
        }
        for (String str : targetGroup) {
            MFilter mFilter = new MFilter();
            mFilter.setText(str);
            mFilter.setValue(str);
            group.add(mFilter);
        }
    }

    /**
     * sortJob
     *
     * @param sysJobs sysJobs
     */
    private void sortJob(List<SysJob> sysJobs) {
        sysJobs.sort(Comparator.comparing(SysJob::getTime).reversed());
    }

    /**
     * batchPublish
     *
     * @param targetSource targetSource
     * @return MonitorResult MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @Override
    public MonitorResult batchPublish(TargetSource targetSource) throws SchedulerException {
        if (ObjectUtil.isEmpty(targetSource)
                || CollectionUtil.isEmpty(targetSource.getDataSourceId())
                || CollectionUtil.isEmpty(targetSource.getJobIds())) {
            return MonitorResult.error("Publish information cannot be empty");
        }
        List<Long> jobIds = targetSource.getJobIds().stream().distinct().collect(Collectors.toList());
        List<SysJob> sysJobs = jobMapper.selectBatchJobByIds(jobIds);
        List<SysJob> zabbix = sysJobs.stream()
                .filter(item -> ConmmonShare.ZABBIX.equals(item.getPlatform())).collect(Collectors.toList());
        List<SysJob> nagios = sysJobs.stream()
                .filter(item -> ConmmonShare.NAGIOS.equals(item.getPlatform())).collect(Collectors.toList());
        // Determine if there is a zabbix data source nagios data source
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && CollectionUtil.isNotEmpty(zabbix)) {
            return MonitorResult.error("Please configure the Zabbix data source information first");
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && CollectionUtil.isNotEmpty(nagios)) {
            return MonitorResult.error("Please configure Nagios configuration information first");
        }
        List<SysSourceTarget> list = new ArrayList<>();
        List<Long> dateSource = targetSource.getDataSourceId();
        for (Long id : dateSource) {
            SysSourceTarget sourceTarget = new SysSourceTarget();
            sourceTarget.setDataSourceId(id);
            sourceTarget.setJobIds(targetSource.getJobIds());
            list.add(sourceTarget);
        }
        for (SysSourceTarget sourceTarget : list) {
            singlePublish(sourceTarget, sysJobs, zabbixConfig, nagiosConfig);
        }
        return MonitorResult.success("Batch release was successful");
    }

    /**
     * singlePublish
     *
     * @param sourceTarget sourceTarget
     * @param sysJobs      sysJobs
     * @param zabbixConfig zabbixConfig
     * @param nagiosConfig nagiosConfig
     * @return MonitorResult MonitorResult
     * @throws SchedulerException SchedulerException
     */
    public MonitorResult singlePublish(SysSourceTarget sourceTarget,
                                       List<SysJob> sysJobs,
                                       SysConfig zabbixConfig,
                                       SysConfig nagiosConfig) throws SchedulerException {
        SysConfig sysConfig = configMapper.getConfigByid(sourceTarget.getDataSourceId());
        if (sysConfig == null) {
            return MonitorResult.error("No information for this host");
        }
        // The indicator below this host needs to stop the original and release the latest
        // Get the indicator information under the original host
        List<Long> oldJobIds = sourceTargetMapper.getJobIdBySourceId(sourceTarget.getDataSourceId());
        // Get all jobs by jobids
        List<SysJob> oldSysJob = jobMapper.selectBatchJobByIds(oldJobIds);
        List<SysJob> newSysjob = jobMapper.selectBatchJobByIds(sourceTarget.getJobIds());
        oldSysJob.removeAll(newSysjob);
        // Stop the old job first, after deleting it, open the job that is in use by other hosts
        stopTimeTask(oldSysJob);
        // Save the latest host publishing information
        sourceTargetMapper.save(sourceTarget);
        // delete
        List<String> remove = dealOldSysJob(oldSysJob, sysConfig.getConnectName());
        MonitorManager.mine().work(AsyncFactory.removeRegistry(remove));
        // Filter out metrics that are used by other hosts
        List<SysJob> otherSysJob = sourceTargetMapper.getMoreThanOneSource(oldSysJob);
        List<SysJob> zabbix = sysJobs.stream()
                .filter(item -> ConmmonShare.ZABBIX.equals(item.getPlatform())).collect(Collectors.toList());
        List<SysJob> nagios = sysJobs.stream()
                .filter(item -> ConmmonShare.NAGIOS.equals(item.getPlatform())).collect(Collectors.toList());
        sysJobs.addAll(otherSysJob);
        // Get rid of nagios.
        sysJobs.removeAll(nagios);
        // Post the latest
        startTimeTask(sysJobs);
        // Execute zabbix publishing tasks
        if (CollectionUtil.isNotEmpty(zabbix)) {
            MonitorManager.mine().work(AsyncFactory.recordZabbix(zabbix, sysConfig, zabbixConfig));
        }
        if (CollectionUtil.isNotEmpty(nagios)) {
            MonitorManager.mine().work(AsyncFactory.recordNagios(nagios, sysConfig, nagiosConfig));
        }
        // Delayed execution of nagios
        if (CollectionUtil.isNotEmpty(nagios)) {
            MonitorManager.mine().work(AsyncFactory.executeNagios(nagios));
        }
        return MonitorResult.success("Published successfully");
    }

    /**
     * publishNagios
     *
     * @param nagios       nagios
     * @param sysConfig    sysConfig
     * @param nagiosConfig nagiosConfig
     */
    public void publishNagios(List<SysJob> nagios, SysConfig sysConfig, SysConfig nagiosConfig) {
        Map<String, Object> all = new HashMap<>();
        sysConfig.setPassword(Base64.decode(sysConfig.getPassword()));
        for (SysJob sysJob : nagios) {
            DriverManagerDataSource dataSource = getDataSource(sysConfig);
            jdbcTemplate = new JdbcTemplate(dataSource);
            List<Map<String, Object>> list = executeSql(jdbcTemplate, sysJob.getTarget());
            dealList(list);
            String name = sysConfig.getConnectName();
            Map<String, Object> nagiosMap = new HashMap<>();
            addMap(list, all, name, sysJob, nagiosMap);
        }
        nagiosService.setConfig(all, nagiosConfig);
    }

    private void addMap(List<Map<String, Object>> list, Map<String, Object> all,
                        String name, SysJob sysJob, Map<String, Object> nagiosMap) {
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> arry = list.get(i);
            Map<String, Object> metric = HandleUtils.getMap(arry);
            dealMetric(metric, i);
            for (Map.Entry<String, Object> entry : arry.entrySet()) {
                if (entry.getValue().toString().matches(ISNUM) || entry.getValue().toString().matches(KEXUE)) {
                    nagiosMap.put(entry.getKey()
                            + "_" + sysJob.getJobName() + "_" + name + "_" + i, entry.getValue());
                    all.putAll(nagiosMap);
                }
            }
        }
    }

    private void dealList(List<Map<String, Object>> list) {
        for (Map<String, Object> maps : list) {
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                if (ObjectUtil.isEmpty(entry.getValue()) && !entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "default");
                }
                if (ObjectUtil.isEmpty(entry.getValue()) && entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "0");
                }
                if (entry.getValue().toString().startsWith(".")) {
                    String value = "0" + entry.getValue().toString();
                    maps.put(entry.getKey(), value);
                }
            }
        }
    }

    /**
     * startNagios
     *
     * @param nagios nagios
     */
    public void startNagios(List<SysJob> nagios) {
        try {
            Thread.sleep(NagiosConfig.getDelayTime());
        } catch (InterruptedException e) {
            log.error("startNagios_{}", e.getMessage());
        }
        startTimeTask(nagios);
    }

    /**
     * singlePublishPause
     *
     * @param sourceTarget sourceTarget
     * @return MonitorResult MonitorResult
     * @throws SchedulerException SchedulerException
     */
    @Override
    public MonitorResult singlePublishPause(SysSourceTarget sourceTarget) {
        SysConfig sysConfig = configMapper.getConfigByid(sourceTarget.getDataSourceId());
        if (sysConfig == null) {
            return MonitorResult.error("No information for this host");
        }
        List<Long> jobs = sourceTarget.getJobIds();
        // Collection of jobs that need to be deleted
        List<SysJob> sysJobs = jobMapper.selectBatchJobByIds(jobs);
        stopTimeTask(sysJobs);
        // Handling oldSysJob modifying columns
        // delete
        List<String> remove = dealOldSysJob(sysJobs, sysConfig.getConnectName());
        MonitorManager.mine().work(AsyncFactory.removeRegistry(remove));
        SysSourceTarget sysSourceTarget = sourceTargetMapper.sysSourceTargetById(sourceTarget.getDataSourceId());
        if (ObjectUtil.isNotEmpty(sysSourceTarget)) {
            List<Long> old = sysSourceTarget.getJobIds();
            List<Long> delete = sourceTarget.getJobIds();
            if (old != null && CollectionUtil.isNotEmpty(old)) {
                old.removeAll(delete);
                sysSourceTarget.setJobIds(old);
                sourceTargetMapper.save(sysSourceTarget);
            }
        }
        List<SysJob> otherSysJob = sourceTargetMapper.getMoreThanOneSource(sysJobs);
        startTimeTask(otherSysJob);
        return MonitorResult.success("Stop publishing successfully");
    }

    /**
     * startTimeTask
     *
     * @param sysJobs sysJobs
     * @return int int
     */
    private int startTimeTask(List<SysJob> sysJobs) {
        int num = 0;
        try {
            if (CollectionUtil.isNotEmpty(sysJobs)) {
                for (SysJob sysJob : sysJobs) {
                    MonitorManager.mine().work(
                            AsyncFactory.executeOne(sysJob.getTarget(), sysJob.getJobName(), sysJob.getJobId()));
                    sysJob.setStatus(ScheduleCommon.Status.NORMAL.getValue());
                    num = resumeJob(sysJob);
                }
            }
        } catch (SchedulerException e) {
            log.error("startTimeTask-->{}", e.getMessage());
        }
        return num;
    }

    private int stopTimeTask(List<SysJob> sysJobs) {
        int num = 0;
        try {
            if (CollectionUtil.isNotEmpty(sysJobs)) {
                for (SysJob sysJob : sysJobs) {
                    sysJob.setStatus(ScheduleCommon.Status.PAUSE.getValue());
                    num = pauseJob(sysJob);
                }
            }
        } catch (SchedulerException e) {
            log.error("stopTimeTask-->{}", e.getMessage());
        }
        return num;
    }

    private List<String> dealOldSysJob(List<SysJob> sysJobs, String name) {
        List<String> remove = new ArrayList<>();
        if (CollectionUtil.isEmpty(sysJobs)) {
            return remove;
        }
        for (SysJob sysJob : sysJobs) {
            if (ObjectUtil.isNotEmpty(sysJob)) {
                List<String> column = sysJob.getColumn();
                if (CollectionUtil.isNotEmpty(column)) {
                    column = column.stream().map(item -> item + name).collect(Collectors.toList());
                    remove.addAll(column);
                }
            }
        }

        return remove;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleCommon.Status.PAUSE.getValue());
        int rows = jobMapper.updateJob(job);
        scheduler.pauseJob(MonitorTaskUtils.getMonitorWorkKey(jobId, jobGroup));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = "DEFAULT";
        int rows = jobMapper.updateJob(job);
        scheduler.resumeJob(MonitorTaskUtils.getMonitorWorkKey(jobId, jobGroup));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTask(SysJob task) {
        Long jobId = task.getJobId();
        String jobGroup = task.getJobGroup();
        Boolean isDelete = jobMapper.deleteJobByIds(Arrays.asList(jobId));
        if (isDelete) {
            try {
                scheduler.deleteJob(MonitorTaskUtils.getMonitorWorkKey(jobId, jobGroup));
            } catch (SchedulerException exception) {
                log.error("deleteTask-->{}", exception.getMessage());
            }
        }
        return isDelete;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskByIds(Long[] jobIds) {
        // delete
        for (Long jobId : jobIds) {
            SysJob job = jobMapper.selectJobById(jobId);
            deleteTask(job);
            MonitorManager.mine().work(AsyncFactory.removeJobId(jobId));
            if (!job.getPlatform().equals(ConmmonShare.NAGIOS)) {
                List<Long> sourceId = sourceTargetMapper.getSourceIdByJobId(jobId);
                List<SysConfig> sysConfigs = configMapper.getBatchById(sourceId);
                dealExec(sysConfigs, job);
            }
        }
    }

    private void dealExec(List<SysConfig> sysConfigs, SysJob job) {
        if (CollectionUtil.isNotEmpty(sysConfigs)) {
            for (SysConfig sysConfig : sysConfigs) {
                if (ObjectUtil.isNotEmpty(job)
                        && ObjectUtil.isNotEmpty(sysConfig)
                        && ObjectUtil.isNotEmpty(sysConfig.getConnectName())) {
                    List<String> remove = dealOldSysJob(Arrays.asList(job), sysConfig.getConnectName());
                    MonitorManager.mine().work(AsyncFactory.removeRegistry(remove));
                }
            }
        }
    }

    private void deleteRelation(Long jobId) {
        sourceTargetMapper.removeJobids(jobId);
    }

    @Override
    public ResponseVO checkJobIds(Long[] jobIds) {
        for (Long jobId : jobIds) {
            List<Long> sourceId = sourceTargetMapper.getSourceIdByJobId(jobId);
            if (CollectionUtil.isNotEmpty(sourceId)) {
                List<SysConfig> sysConfigs = configMapper.getBatchById(sourceId);
                List<String> nameList = sysConfigs.stream().map(SysConfig::getConnectName).collect(Collectors.toList());
                return ResponseVO.successResponseVO("It is detected that the indicator is already in"
                        + nameList + "Publish in the instance, delete the batch from the instance," +
                        " whether to continue?");
            }
        }
        return ResponseVO.successResponseVO("");
    }

    @Override
    public MonitorResult batchPublishPause(TargetSource targetSource) throws SchedulerException {
        if (ObjectUtil.isEmpty(targetSource)
                || CollectionUtil.isEmpty(targetSource.getDataSourceId())
                || CollectionUtil.isEmpty(targetSource.getJobIds())) {
            return MonitorResult.error("Publish information cannot be empty");
        }
        List<SysSourceTarget> list = new ArrayList<>();
        List<Long> dateSource = targetSource.getDataSourceId();
        for (Long id : dateSource) {
            SysSourceTarget sourceTarget = new SysSourceTarget();
            sourceTarget.setDataSourceId(id);
            sourceTarget.setJobIds(targetSource.getJobIds());
            list.add(sourceTarget);
        }
        for (SysSourceTarget sourceTarget : list) {
            singlePublishPause(sourceTarget);
        }
        return MonitorResult.success("Batch publishing stopped successfully");
    }

    @Override
    public ResponseVO selectGroup() {
        return ResponseVO.successResponseVO(jobMapper.getGroup());
    }

    /**
     * executeZabbix
     *
     * @param zabbix       zabbix
     * @param sysConfig    sysConfig
     * @param zabbixConfig zabbixConfig
     */
    public void executeZabbix(List<SysJob> zabbix, SysConfig sysConfig, SysConfig zabbixConfig) {
        // Insert host sql
        synchronized (this) {
            String name = sysConfig.getConnectName();
            JdbcTemplate openGaussTemplate = commonService.getTem(sysConfig);
            if (ObjectUtil.isNotEmpty(zabbixConfig)
                    && StrUtil.isNotBlank(zabbixConfig.getContainerPort())
                    && StrUtil.isNotBlank(zabbixConfig.getContainerIp())) {
                ZabbixMessge zabbixMessge = zabbixService.insertNecessary(
                        name, zabbixConfig.getContainerIp(), zabbixConfig.getContainerPort());
                zabbixService.insertTarget(zabbixMessge, zabbix, openGaussTemplate);
            }
        }
    }

    /**
     * run
     *
     * @param job job
     * @throws SchedulerException SchedulerException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void start(SysJob job) {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob properties = jobMapper.selectJobById(job.getJobId());
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleCommon.MONITOR_PROPERTIES, properties);
        try {
            scheduler.triggerJob(MonitorTaskUtils.getMonitorWorkKey(jobId, jobGroup), dataMap);
        } catch (SchedulerException exception) {
            log.error("start-->{}", exception.getMessage());
        }
    }

    /**
     * insertJob
     *
     * @param job job
     * @return MonitorResult MonitorResult
     * @throws SchedulerException SchedulerException
     * @throws TaskException      TaskException
     */
    @Override
    public MonitorResult insertTask(SysJob job) {
        if (job.getTargetGroup().equalsIgnoreCase(ConmmonShare.SYSTEMTARGET) && !job.getIsCreate()) {
            modifyDefault(job);
            return MonitorResult.success("Modify successfully");
        }
        if (ObjectUtil.isEmpty(job) || ObjectUtil.isEmpty(job.getDataSourceId())) {
            return MonitorResult.error("Verify that host information cannot be empty");
        }
        if (job.getIsCreate() && job.getTargetGroup().equalsIgnoreCase(ConmmonShare.SYSTEMTARGET)) {
            return MonitorResult.error("The custom group cannot be the system default group," +
                    "please change the group name");
        }
        String res = checkConfig(job);
        if (StrUtil.isNotEmpty(res)) {
            return MonitorResult.error(res);
        }
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        Integer max = dealMax(sysJobs) + 1;
        if (job.getIsCreate()) {
            job.setJobName(job.getJobName() + max);
        }
        String message = checkTarget(job, sysJobs);
        if (StrUtil.isNotBlank(message)) {
            return MonitorResult.error(message);
        }
        checkNum(job);
        SysConfig sysConfig = getCheckConfig(job);
        if (sysConfig == null) {
            return MonitorResult.error("Please configure the data source first");
        }
        DriverManagerDataSource dataSource = getDataSource(sysConfig);
        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> list = null;
        try {
            list = commonService.executeSql(jdbcTemplate, job.getTarget());
        } catch (DataAccessException exception) {
            return MonitorResult.error(exception.getMessage());
        }
        if (CollectionUtil.isEmpty(list) && ObjectUtil.isNotEmpty(job.getIsFalse()) && !job.getIsFalse()) {
            return MonitorResult.target("Unable to generate indicator");
        }
        dealSysJob(job, list, sysConfig);
        int rows = jobMapper.insertJob(job);
        if (rows > 0) {
            MonitorTaskUtils.createMonitorJob(scheduler, job);
        }
        return MonitorResult.success("Save successfully");
    }

    private String checkConfig(SysJob job) {
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && job.getPlatform().equals(ConmmonShare.ZABBIX)
                && !job.getTargetGroup().equalsIgnoreCase(ConmmonShare.SYSTEMTARGET)) {
            return "Please configure the Zabbix data source information first";
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && job.getPlatform().equals(ConmmonShare.NAGIOS)
                && !job.getTargetGroup().equalsIgnoreCase(ConmmonShare.SYSTEMTARGET)) {
            return "Please configure Nagios configuration information first";
        }
        return "";
    }

    private SysConfig getCheckConfig(SysJob job) {
        List<SysConfig> sysConfigs = configMapper.getAllConfig()
                .stream().filter(item -> item.getPlatform().equals(ConmmonShare.PROM)).collect(Collectors.toList());
        SysConfig sysConfig = sysConfigs.stream().filter(item -> ObjectUtil.isNotEmpty(item.getDataSourceId())
                && item.getDataSourceId().equals(job.getDataSourceId())).findFirst().orElse(null);
        if (sysConfig != null) {
            sysConfig.setPassword(Base64.decode(sysConfig.getPassword()));
        }
        return sysConfig;
    }

    private Integer dealMax(List<SysJob> sysJobs) {
        Integer max = 0;
        if (CollectionUtil.isNotEmpty(sysJobs)) {
            List<String> jobName = sysJobs.stream().map(SysJob::getJobName).collect(Collectors.toList());
            List<String> finalName = jobName
                    .stream().map(item -> item.substring(3))
                    .collect(Collectors.toList());
            List<Integer> codesInteger = finalName.stream().map(Integer::parseInt).collect(Collectors.toList());
            max = codesInteger.stream().reduce(Integer::max).get();
        }
        return max;
    }

    private void modifyDefault(SysJob job) {
        checkNum(job);
        String cronExpression = getCron(job.getNum(), job.getTimeType());
        job.setCronExpression(cronExpression);
        int rows = jobMapper.insertJob(job);
        updateSchedulerJob(job, job.getJobGroup());
    }

    /**
     * checkNum
     *
     * @param job job
     */
    private void checkNum(SysJob job) {
        if (ObjectUtil.isNotEmpty(job)
                && ObjectUtil.isNotEmpty(job.getNum())
                && ObjectUtil.isNotEmpty(job.getTimeType())) {
            if (job.getTimeType().equals(ConmmonShare.SECOND) || job.getTimeType().equals(ConmmonShare.MINUTE)) {
                AssertUtil.isTrue(job.getNum() < 0 || job.getNum() > 59, "The time interval should be " +
                        "greater than 0 and less than or equal to 59");
            } else if (job.getTimeType().equals(ConmmonShare.HOUR)) {
                AssertUtil.isTrue(job.getNum() < 0 || job.getNum() > 23, "Time interval should be " +
                        "greater than 0 less than or equal to 23");
            } else if (job.getTimeType().equals(ConmmonShare.DAY)) {
                AssertUtil.isTrue(job.getNum() < 0 || job.getNum() > 30, "Time interval should be " +
                        "greater than 0 less than or equal to 30");
            } else if (job.getTimeType().equals(ConmmonShare.WEEK)) {
                AssertUtil.isTrue(job.getNum() < 0 || job.getNum() > 4, "Time interval should be " +
                        "greater than 0 less than or equal to 4");
            } else if (job.getTimeType().equals(ConmmonShare.MONTH)) {
                AssertUtil.isTrue(job.getNum() < 0 || job.getNum() > 12, "Time interval should be " +
                        "greater than 0 and less than 12");
            } else {
                log.error("checkNum fail");
            }
        }
    }

    /**
     * checkTarget
     *
     * @param job     job
     * @param sysJobs sysJobs
     * @return String String
     */
    private String checkTarget(SysJob job, List<SysJob> sysJobs) {
        String str = SqlUtil.checkDql(job.getTarget());
        if (StrUtil.isNotEmpty(str)) {
            return str;
        }
        String old = "";
        if (ObjectUtil.isNotEmpty(job.getJobId())) {
            SysJob sysJob = jobMapper.selectJobById(job.getJobId());
            if (ObjectUtil.isNotEmpty(sysJob)) {
                old = sysJob.getTarget().replace(" ", "")
                        .replace(FITE, "")
                        .replace(";", "");
            }
        }
        String sql = job.getTarget().replace(" ", "")
                .replace(FITE, "")
                .replace(";", "");
        if (ConmmonShare.PROM.equals(job.getPlatform())) {
            List<String> prom = getProm(sysJobs);
            if (prom.contains(sql) && job.getIsCreate()) {
                return "Prometheus Metrics Duplicate";
            }
            if (prom.contains(sql) && !job.getIsCreate() && !old.equals(sql)) {
                return "Prometheus Metrics Duplicate";
            }
        } else if (ConmmonShare.ZABBIX.equals(job.getPlatform())) {
            List<String> zabbix = getZabbix(sysJobs);
            if (zabbix.contains(sql) && job.getIsCreate()) {
                return "Zabbix Metrics Duplicate";
            }
            if (zabbix.contains(sql) && !job.getIsCreate() && !old.equals(sql)) {
                return "Zabbix Metrics Duplicate";
            }
        } else {
            List<String> nagios = getNagios(sysJobs);
            if (nagios.contains(sql) && job.getIsCreate()) {
                return "Nagios Indicator Duplicate";
            }
            if (nagios.contains(sql) && !job.getIsCreate() && !old.equals(sql)) {
                return "Nagios Indicator Duplicate";
            }
        }
        return "";
    }

    private List<String> getNagios(List<SysJob> sysJobs) {
        List<String> nagios = sysJobs.stream()
                .filter(item -> item.getPlatform().equals(ConmmonShare.NAGIOS)).map(SysJob::getTarget)
                .collect(Collectors.toList());
        nagios = nagios.stream().map(item -> item.replace(" ", "")
                        .replace(FITE, "")
                        .replace(";", ""))
                .collect(Collectors.toList());
        return nagios;
    }

    private List<String> getZabbix(List<SysJob> sysJobs) {
        List<String> zabbix = sysJobs.stream()
                .filter(item -> item.getPlatform().equals(ConmmonShare.ZABBIX)).map(SysJob::getTarget)
                .collect(Collectors.toList());
        zabbix = zabbix.stream().map(item -> item.replace(" ", "")
                .replace(FITE, "")
                .replace(";", "")).collect(Collectors.toList());
        return zabbix;
    }

    private List<String> getProm(List<SysJob> sysJobs) {
        List<String> prom = sysJobs.stream()
                .filter(item -> item.getPlatform().equals(ConmmonShare.PROM)).map(SysJob::getTarget)
                .collect(Collectors.toList());
        prom = prom.stream().map(item -> item.replace(" ", "")
                        .replace(FITE, "")
                        .replace(";", ""))
                .collect(Collectors.toList());
        return prom;
    }

    /**
     * dealSysJob
     *
     * @param sysConfig sysConfig
     * @param sysJob    sysJob
     * @param list      list
     */
    private void dealSysJob(SysJob sysJob, List<Map<String, Object>> list, SysConfig sysConfig) {
        if (sysJob.getIsCreate()) {
            sysJob.setJobId(MonitorFlake.nextId());
        }
        sysJob.setDataSourceId(sysConfig.getDataSourceId());
        sysJob.setStatus(ScheduleCommon.Status.PAUSE.getValue());
        String target = sysJob.getTarget().replace(";", "");
        sysJob.setTarget(target);
        String jobId = sysJob.getJobId() + "L";
        String invokeTarget = "monitorTask.targetParams('" + sysJob.getTarget() + "'" + "#" + "'"
                + sysJob.getJobName() + "'" + "#" + jobId + "$";
        sysJob.setInvokeTarget(invokeTarget);
        String cronExpression = getCron(sysJob.getNum(), sysJob.getTimeType());
        sysJob.setCronExpression(cronExpression);
        sysJob.setCreateBy("admin");
        if (!sysJob.getTargetGroup().equals(ConmmonShare.SYSTEMTARGET)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = simpleDateFormat.format(new Date());
            try {
                sysJob.setCreateTime(simpleDateFormat.parse(time));
            } catch (ParseException e) {
                log.error("ParseException_dealSysJob");
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dataPattern);
            LocalDateTime ldt = LocalDateTime.now();
            sysJob.setStartTime(ldt.format(dtf));
            sysJob.setTime(System.currentTimeMillis());
        }
        List<String> columnList = getColumnList(list, sysJob);
        sysJob.setColumn(columnList);
    }

    private List<String> getColumnList(List<Map<String, Object>> list, SysJob sysJob) {
        for (Map<String, Object> maps : list) {
            for (Map.Entry<String, Object> entry : maps.entrySet()) {
                if (ObjectUtil.isEmpty(entry.getValue()) && !entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "default");
                }
                if (ObjectUtil.isEmpty(entry.getValue()) && entry.getKey().equalsIgnoreCase("toastsize")) {
                    maps.put(entry.getKey(), "0");
                }
                if (entry.getValue().toString().startsWith(".")) {
                    String value = "0" + entry.getValue().toString();
                    maps.put(entry.getKey(), value);
                }
            }
        }
        List<String> columnList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> arry = list.get(i);
            Map<String, Object> metric = HandleUtils.getMap(arry);
            dealMetric(metric, i);
            for (Map.Entry<String, Object> entry : arry.entrySet()) {
                if (entry.getValue().toString().matches(ISNUM) || entry.getValue().toString().matches(KEXUE)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(entry.getKey()).append("_").append(sysJob.getJobName()).append("_");
                    columnList.add(stringBuilder.toString());
                }
            }
        }
        return columnList;
    }

    private void dealMetric(Map<String, Object> metric, int num) {
        if (CollectionUtil.isEmpty(metric)) {
            metric.put("instance", "node" + num);
        }
    }

    private String getCron(Integer num, String timeType) {
        String cronExpression = "";
        String time = num.toString();
        if (ConmmonShare.SECOND.equals(timeType)) {
            cronExpression = "0/" + time + " * * * * ?";
        } else if (ConmmonShare.MINUTE.equals(timeType)) {
            cronExpression = "0 */" + time + " * * * ?";
        } else if (ConmmonShare.HOUR.equals(timeType)) {
            cronExpression = "* * 0/" + time + " * * ?";
        } else if (ConmmonShare.DAY.equals(timeType)) {
            cronExpression = "0 0 23 * * ?";
        } else if (ConmmonShare.WEEK.equals(timeType)) {
            cronExpression = "0 0 12 ? * WED";
        } else if (ConmmonShare.MONTH.equals(timeType)) {
            cronExpression = "0 15 10 15 * ?";
        } else if (ConmmonShare.YEAR.equals(timeType)) {
            cronExpression = "0 10,44 14 ? 3 WED";
        } else {
            log.error("getCron fail");
        }
        return cronExpression;
    }

    /**
     * getDataSource
     *
     * @param sysConfig sysConfig
     * @return DriverManagerDataSource source
     */
    public DriverManagerDataSource getDataSource(SysConfig sysConfig) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sysConfig.getUrl());
        dataSource.setUsername(sysConfig.getUserName());
        dataSource.setPassword(sysConfig.getPassword());
        return dataSource;
    }

    /**
     * getJdbcTemplate
     *
     * @param dataSource dataSource
     * @return JdbcTemplate JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate(DriverManagerDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * executeSql
     *
     * @param jdbcTemplate jdbcTemplate
     * @param sql          sql
     * @return List<Map < String, Object>> list
     */
    public List<Map<String, Object>> executeSql(JdbcTemplate jdbcTemplate, String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = jdbcTemplate.queryForList(sql);
        } catch (DataAccessException exception) {
            log.error("SysJobServiceImpl_executeSql,{}", exception.getMessage());
        }
        return list;
    }

    /**
     * updateJob
     *
     * @param task task
     * @return MonitorResult MonitorResult
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorResult updateTask(SysJob task) {
        if (task == null) {
            return MonitorResult.error("Task is empty");
        }
        if (task.getTargetGroup().equalsIgnoreCase(ConmmonShare.SYSTEMTARGET) && task.getTime() > ConmmonShare.DEFAULTERNUM) {
            return MonitorResult.error("Group cannot be the system default group");
        }
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && task.getPlatform().equals(ConmmonShare.ZABBIX)) {
            return MonitorResult.error("Please configure the Zabbix data source information first");
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && task.getPlatform().equals(ConmmonShare.NAGIOS)) {
            return MonitorResult.error("Please configure Nagios configuration information first");
        }
        MonitorResult MonitorResult = insertTask(task);
        if (MonitorResult.get("code").equals(HttpStatus.HTTP_OK)) {
            jobMapper.deleteJobByIds(Arrays.asList(task.getJobId()));
            insertTask(task);
        }
        List<Long> publishJobIds = sourceTargetMapper.getPublishJobIds();
        if (publishJobIds.contains(task.getJobId())) {
            startTimeTask(Arrays.asList(task));
        }
        return MonitorResult;
    }

    /**
     * updateSchedulerJob
     *
     * @param job      job
     * @param jobGroup jobGroup
     * @throws SchedulerException SchedulerException
     * @throws TaskException      TaskException
     */
    public void updateSchedulerJob(SysJob job, String jobGroup) {
        Long jobId = job.getJobId();
        JobKey jobKey = MonitorTaskUtils.getMonitorWorkKey(jobId, jobGroup);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException exception) {
            log.error("updateSchedulerJob-->{}", exception.getMessage());
        }
        MonitorTaskUtils.createMonitorJob(scheduler, job);
    }
}
