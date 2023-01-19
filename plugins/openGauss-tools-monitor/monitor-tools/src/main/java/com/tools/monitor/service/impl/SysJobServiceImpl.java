package com.tools.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tools.monitor.common.contant.Constants;
import com.tools.monitor.common.contant.HttpStatus;
import com.tools.monitor.common.contant.ScheduleConstants;
import com.tools.monitor.config.NagiosConfig;
import com.tools.monitor.entity.AjaxResult;
import com.tools.monitor.entity.MFilter;
import com.tools.monitor.entity.ResponseVO;
import com.tools.monitor.entity.SysConfig;
import com.tools.monitor.entity.SysSourceTarget;
import com.tools.monitor.entity.TargetSource;
import com.tools.monitor.entity.zabbix.FailMessage;
import com.tools.monitor.entity.zabbix.ZabbixMessge;
import com.tools.monitor.exception.ParamsException;
import com.tools.monitor.exception.job.TaskException;
import com.tools.monitor.manager.AsyncManager;
import com.tools.monitor.manager.factory.AsyncFactory;
import com.tools.monitor.mapper.SysConfigMapper;
import com.tools.monitor.mapper.SysJobMapper;
import com.tools.monitor.mapper.SysSourceTargetMapper;
import com.tools.monitor.quartz.domain.SysJob;
import com.tools.monitor.quartz.util.CronUtils;
import com.tools.monitor.quartz.util.ScheduleUtils;
import com.tools.monitor.service.ISysJobService;
import com.tools.monitor.service.MonitorService;
import com.tools.monitor.service.SnowFlake;
import com.tools.monitor.util.AssertUtil;
import com.tools.monitor.util.Base64;
import com.tools.monitor.util.HandleUtils;
import com.tools.monitor.util.SqlUtil;
import com.tools.monitor.util.StringUtils;
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
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
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

    private SnowFlake snowFlake = new SnowFlake(11, 11);

    private JdbcTemplate jdbcTemplate;

    public static List<FailMessage> failMessages = new ArrayList<>();

    private static final String ISNUM = "^(\\-|\\+)?\\d+(\\.\\d+)?$";

    private static final String KEXUE = "^[+-]?\\d+\\.?\\d*[Ee][+-]?\\d+$";

    @PostConstruct
    public void init() throws SchedulerException, TaskException {
        scheduler.clear();
        List<SysJob> jobList = jobMapper.selectJobAll();
        for (SysJob job : jobList) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
    }

    /**
     * selectAllJob
     *
     * @param page page
     * @param size size
     * @param job job
     * @return ResponseVO
     */
    @Override
    public ResponseVO selectAllJob(Integer page, Integer size, SysJob job) {
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        addPublish(sysJobs, job);
        sortJob(sysJobs);
        List<String> platForm = sysJobs.stream().map(SysJob::getPlatform).distinct().collect(Collectors.toList());
        List<String> targetGroup = sysJobs.stream().map(SysJob::getTargetGroup).distinct().collect(Collectors.toList());
        sysJobs = filter(sysJobs, job);
        List<SysJob> resulte = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(job) && ObjectUtil.isNotEmpty(job.getIsManagement()) && job.getIsManagement()) {
            resulte = sysJobs;
        } else {
            resulte = sysJobs.stream().skip((page - 1) * size).limit(size).collect(Collectors.toList());
        }
        addIsCanUpdate(resulte);
        List<MFilter> form = new ArrayList<>();
        List<MFilter> group = new ArrayList<>();
        dealMap(platForm, targetGroup, form, group);
        Map<String, Object> myResMap = new HashMap<>();
        myResMap.put("tableData", resulte);
        myResMap.put("platForm", form);
        myResMap.put("targetGroup", group);
        return ResponseVO.pageResponseVO(sysJobs.size(), myResMap);
    }

    @Override
    public AjaxResult getDefaultTarget() {
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        if (CollectionUtil.isNotEmpty(sysJobs)) {
            sysJobs = sysJobs.stream().filter(item -> item.getTargetGroup().equals(Constants.SYSTEMTARGET)).collect(Collectors.toList());
            return AjaxResult.success(sysJobs);
        }
        return AjaxResult.success(new ArrayList<>());
    }

    private void addIsCanUpdate(List<SysJob> resulte) {
        List<Long> publishJobIds = sourceTargetMapper.getPublishJobIds();
        List<SysConfig> sysConfigs = configMapper.getAllConfig();
        List<Long> ids = sysConfigs.stream().map(SysConfig::getDataSourceId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(resulte)) {
            for (SysJob sysJob : resulte) {
                if (ObjectUtil.isNotEmpty(sysJob) && ObjectUtil.isNotEmpty(sysJob.getDataSourceId()) && !ids.contains(sysJob.getDataSourceId())) {
                    sysJob.setDataSourceId(null);
                }
                if (CollectionUtil.isNotEmpty(publishJobIds)) {
                    Long id = publishJobIds.stream().filter(item -> ObjectUtil.isNotEmpty(item) && item.equals(sysJob.getJobId())).findFirst().orElse(null);
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
    }

    private void addPublish(List<SysJob> sysJobs, SysJob job) {
        if (ObjectUtil.isNotEmpty(job) && job.getDataSourceId() != null) {
            List<Long> jobIds = sourceTargetMapper.getJobIdBySourceId(job.getDataSourceId());
            for (SysJob sysJob : sysJobs) {
                if (ObjectUtil.isNotEmpty(sysJob) && CollectionUtil.isNotEmpty(jobIds) && jobIds.contains(sysJob.getJobId())) {
                    sysJob.setIsPbulish(true);
                } else {
                    sysJob.setIsPbulish(false);
                }
            }
        }
    }

    private List<SysJob> filter(List<SysJob> sysJobs, SysJob job) {
        if (job != null) {
            if (StrUtil.isNotEmpty(job.getPlatform())) {
                List<String> platForm = Arrays.asList(job.getPlatform().split(","));
                sysJobs = sysJobs.stream().filter(item -> platForm.contains(item.getPlatform())).collect(Collectors.toList());
            }
            if (StrUtil.isNotEmpty(job.getTargetGroup())) {
                List<String> targetGroup = Arrays.asList(job.getTargetGroup().split(","));
                sysJobs = sysJobs.stream().filter(item -> targetGroup.contains(item.getTargetGroup())).collect(Collectors.toList());
            }
            if (CollectionUtil.isNotEmpty(job.getTimeInterval()) && job.getTimeInterval().size() == 2) {
                List<String> strings = job.getTimeInterval();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    Date start = simpleDateFormat.parse(strings.get(0));
                    Date end = simpleDateFormat.parse(strings.get(1).substring(0,10) + " " + "23:59:59");
                    sysJobs = sysJobs.stream().filter(item -> !item.getCreateTime().before(start) && !item.getCreateTime().after(end)).collect(Collectors.toList());
                } catch (ParseException e) {
                    log.error("simpleDateFormat_parse,{}", e.getMessage());
                }
            }
            return sysJobs;
        } else {
            return sysJobs;
        }
    }

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

    private void sortJob(List<SysJob> sysJobs) {
        sysJobs.sort(Comparator.comparing(SysJob::getTime).reversed());
    }

    /**
     * batchPublish
     *
     * @param targetSource
     * @return
     * @throws SchedulerException
     */
    @Override
    public AjaxResult batchPublish(TargetSource targetSource) throws SchedulerException {
        if (ObjectUtil.isEmpty(targetSource) || CollectionUtil.isEmpty(targetSource.getDataSourceId()) || CollectionUtil.isEmpty(targetSource.getJobIds())) {
            return AjaxResult.error("发布信息不能为空");
        }
        List<SysSourceTarget> list = new ArrayList<>();
        List<Long> dateSource = targetSource.getDataSourceId();
        List<Long> jobIds = targetSource.getJobIds().stream().distinct().collect(Collectors.toList());
        List<SysJob> sysJobs = jobMapper.selectBatchJobByIds(jobIds);
        List<SysJob> zabbix = sysJobs.stream().filter(item -> Constants.ZABBIX.equals(item.getPlatform())).collect(Collectors.toList());
        List<SysJob> nagios = sysJobs.stream().filter(item -> Constants.NAGIOS.equals(item.getPlatform())).collect(Collectors.toList());
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && CollectionUtil.isNotEmpty(zabbix)) {
            return AjaxResult.error("请先配置Zabbix数据源信息");
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && CollectionUtil.isNotEmpty(nagios)) {
            return AjaxResult.error("请先配置Nagios配置信息");
        }
        for (Long id : dateSource) {
            SysSourceTarget sourceTarget = new SysSourceTarget();
            sourceTarget.setDataSourceId(id);
            sourceTarget.setJobIds(targetSource.getJobIds());
            list.add(sourceTarget);
        }
        for (SysSourceTarget sourceTarget : list) {
            singlePublish(sourceTarget, sysJobs, zabbix, zabbixConfig, nagios, nagiosConfig);
        }
        return AjaxResult.success("批量发布成功");
    }

    /**
     * singlePublish
     *
     * @param sourceTarget
     * @param sysJobs
     * @param zabbix
     * @param zabbixConfig
     * @param nagios
     * @param nagiosConfig
     * @return
     * @throws SchedulerException
     */
    public AjaxResult singlePublish(SysSourceTarget sourceTarget, List<SysJob> sysJobs, List<SysJob> zabbix, SysConfig zabbixConfig, List<SysJob> nagios, SysConfig nagiosConfig) throws SchedulerException {
        SysConfig sysConfig = configMapper.getConfigByid(sourceTarget.getDataSourceId());
        if (sysConfig == null) {
            return AjaxResult.error("没有该主机信息");
        }
        List<Long> oldJobIds = sourceTargetMapper.getJobIdBySourceId(sourceTarget.getDataSourceId());
        List<SysJob> oldSysJob = jobMapper.selectBatchJobByIds(oldJobIds);
        List<SysJob> newSysjob = jobMapper.selectBatchJobByIds(sourceTarget.getJobIds());
        oldSysJob.removeAll(newSysjob);
        stopTimeTask(oldSysJob);
        sourceTargetMapper.save(sourceTarget);
        List<String> remove = dealOldSysJob(oldSysJob, sysConfig.getConnectName());
        AsyncManager.me().execute(AsyncFactory.removeRegistry(remove));
        List<SysJob> otherSysJob = sourceTargetMapper.getMoreThanOneSource(oldSysJob);
        sysJobs.addAll(otherSysJob);
        sysJobs.removeAll(nagios);
        startTimeTask(sysJobs);
        if (CollectionUtil.isNotEmpty(zabbix)) {
            AsyncManager.me().execute(AsyncFactory.recordZabbix(zabbix, sysConfig, zabbixConfig));
        }
        if (CollectionUtil.isNotEmpty(nagios)) {
            AsyncManager.me().execute(AsyncFactory.recordNagios(nagios, sysConfig, nagiosConfig));
        }
        if (CollectionUtil.isNotEmpty(nagios)) {
            AsyncManager.me().execute(AsyncFactory.executeNagios(nagios));
        }
        return AjaxResult.success("发布成功");
    }

    public void publishNagios(List<SysJob> nagios, SysConfig sysConfig, SysConfig nagiosConfig) {
        Map<String, Object> all = new HashMap<>();
        sysConfig.setPassword(Base64.decode(sysConfig.getPassword()));
        for (SysJob sysJob : nagios) {
            DriverManagerDataSource dataSource = getDataSource(sysConfig);
            jdbcTemplate = new JdbcTemplate(dataSource);
            List<Map<String, Object>> list = executeSql(jdbcTemplate, sysJob.getTarget());
            String name = sysConfig.getConnectName();
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
            Map<String, Object> nagiosMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> arry = list.get(i);
                Map<String, Object> metric = HandleUtils.getMap(arry);
                dealMetric(metric, i);
                for (Map.Entry<String, Object> entry : arry.entrySet()) {
                    if (entry.getValue().toString().matches(ISNUM) || entry.getValue().toString().matches(KEXUE)) {
                        nagiosMap.put(entry.getKey() + "_" + sysJob.getJobName() + "_" + name + "_" + i, entry.getValue());
                        all.putAll(nagiosMap);
                    }
                }
            }
        }
        nagiosService.setConfig(all, nagiosConfig);
    }

    public void startNagios(List<SysJob> nagios) {
        try {
            Thread.sleep(NagiosConfig.getDelayTime());
        } catch (InterruptedException e) {
            log.error("startNagios_{}", e.getMessage());
        }
        startTimeTask(nagios);
    }

    @Override
    public AjaxResult singlePublishPause(SysSourceTarget sourceTarget) throws SchedulerException {
        SysConfig sysConfig = configMapper.getConfigByid(sourceTarget.getDataSourceId());
        if (sysConfig == null) {
            return AjaxResult.error("没有该主机信息");
        }
        List<Long> jobs = sourceTarget.getJobIds();
        List<SysJob> sysJobs = jobMapper.selectBatchJobByIds(jobs);
        stopTimeTask(sysJobs);
        List<String> remove = dealOldSysJob(sysJobs, sysConfig.getConnectName());
        AsyncManager.me().execute(AsyncFactory.removeRegistry(remove));
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
        return AjaxResult.success("停止发布成功");
    }


    private int startTimeTask(List<SysJob> sysJobs) {
        int num = 0;
        try {
            if (CollectionUtil.isNotEmpty(sysJobs)) {
                for (SysJob sysJob : sysJobs) {
                    AsyncManager.me().execute(AsyncFactory.executeOne(sysJob.getTarget(), sysJob.getJobName(), sysJob.getJobId()));
                    sysJob.setStatus(ScheduleConstants.Status.NORMAL.getValue());
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
                    sysJob.setStatus(ScheduleConstants.Status.PAUSE.getValue());
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
        if (CollectionUtil.isNotEmpty(sysJobs)) {
            for (SysJob sysJob : sysJobs) {
                if (ObjectUtil.isNotEmpty(sysJob)) {
                    List<String> column = sysJob.getColumn();
                    if (CollectionUtil.isNotEmpty(column)) {
                        column = column.stream().map(item -> item + name).collect(Collectors.toList());
                        remove.addAll(column);
                    }
                }
            }
        }
        return remove;
    }

    /**
     * pauseJob
     *
     * @param job 调度信息
     * @return
     * @throws SchedulerException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = jobMapper.updateJob(job);
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        return rows;
    }

    /**
     * resumeJob
     *
     * @param job
     * @return
     * @throws SchedulerException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = "DEFAULT";
        int rows = jobMapper.updateJob(job);
        scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        return rows;
    }

    /**
     * deleteJob
     *
     * @param job
     * @return
     * @throws SchedulerException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        Boolean isDelete = jobMapper.deleteJobByIds(Arrays.asList(jobId));
        if (isDelete) {
            scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return isDelete;
    }

    /**
     * deleteJobByIds
     *
     * @param jobIds
     * @throws SchedulerException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
        for (Long jobId : jobIds) {
            SysJob job = jobMapper.selectJobById(jobId);
            deleteJob(job);
            AsyncManager.me().execute(AsyncFactory.removeJobId(jobId));
            if (!job.getPlatform().equals(Constants.NAGIOS)) {
                List<Long> sourceId = sourceTargetMapper.getSourceIdByJobId(jobId);
                List<SysConfig> sysConfigs = configMapper.getBatchById(sourceId);
                if (CollectionUtil.isNotEmpty(sysConfigs)) {
                    for (SysConfig sysConfig : sysConfigs) {
                        if (ObjectUtil.isNotEmpty(job) && ObjectUtil.isNotEmpty(sysConfig) && ObjectUtil.isNotEmpty(sysConfig.getConnectName())) {
                            List<String> remove = dealOldSysJob(Arrays.asList(job), sysConfig.getConnectName());
                            AsyncManager.me().execute(AsyncFactory.removeRegistry(remove));
                        }
                    }
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
                return ResponseVO.successResponseVO("检测到指标已在" + nameList + "实例中发布,将批量从实例中删除,是否继续?");
            }
        }
        return ResponseVO.successResponseVO("");
    }

    @Override
    public ResponseVO getResulter() {
        if (CollectionUtil.isNotEmpty(failMessages)) {
            return ResponseVO.successResponseVO(failMessages);
        } else {
            return ResponseVO.successResponseVO("恭喜您！全部发布成功");
        }

    }

    @Override
    public AjaxResult batchPublishPause(TargetSource targetSource) throws SchedulerException {
        if (ObjectUtil.isEmpty(targetSource) || CollectionUtil.isEmpty(targetSource.getDataSourceId()) || CollectionUtil.isEmpty(targetSource.getJobIds())) {
            return AjaxResult.error("发布信息不能为空");
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
        return AjaxResult.success("批量发布停止成功");
    }

    @Override
    public ResponseVO selectGroup() {
        return ResponseVO.successResponseVO(jobMapper.getGroup());
    }

    public void executeZabbix(List<SysJob> zabbix, SysConfig sysConfig, SysConfig zabbixConfig) {
        synchronized (this) {
            String name = sysConfig.getConnectName();
            JdbcTemplate openGaussTemplate = commonService.getTem(sysConfig);
            if (ObjectUtil.isNotEmpty(zabbixConfig) && StrUtil.isNotBlank(zabbixConfig.getContainerPort()) && StrUtil.isNotBlank(zabbixConfig.getContainerIp())) {
                ZabbixMessge zabbixMessge = zabbixService.insertNecessary(name, zabbixConfig.getContainerIp(), zabbixConfig.getContainerPort());
                zabbixService.insertTarget(zabbixMessge, zabbix, openGaussTemplate);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob properties = jobMapper.selectJobById(job.getJobId());
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
        scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup), dataMap);
    }

    @Override
    public AjaxResult insertJob(SysJob job) throws SchedulerException, TaskException {
        if (job.getTargetGroup().equalsIgnoreCase(Constants.SYSTEMTARGET) && !job.getIsCreate()) {
            checkNum(job);
            String cronExpression;
            cronExpression = getCron(job.getNum(), job.getTimeType());
            job.setCronExpression(cronExpression);
            int rows = jobMapper.insertJob(job);
            updateSchedulerJob(job, job.getJobGroup());
            return AjaxResult.success("修改成功");
        }
        if (ObjectUtil.isEmpty(job) || ObjectUtil.isEmpty(job.getDataSourceId())) {
            return AjaxResult.error("验证主机信息不能为空");
        }
        if (job.getIsCreate() && job.getTargetGroup().equalsIgnoreCase(Constants.SYSTEMTARGET)) {
            return AjaxResult.error("自定义分组不能是系统默认分组,请更换分组名称");
        }
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && job.getPlatform().equals(Constants.ZABBIX) && !job.getTargetGroup().equalsIgnoreCase(Constants.SYSTEMTARGET)) {
            return AjaxResult.error("请先配置Zabbix数据源信息");
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && job.getPlatform().equals(Constants.NAGIOS) && !job.getTargetGroup().equalsIgnoreCase(Constants.SYSTEMTARGET)) {
            return AjaxResult.error("请先配置Nagios配置信息");
        }
        List<SysJob> sysJobs = jobMapper.selectJobAll();
        Integer max = 0;
        if (CollectionUtil.isNotEmpty(sysJobs)) {
            List<String> jobName = sysJobs.stream().map(SysJob::getJobName).collect(Collectors.toList());
            List<String> finalName = jobName.stream().map(item -> item.substring(3)).collect(Collectors.toList());
            List<Integer> codesInteger = finalName.stream().map(Integer::parseInt).collect(Collectors.toList());
            max = codesInteger.stream().reduce(Integer::max).get();
        }
        max = max + 1;
        if (job.getIsCreate()) {
            job.setJobName(job.getJobName() + max);
        }
        String ajaxResult = checkTarget(job, sysJobs);
        if (StrUtil.isNotBlank(ajaxResult)) {
            return AjaxResult.error(ajaxResult);
        }
        checkNum(job);
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        SysConfig sysConfig;
        List<SysConfig> sysConfigs = configMapper.getAllConfig().stream().filter(item -> item.getPlatform().equals(Constants.PROM)).collect(Collectors.toList());
        sysConfig = sysConfigs.stream().filter(item -> ObjectUtil.isNotEmpty(item.getDataSourceId()) && item.getDataSourceId().equals(job.getDataSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(sysConfig)) {
            sysConfigs = (List<SysConfig>) monitorService.getDataSourceList(1, 10).getData();
            sysConfig = sysConfigs.stream().findFirst().orElse(null);
        }
        if (sysConfig == null) {
            return AjaxResult.error("请先配置数据源");
        }
        sysConfig.setPassword(Base64.decode(sysConfig.getPassword()));
        DriverManagerDataSource dataSource = getDataSource(sysConfig);
        jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> list = null;
        try {
            list = commonService.executeSql(jdbcTemplate, job.getTarget());
        } catch (Exception e) {
           return AjaxResult.error(e.getMessage());
        }
        if (CollectionUtil.isEmpty(list) && ObjectUtil.isNotEmpty(job.getIsFalse()) && !job.getIsFalse()) {
            return AjaxResult.target("无法生成指标");
        }
        dealSysJob(job, list);
        job.setDataSourceId(sysConfig.getDataSourceId());
        int rows = jobMapper.insertJob(job);
        if (rows > 0) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return AjaxResult.success("保存成功");
    }

    private void checkNum(SysJob job) {
        if (ObjectUtil.isNotEmpty(job) && ObjectUtil.isNotEmpty(job.getNum()) && ObjectUtil.isNotEmpty(job.getTimeType())) {
            if (job.getTimeType().equals(Constants.SECOND) || job.getTimeType().equals(Constants.MINUTE)) {
                AssertUtil.isTrue(0 > job.getNum() || job.getNum() > 59, "时间间隔应大于0小于等于59");
            } else if (job.getTimeType().equals(Constants.HOUR)) {
                AssertUtil.isTrue(0 > job.getNum() || job.getNum() > 23, "时间间隔应大于0小于等于23");
            } else if (job.getTimeType().equals(Constants.DAY)) {
                AssertUtil.isTrue(0 > job.getNum() || job.getNum() > 30, "时间间隔应大于0小于等于30");
            } else if (job.getTimeType().equals(Constants.WEEK)) {
                AssertUtil.isTrue(0 > job.getNum() || job.getNum() > 4, "时间间隔应大于0小于等于4");
            } else if (job.getTimeType().equals(Constants.MONTH)) {
                AssertUtil.isTrue(0 > job.getNum() || job.getNum() > 12, "时间间隔应大于0小于12");
            }
        }
    }


    private String checkTarget(SysJob job, List<SysJob> sysJobs) {
        String str = SqlUtil.checkDql(job.getTarget());
        if(StrUtil.isNotEmpty(str)) {
            return str;
        }
        String sql = job.getTarget().replace(" ", "").replace("\n", "").replace(";", "");
        if (Constants.PROM.equals(job.getPlatform())) {
            List<String> prom = sysJobs.stream().filter(item -> item.getPlatform().equals(Constants.PROM)).map(SysJob::getTarget).collect(Collectors.toList());
            prom = prom.stream().map(item -> item.replace(" ", "").replace("\n", "").replace(";", "")).collect(Collectors.toList());
            if (prom.contains(sql)) {
                return "Prometheus指标重复";
            }
        } else if (Constants.ZABBIX.equals(job.getPlatform())) {
            List<String> zabbix = sysJobs.stream().filter(item -> item.getPlatform().equals(Constants.ZABBIX)).map(SysJob::getTarget).collect(Collectors.toList());
            zabbix = zabbix.stream().map(item -> item.replace(" ", "").replace("\n", "").replace(";", "")).collect(Collectors.toList());
            if (zabbix.contains(sql)) {
                return "Zabbix指标重复";
            }
        } else {
            List<String> nagios = sysJobs.stream().filter(item -> item.getPlatform().equals(Constants.NAGIOS)).map(SysJob::getTarget).collect(Collectors.toList());
            nagios = nagios.stream().map(item -> item.replace(" ", "").replace("\n", "").replace(";", "")).collect(Collectors.toList());
            if (nagios.contains(sql)) {
                return "Nagios指标重复";
            }
        }
        return "";
    }

    private void checkCron(SysJob job) {
        if (!CronUtils.isValid(job.getCronExpression())) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            throw new ParamsException("新增任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
    }

    private void dealSysJob(SysJob sysJob, List<Map<String, Object>> list) {
        if (sysJob.getIsCreate()) {
            sysJob.setJobId(snowFlake.nextId());
        }
        String target = sysJob.getTarget().replace(";", "");
        sysJob.setTarget(target);
        String jobId = sysJob.getJobId() + "L";
        String invokeTarget = "monitorTask.targetParams('" + sysJob.getTarget() + "'" + "#" + "'" + sysJob.getJobName() + "'" + "#" + jobId + "$";
        sysJob.setInvokeTarget(invokeTarget);
        String cronExpression;
        cronExpression = getCron(sysJob.getNum(), sysJob.getTimeType());
        sysJob.setCronExpression(cronExpression);
        sysJob.setCreateBy("admin");
        if (!sysJob.getTargetGroup().equals(Constants.SYSTEMTARGET)) {
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
        List<String> columnList = new ArrayList<>();
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

    private void dealMetric(Map<String, Object> metric, int i) {
        if (CollectionUtil.isEmpty(metric)) {
            metric.put("instance", "node" + i);
        }
    }

    private String getCron(Integer num, String timeType) {
        String cronExpression = "";
        String time = num.toString();
        if (Constants.SECOND.equals(timeType)) {
            cronExpression = "0/" + time + " * * * * ?";
        } else if (Constants.MINUTE.equals(timeType)) {
            cronExpression = "0 */" + time + " * * * ?";
        } else if (Constants.HOUR.equals(timeType)) {
            cronExpression = "* * 0/" + time + " * * ?";
        } else if (Constants.DAY.equals(timeType)) {
            cronExpression = "0 0 23 * * ?";
        } else if (Constants.WEEK.equals(timeType)) {
            cronExpression = "0 0 12 ? * WED";
        } else if (Constants.MONTH.equals(timeType)) {
            cronExpression = "0 15 10 15 * ?";
        } else if (Constants.YEAR.equals(timeType)) {
            cronExpression = "0 10,44 14 ? 3 WED";
        }
        return cronExpression;
    }

    public DriverManagerDataSource getDataSource(SysConfig sysConfig) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sysConfig.getUrl());
        dataSource.setUsername(sysConfig.getUserName());
        dataSource.setPassword(sysConfig.getPassword());
        return dataSource;
    }

    public JdbcTemplate getJdbcTemplate(DriverManagerDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> executeSql(JdbcTemplate jdbcTemplate, String sql) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = jdbcTemplate.queryForList(sql);
        } catch (Exception exception) {
            log.error("SysJobServiceImpl_executeSql,{}", exception.getMessage());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateJob(SysJob job) throws SchedulerException, TaskException {
        if (job == null) {
            return AjaxResult.error("任务为空");
        }
        if (job.getTargetGroup().equalsIgnoreCase(Constants.SYSTEMTARGET) && job.getTime() > Constants.DEFAULTERNUM) {
            return AjaxResult.error("分组不能是系统默认分组");
        }
        SysConfig zabbixConfig = configMapper.getZabbixConfig();
        SysConfig nagiosConfig = configMapper.getNagiosConfig();
        if (ObjectUtil.isEmpty(zabbixConfig) && job.getPlatform().equals(Constants.ZABBIX)) {
            return AjaxResult.error("请先配置Zabbix数据源信息");
        }
        if (ObjectUtil.isEmpty(nagiosConfig) && job.getPlatform().equals(Constants.NAGIOS)) {
            return AjaxResult.error("请先配置Nagios配置信息");
        }
        AjaxResult ajaxResult = insertJob(job);
        if (ajaxResult.get("code").equals(HttpStatus.SUCCESS)) {
            jobMapper.deleteJobByIds(Arrays.asList(job.getJobId()));
            insertJob(job);
        }
        List<Long> publishJobIds = sourceTargetMapper.getPublishJobIds();
        if (publishJobIds.contains(job.getJobId())) {
            startTimeTask(Arrays.asList(job));
        }
        return ajaxResult;
    }

    public void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getJobId();
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}
