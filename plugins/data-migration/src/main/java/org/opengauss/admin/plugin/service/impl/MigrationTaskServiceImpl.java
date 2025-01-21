/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.plugin.constants.TaskAlertConstants;
import org.opengauss.admin.plugin.context.MigrationTaskContext;
import org.opengauss.admin.plugin.domain.*;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.opengauss.admin.plugin.enums.MigrationMode;
import org.opengauss.admin.plugin.enums.ProcessType;
import org.opengauss.admin.plugin.enums.TaskOperate;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.enums.ToolsConfigEnum;
import org.opengauss.admin.plugin.handler.MigrationRecoveryHandler;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskMapper;
import org.opengauss.admin.plugin.service.*;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.FullCheckParam;
import org.opengauss.admin.plugin.vo.ProcessStatus;
import org.opengauss.admin.plugin.vo.TaskProcessStatus;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.opengauss.admin.plugin.constants.ToolsParamsLog.NEW_DESC_PREFIX;
import static org.opengauss.admin.plugin.constants.ToolsParamsLog.NEW_PARAM_PREFIX;

import javax.annotation.Resource;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskServiceImpl extends ServiceImpl<MigrationTaskMapper, MigrationTask> implements MigrationTaskService {
    /**
     * special chars
     */
    private static final char[] SPECIAL_CHARS = {'\\', '+', '-', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}',
            '~', '*', '?', '>', '<', '|', '&', ';', '/', '.', '$'};
    private static final float SUB_PROCESS = 0.05f;

    /**
     * The constants AES secretKey
     */
    private static final String AES_SECRETKEY = "yykczOWf3hoHsOn6ADZcQKpAlck0ZRK12T9N3sf0WB4=";
    private static final List<Integer> INCREMENTAL_STATUS = Arrays.asList(TaskStatus.INCREMENTAL_START.getCode(),
        TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskStatus.INCREMENTAL_PAUSE.getCode(),
        TaskStatus.INCREMENTAL_STOPPED.getCode());

    private static final List<Integer> REVERSE_STATUS = Arrays.asList(TaskStatus.REVERSE_START.getCode(),
        TaskStatus.REVERSE_RUNNING.getCode(), TaskStatus.REVERSE_PAUSE.getCode(), TaskStatus.REVERSE_STOP.getCode());

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @Autowired
    private MigrationTaskMapper migrationTaskMapper;

    @Autowired
    private MigrationTaskParamService migrationTaskParamService;

    @Autowired
    private MigrationTaskHostRefService migrationTaskHostRefService;

    @Autowired
    private MigrationTaskGlobalParamService migrationTaskGlobalParamService;

    @Autowired
    private MigrationTaskExecResultDetailService migrationTaskExecResultDetailService;

    @Autowired
    private MigrationTaskStatusRecordService migrationTaskStatusRecordService;

    @Autowired
    private MigrationTaskOperateRecordService migrationTaskOperateRecordService;

    @Value("${migration.taskOfflineSchedulerIntervalsMillisecond}")
    private Long taskOfflineSchedulerIntervalsMillisecond;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MigrationHostPortalInstallHostService migrationHostPortalInstallHostService;

    @Autowired
    private TbMigrationTaskGlobalToolsParamService toolsParamService;

    @Autowired
    private MigrationMainTaskService migrationMainTaskService;

    @Autowired
    private MigrationMqInstanceService migrationMqInstanceService;
    @Resource
    private MigrationRecoveryHandler migrationRecoveryHandler;
    @Resource
    private MigrationTaskCheckProgressService migrationTaskCheckProgressService;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;
    private Map<Integer, MigrationHostPortalInstall> installMap = new ConcurrentHashMap<>();

    @Override
    public void initMigrationTaskCheckProgressMonitor() {
        // 启动加载历史任务
        migrationTaskCheckProgressMonitor(list());
        // 定时任务监控启动后未完成任务状态
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // 迁移任务阶段在 FULL_CHECK_START 和 FULL_CHECK_FINISH 之间的任务，需要检查进度
            List<MigrationTask> checkTaskList = list();
            for (MigrationTask migrationTask : checkTaskList) {
                if (migrationTask.getExecStatus() > TaskStatus.FULL_START.getCode()) {
                    migrationTaskCheckProgressService.refreshCheckProgress(migrationTask.getId(),
                        migrationTask.getExecStatus());
                }
            }
        }, 5, 1, TimeUnit.SECONDS);
    }

    private void migrationTaskCheckProgressMonitor(List<MigrationTask> list) {
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(checking -> {
                if (checking.getExecStatus() >= TaskStatus.FULL_START.getCode()) {
                    MigrationTaskContext context = new MigrationTaskContext();
                    context.setId(checking.getId());
                    if (installMap.containsKey(checking.getId())) {
                        context.setInstallHost(installMap.get(checking.getId()));
                    } else {
                        MigrationHostPortalInstall portalInstall = migrationHostPortalInstallHostService.getOneByHostId(
                            checking.getRunHostId());
                        installMap.put(checking.getId(), portalInstall);
                        context.setInstallHost(portalInstall);
                    }
                    context.setMigrationTask(checking);
                    migrationTaskCheckProgressService.startCheckProgress(context);
                }
            });
        }
    }

    /**
     * Query the sub task page list by mainTaskId
     *
     * @param mainTaskId mainTaskId
     * @return MigrationTask
     */
    @Override
    public IPage<MigrationTask> selectList(IPage<MigrationTask> page, Integer mainTaskId) {
        IPage<MigrationTask> taskPage = migrationTaskMapper.selectTaskPage(page, mainTaskId);
        List<MigrationTask> tasks = taskPage.getRecords();
        tasks.forEach(task -> task.setCheckDataLevelingAndIncrementFinish(
                migrationMainTaskService.checkDataLevelingAndIncrementFinish(task.getId())));
        return taskPage;
    }

    @Override
    public void deleteByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getMainTaskId, mainTaskId);
        List<Integer> ids = list(queryWrapper).stream().map(MigrationTask::getId).collect(Collectors.toList());
        removeBatchByIds(ids);
        migrationTaskExecResultDetailService.deleteByTaskIds(ids);
        migrationTaskStatusRecordService.deleteByTaskIds(ids);
        migrationTaskOperateRecordService.deleteByTaskIds(ids);
        migrationTaskCheckProgressService.deleteByTaskIds(ids);
        ids.forEach(id -> {
            if (installMap.containsKey(id)) {
                installMap.remove(id);
            }
        });
    }

    @Override
    public List<MigrationTask> listByMainTaskId(Integer mainTaskId) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getMainTaskId, mainTaskId);
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getTaskDetailById(Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        MigrationTask task = getTaskInfo(taskId);
        result.put("task", task);
        setProcessExecDetails(task, result);
        MigrationTaskExecResultDetail fullProcess = null;
        Object fullProcessObj = result.get("fullProcess");
        if (fullProcessObj instanceof MigrationTaskExecResultDetail) {
            fullProcess = (MigrationTaskExecResultDetail) fullProcessObj;
        }
        if (fullProcess != null && StringUtils.isNotBlank(fullProcess.getExecResultDetail())) {
            Map<String, Object> processMap = JSON.parseObject(fullProcess.getExecResultDetail());
            result.put("tableCounts",
                new FullMigrationSubProcessCounter(processMap, FullMigrationDbObjEnum.TABLE.getObjectType()));
            result.put("viewCounts",
                new FullMigrationSubProcessCounter(processMap, FullMigrationDbObjEnum.VIEW.getObjectType()));
            result.put("funcCounts",
                new FullMigrationSubProcessCounter(processMap, FullMigrationDbObjEnum.FUNCTION.getObjectType()));
            result.put("triggerCounts",
                new FullMigrationSubProcessCounter(processMap, FullMigrationDbObjEnum.TRIGGER.getObjectType()));
            result.put("produceCounts",
                new FullMigrationSubProcessCounter(processMap, FullMigrationDbObjEnum.PROCEDURE.getObjectType()));
            FullMigrationProcessCounter processCounter = new FullMigrationProcessCounter(processMap);
            result.put("totalWaitCount", processCounter.getTotalWaitCount());
            result.put("totalRunningCount", processCounter.getTotalRunningCount());
            result.put("totalSuccessCount", processCounter.getTotalSuccessCount());
            result.put("totalErrorCount", processCounter.getTotalErrorCount());
        }
        List<String> logPaths = new ArrayList<>();
        if (!task.getExecStatus().equals(TaskStatus.NOT_RUN.getCode())) {
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
                task.getRunHostId());
            logPaths = PortalHandle.getPortalLogPath(installHost.getHost(), installHost.getPort(),
                installHost.getRunUser(), encryptionUtils.decrypt(installHost.getRunPassword()),
                installHost.getInstallPath(), task);
        }
        result.put("logs", logPaths);
        return result;
    }

    private MigrationTask getTaskInfo(Integer taskId) {
        MigrationTask task = getById(taskId);
        task.setCurrentTime(new Date());
        String maskedPass = "********";
        task.setSourceDbPass(maskedPass);
        task.setTargetDbPass(maskedPass);
        return task;
    }

    private void setProcessExecDetails(MigrationTask task, Map<String, Object> result) {
        MigrationTaskExecResultDetail fullProcess = null;
        MigrationTaskExecResultDetail incrementalProcess = null;
        MigrationTaskExecResultDetail reverseProcess = null;
        MigrationTaskExecResultDetail dataCheckProcess = null;
        if (!task.getExecStatus().equals(TaskStatus.MIGRATION_FINISH.getCode()) && !task.getExecStatus()
            .equals(TaskStatus.FULL_CHECK_FINISH.getCode())) {
            Map<String, Object> getResult = getSingleTaskStatusAndProcessByProtal(task);
            fullProcess = generateProcessDetail(getResult, "fullProcess");
            if (task.getMigrationModelId().equals(MigrationMode.ONLINE.getCode())) {
                incrementalProcess = generateProcessDetail(getResult, "incrementalProcess");
                reverseProcess = generateProcessDetail(getResult, "reverseProcess");
            }
            dataCheckProcess = generateProcessDetail(getResult, "dataCheckProcess");
        }
        if (fullProcess == null || fullProcess.isEmpty()) {
            fullProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(task.getId(),
                ProcessType.FULL.getCode());
        }
        if (dataCheckProcess == null || dataCheckProcess.isEmpty()) {
            dataCheckProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(task.getId(),
                ProcessType.DATA_CHECK.getCode());
        }
        if (task.getMigrationModelId().equals(MigrationMode.ONLINE.getCode())) {
            if (incrementalProcess == null || incrementalProcess.isEmpty()) {
                incrementalProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(task.getId(),
                    ProcessType.INCREMENTAL.getCode());
            }
            if (reverseProcess == null || reverseProcess.isEmpty()) {
                reverseProcess = migrationTaskExecResultDetailService.getByTaskIdAndProcessType(task.getId(),
                    ProcessType.REVERSE.getCode());
            }
        }
        result.put("fullProcess", fullProcess);
        if (dataCheckProcess != null && StringUtils.isNotBlank(dataCheckProcess.getExecResultDetail())) {
            result.put("dataCheckProcess", dataCheckProcess);
        }
        if (task.getMigrationModelId().equals(MigrationMode.ONLINE.getCode())) {
            result.put("incrementalProcess", incrementalProcess);
            result.put("reverseProcess", reverseProcess);
            List<MigrationTaskStatusRecord> migrationTaskStatusRecords
                = migrationTaskStatusRecordService.selectByTaskId(task.getId());
            // migrationTaskStatusRecords  存在可能为空情况，导致接口查询失败。添加过滤条件，过滤掉operateType为空的记录
            Map<Integer, List<MigrationTaskStatusRecord>> recordMap = migrationTaskStatusRecords.stream()
                .filter(record -> Objects.nonNull(record.getOperateType()))
                .collect(Collectors.groupingBy(MigrationTaskStatusRecord::getOperateType));
            result.put("statusRecords", recordMap);
        }
    }

    private MigrationTaskExecResultDetail generateProcessDetail(Map<String, Object> detailsMap, String processKey) {
        return MigrationTaskExecResultDetail.builder().execResultDetail(MapUtil.getStr(detailsMap, processKey)).build();
    }

    @Override
    public Map<String, Object> getSingleTaskStatusAndProcessByProtal(MigrationTask t) {
        Map<String, Object> result = new HashMap<>();
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(t.getRunHostId());
        String password = encryptionUtils.decrypt(installHost.getRunPassword());
        String portalStatus = PortalHandle.getPortalStatus(installHost.getHost(), installHost.getPort(),
            installHost.getRunUser(), password, installHost.getInstallPath(), t);
        log.debug("get portal stauts content: {}, subTaskId: {}", portalStatus, t.getId());
        if (org.opengauss.admin.common.utils.StringUtils.isNotEmpty(portalStatus)) {
            List<Map<String, Object>> statusList = (List<Map<String, Object>>) JSON.parse(portalStatus);
            List<Map<String, Object>> statusResultList = statusList.stream()
                .sorted(Comparator.comparing(m -> MapUtil.getLong(m, "timestamp")))
                .collect(Collectors.toList());
            Map<String, Object> lastStatus = statusResultList.get(statusResultList.size() - 1);
            Integer state = MapUtil.getInt(lastStatus, "status");
            MigrationTask update = MigrationTask.builder().id(t.getId()).build();
            migrationTaskStatusRecordService.saveTaskRecord(t.getId(), statusResultList);
            BigDecimal migrationProcess = new BigDecimal(0);
            String portalFullProcess = PortalHandle.getPortalFullProcess(installHost.getHost(), installHost.getPort(),
                installHost.getRunUser(), password, installHost.getInstallPath(), t);
            log.debug("get portal full process content: {}, subTaskId: {}", portalFullProcess, t.getId());
            if (StringUtils.isNotBlank(portalFullProcess)) {
                migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portalFullProcess.trim(),
                    ProcessType.FULL.getCode());
                migrationProcess = calculateFullMigrationProgress(portalFullProcess);
            }
            result.put("fullProcess", portalFullProcess);
            String portalDataCheckProcess = PortalHandle.getPortalDataCheckProcess(installHost.getHost(),
                installHost.getPort(), installHost.getRunUser(), password, installHost.getInstallPath(), t);
            log.debug("get portal data check process content: {}, subTaskId: {}", portalDataCheckProcess, t.getId());
            if (state >= TaskStatus.FULL_FINISH.getCode()) {
                migrationProcess = new BigDecimal(1);
            }
            update.setMigrationProcess(migrationProcess.setScale(2, RoundingMode.UP).toPlainString());
            if (StringUtils.isNotBlank(portalDataCheckProcess)) {
                migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portalDataCheckProcess.trim(),
                    ProcessType.DATA_CHECK.getCode());
                result.put("dataCheckProcess", portalDataCheckProcess);
            }
            if (TaskStatus.INCREMENTAL_START.getCode().equals(state) || TaskStatus.INCREMENTAL_RUNNING.getCode()
                .equals(state) || TaskStatus.INCREMENTAL_PAUSE.getCode().equals(state)) {
                String portalIncrementalProcess = PortalHandle.getPortalIncrementalProcess(installHost.getHost(),
                    installHost.getPort(), installHost.getRunUser(), password, installHost.getInstallPath(), t);
                log.debug("get portal incremental process content: {}, subTaskId: {}", portalIncrementalProcess,
                    t.getId());
                if (StringUtils.isNotBlank(portalIncrementalProcess)) {
                    migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(),
                        portalIncrementalProcess.trim(), ProcessType.INCREMENTAL.getCode());
                }
                result.put("incrementalProcess", portalIncrementalProcess);
            } else if (TaskStatus.INCREMENTAL_STOPPED.getCode().equals(state)
                || TaskStatus.INCREMENTAL_FINISHED.getCode().equals(state)) {
                String portalIncrementalProcess = PortalHandle.getPortalIncrementalProcess(installHost.getHost(),
                    installHost.getPort(), installHost.getRunUser(), password, installHost.getInstallPath(), t);
                result.put("incrementalProcess", portalIncrementalProcess);
            } else if (TaskStatus.REVERSE_START.getCode().equals(state) || TaskStatus.REVERSE_RUNNING.getCode()
                .equals(state)) {
                String portaReverselProcess = PortalHandle.getPortalReverseProcess(installHost.getHost(),
                    installHost.getPort(), installHost.getRunUser(), password, installHost.getInstallPath(), t);
                log.debug("get portal reverse process content: {}, subTaskId: {}", portaReverselProcess, t.getId());
                if (StringUtils.isNotBlank(portaReverselProcess)) {
                    migrationTaskExecResultDetailService.saveOrUpdateByTaskId(t.getId(), portaReverselProcess.trim(),
                        ProcessType.REVERSE.getCode());
                }
                result.put("reverseProcess", portaReverselProcess);
                String portalIncrementalProcess = PortalHandle.getPortalIncrementalProcess(installHost.getHost(),
                    installHost.getPort(), installHost.getRunUser(), password, installHost.getInstallPath(), t);
                result.put("incrementalProcess", portalIncrementalProcess);
            } else {
                log.debug("task status {} : {}", t.getId(), state);
            }
            if (state > t.getExecStatus()) {
                update.setExecStatus(state);
            } else if (Objects.equals(t.getExecStatus(), TaskStatus.INCREMENTAL_PAUSE.getCode()) || Objects.equals(
                t.getExecStatus(), TaskStatus.REVERSE_PAUSE.getCode())) {
                update.setExecStatus(state);
            } else {
                log.debug("task status {} : {} no update", t.getId(), state);
            }

            if (TaskStatus.FULL_CHECK_FINISH.getCode().equals(state) && t.getMigrationModelId().equals(1)) {
                update.setExecStatus(TaskStatus.MIGRATION_FINISH.getCode());
                update.setFinishTime(new Date());
            }
            if (TaskStatus.MIGRATION_ERROR.getCode().equals(state)) {
                String msg = MapUtil.getStr(lastStatus, "msg");
                update.setStatusDesc(msg);
            } else {
                // not check failure
                if (!TaskStatus.CHECK_ERROR.getCode().equals(t.getExecStatus())) {
                    update.setStatusDesc("");
                }
            }
            updateById(update);
        }
        return result;
    }

    /**
     * Calculate the progress bar of the full migration subtask
     *
     * @param portalProcess
     * @return process
     */
    private BigDecimal calculateFullMigrationProgress(String portalProcess) {
        Map<String, Object> processMap = JSON.parseObject(portalProcess);
        FullMigrationProcessCounter processCounter = new FullMigrationProcessCounter(processMap);
        int totalCount = processCounter.getTotalProcessCount();
        int totalFinishCount = processCounter.getTotalFinishCount();
        BigDecimal result = new BigDecimal(0);
        BigDecimal minProcess = new BigDecimal(0);
        if (totalCount > 0 && totalFinishCount > 0) {
            float coefficient = (float) 1 - (SUB_PROCESS * processCounter.getUnCountedNumber());
            minProcess = new BigDecimal(coefficient).subtract(new BigDecimal(SUB_PROCESS));
            result = new BigDecimal((float) totalFinishCount / totalCount * coefficient);
        }
        return BigDecimal.valueOf(Math.min(result.floatValue(), minProcess.floatValue()));
    }


    /**
     * Query the number of tasks not finish by target
     *
     * @param targetNodeId targetNodeId of the OpsHost object
     * @param targetDb     targetDb of the OpsHost object
     * @return number of tasks
     */
    @Override
    public Integer countNotFinishByTargetDb(String targetNodeId, String targetDb) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getTargetDb, targetDb).eq(MigrationTask::getTargetNodeId, targetNodeId);
        query.notIn(MigrationTask::getExecStatus, TaskStatus.MIGRATION_FINISH.getCode());
        return Math.toIntExact(count(query));
    }

    @Override
    public Integer countRunningByTargetDb(String targetDb) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getTargetDb, targetDb);
        query.in(MigrationTask::getExecStatus, TaskStatus.FULL_START.getCode(),
                TaskStatus.FULL_RUNNING.getCode(), TaskStatus.FULL_FINISH.getCode(),
                TaskStatus.FULL_CHECK_START.getCode(), TaskStatus.FULL_CHECKING.getCode(),
                TaskStatus.FULL_CHECK_FINISH.getCode(), TaskStatus.INCREMENTAL_START.getCode(),
                TaskStatus.INCREMENTAL_RUNNING.getCode(), TaskStatus.REVERSE_START.getCode(),
                TaskStatus.REVERSE_RUNNING.getCode()
        );
        return Math.toIntExact(count(query));
    }

    /**
     * Query the number of tasks running on the machine
     *
     * @param hostId Id of the OpsHost object
     * @return number of tasks
     */
    @Override
    public Integer countRunningByHostId(String hostId) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getRunHostId, hostId);
        query.notIn(MigrationTask::getExecStatus, TaskStatus.NOT_RUN.getCode(), TaskStatus.MIGRATION_FINISH.getCode());
        return Math.toIntExact(count(query));
    }

    @Override
    public List<MigrationTask> listRunningTaskByHostId(String hostId) {
        LambdaQueryWrapper<MigrationTask> query = new LambdaQueryWrapper<>();
        query.eq(MigrationTask::getRunHostId, hostId);
        query.notIn(MigrationTask::getExecStatus, TaskStatus.NOT_RUN.getCode(), TaskStatus.MIGRATION_FINISH.getCode());
        return list(query);
    }

    @Override
    public List<MigrationTask> listTaskByStatus(TaskStatus taskStatus) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getExecStatus, taskStatus.getCode());
        return list(queryWrapper);
    }

    @Override
    public Integer countTaskByStatus(TaskStatus taskStatus) {
        LambdaQueryWrapper<MigrationTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTask::getExecStatus, taskStatus.getCode());
        return Math.toIntExact(count(queryWrapper));
    }

    @Override
    public void updateStatus(Integer id, TaskStatus taskStatus) {
        MigrationTask migrationTask = new MigrationTask();
        migrationTask.setExecStatus(taskStatus.getCode());
        migrationTask.setId(id);
        updateById(migrationTask);
    }

    @Override
    public List<Map<String, Object>> countByMainTaskIdGroupByModel(Integer mainTaskId) {
        return migrationTaskMapper.countByMainTaskIdGroupByModelId(mainTaskId);
    }

    @Override
    public void runTask(MigrationTaskHostRef h, MigrationTask t, List<MigrationTaskGlobalParam> globalParams,
        String operateUsername) {
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(h.getRunHostId());
        installHost.setRunPassword(encryptionUtils.decrypt(installHost.getRunPassword()));
        t.setRunHostId(h.getRunHostId());
        MigrationTask update = MigrationTask.builder()
            .id(t.getId())
            .runHostId(h.getRunHostId())
            .runHost(h.getHost())
            .runHostname(h.getHostName())
            .runPort(h.getPort())
            .runUser(h.getUser())
            .runPass(h.getPassword())
            .build();
        updateById(update);
        if (!execMigrationCheck(installHost, t, globalParams, "verify_pre_migration")) {
            return;
        }
        PortalHandle.startPortal(installHost, t, installHost.getJarName(), getTaskParam(installHost, globalParams, t));
        update = MigrationTask.builder()
            .id(t.getId())
            .execStatus(TaskStatus.FULL_START.getCode())
            .execTime(new Date())
            .build();
        migrationTaskOperateRecordService.saveRecord(t.getId(), TaskOperate.RUN, operateUsername);
        updateById(update);
        MigrationTaskContext context = new MigrationTaskContext();
        context.setId(t.getId());
        context.setMigrationTask(t);
        context.setInstallHost(installHost);
        installMap.put(t.getId(), installHost);
        migrationTaskCheckProgressService.startCheckProgress(context);
    }

    /**
     * send command and return result by check result
     *
     * @param installHost installHost
     * @param t task
     * @param globalParams globalParams
     * @param command command
     * @return check result
     */
    public boolean execMigrationCheck(MigrationHostPortalInstall installHost, MigrationTask t,
        List<MigrationTaskGlobalParam> globalParams, String command) {
        JschResult checkResult = PortalHandle.checkBeforeMigration(installHost, t, installHost.getJarName(),
            getTaskParam(installHost, globalParams, t), command);
        MigrationTask update = MigrationTask.builder().id(t.getId()).build();
        if (checkResult.getResult().contains("verify migration success.")) {
            return true;
        }
        if (!checkResult.isOk()) {
            log.error("exec checkBeforeMigration command failed.");
            update.setExecStatus(TaskStatus.MIGRATION_ERROR.getCode());
            update.setStatusDesc(checkResult.getResult());
        }
        if (checkResult.getResult().contains("verify migration failed.")) {
            log.info("exec checkBeforeMigration command result {}", checkResult.getResult());
            update.setExecStatus(TaskStatus.CHECK_ERROR.getCode());
            String result = PortalHandle.getPortalCheckResult(installHost, t.getId());
            update.setStatusDesc(result);
        }
        updateById(update);
        migrationMainTaskService.updateStatus(t.getMainTaskId(), MainTaskStatus.CHECK_MIGRATION);
        return false;
    }


    @Override
    public TaskProcessStatus checkStatusOfIncrementalOrReverseMigrationTask(Integer id) {
        MigrationTask migrationTask = getById(id);
        OpsAssert.nonNull(migrationTask, "migration task not exist.");
        MigrationTaskStatusRecord lastTaskStatus = migrationTaskStatusRecordService.getLastByTaskId(id);
        OpsAssert.nonNull(lastTaskStatus, "migration task status not exist.");
        TaskProcessStatus taskProcessStatus = new TaskProcessStatus();
        taskProcessStatus.setId(id);
        taskProcessStatus.setStatus(lastTaskStatus.getStatusId());
        taskProcessStatus.setMessage(TaskStatus.getCommandByCode(lastTaskStatus.getStatusId()));
        // 未流转到增量迁移阶段，或者迁移任务已结束，则直接返回
        if (taskProcessStatus.getStatus() < TaskStatus.INCREMENTAL_START.getCode()) {
            taskProcessStatus.setMessage("The current task has not reached the incremental migration stage");
            taskProcessStatus.setType("no start");
            return taskProcessStatus;
        } else if (Objects.equals(taskProcessStatus.getStatus(), TaskStatus.MIGRATION_FINISH.getCode())) {
            taskProcessStatus.setType("finished");
            return taskProcessStatus;
        } else {
            // 流转到增量迁移阶段之后，则检测进程相关信息
            MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
                migrationTask.getRunHostId());
            String installPath = installHost.getInstallRootPath();
            Map<String, ProcessStatus> processMap = migrationRecoveryHandler.fetchProcessStatusListByMigrationTask(
                migrationTask, installPath);
            if (!migrationRecoveryHandler.hasPortal(processMap)) {
                taskProcessStatus.setType("error");
                String version = migrationRecoveryHandler.getJarVersion(installHost.getJarName());
                if (migrationRecoveryHandler.checkPortalVersion(version)) {
                    taskProcessStatus.setMessage("The current portal version" + version
                        + " does not support migration recovery. task has been error");
                } else {
                    taskProcessStatus.setMessage("The current portal process does not exist. task has been error");
                }
                return taskProcessStatus;
            }
            if (INCREMENTAL_STATUS.contains(lastTaskStatus.getStatusId())) {
                taskProcessStatus.setType("online");
                taskProcessStatus.setSource(migrationRecoveryHandler.hasIncrementalSource(processMap));
                taskProcessStatus.setSink(migrationRecoveryHandler.hasIncrementalSink(processMap));
            } else if (REVERSE_STATUS.contains(lastTaskStatus.getStatusId())) {
                taskProcessStatus.setType("reverse");
                taskProcessStatus.setSource(migrationRecoveryHandler.hasReverseSource(processMap));
                taskProcessStatus.setSink(migrationRecoveryHandler.hasReverseSink(processMap));
            } else {
                log.debug("The current portal process does not exist.");
            }
        }
        return taskProcessStatus;
    }

    @Override
    public TaskProcessStatus startTaskOfIncrementalOrReverseMigrationProcess(Integer id, String name) {
        MigrationTask migrationTask = getById(id);
        OpsAssert.nonNull(migrationTask, "migration task not exist.");
        MigrationTaskStatusRecord lastTaskStatus = migrationTaskStatusRecordService.getLastByTaskId(id);
        OpsAssert.nonNull(lastTaskStatus, "migration task status not exist.");
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
            migrationTask.getRunHostId());
        String installPath = installHost.getInstallRootPath();
        Map<String, ProcessStatus> processMap = migrationRecoveryHandler.fetchProcessStatusListByMigrationTask(
            migrationTask, installPath);
        if (!migrationRecoveryHandler.hasPortal(processMap)) {
            String version = migrationRecoveryHandler.getJarVersion(installHost.getJarName());
            if (migrationRecoveryHandler.checkPortalVersion(version)) {
                throw new OpsException("The current portal version" + version
                    + " does not support migration recovery. task has been error");
            } else {
                throw new OpsException("The current portal process does not exist. task has been error");
            }
        }
        // 未流转到增量迁移阶段，或者迁移任务已结束，则直接返回
        if (INCREMENTAL_STATUS.contains(lastTaskStatus.getStatusId())) {
            migrationRecoveryHandler.startProcessOfIncrementalMigration(migrationTask, installHost, name);
            updateStatus(migrationTask.getId(), TaskStatus.INCREMENTAL_RUNNING);
        } else if (REVERSE_STATUS.contains(lastTaskStatus.getStatusId())) {
            migrationRecoveryHandler.startProcessOfReverseMigration(migrationTask, installHost, name);
            updateStatus(migrationTask.getId(), TaskStatus.REVERSE_RUNNING);
        } else {
            log.debug("The current portal process does not exist.");
        }
        return checkStatusOfIncrementalOrReverseMigrationTask(id);
    }

    @Override
    public MigrationTaskCheckProgressSummary queryFullCheckSummaryOfMigrationTask(Integer id) {
        return migrationTaskCheckProgressService.queryFullCheckSummaryOfMigrationTask(id);
    }

    @Override
    public IPage<MigrationTaskCheckProgressDetail> queryFullCheckDetailOfMigrationTask(FullCheckParam fullCheckParam) {
        Integer id = fullCheckParam.getId();
        OpsAssert.nonNull(id, "migration task id can't be null");
        MigrationTask migrationTask = getById(id);
        OpsAssert.nonNull(migrationTask, "migration task not exist");
        String status = fullCheckParam.getStatus();
        int pageNum = fullCheckParam.getPageNum();
        int pageSize = fullCheckParam.getPageSize();
        return migrationTaskCheckProgressService.pageFullCheckDetailOfMigrationTask(id, status, pageSize, pageNum);
    }

    @Override
    public String downloadRepairFile(Integer id, String repairFileName) {
        MigrationTask task = getById(id);
        MigrationHostPortalInstall installHost = migrationHostPortalInstallHostService.getOneByHostId(
            task.getRunHostId());
        String catFailedRepairFile = "cat " + installHost.getInstallPath() + "portal/workspace/" + id
            + "/checkResult/result/" + repairFileName;
        JschResult result = ShellUtil.execCommandGetResult(task.getRunHost(), task.getRunPort(), task.getRunUser(),
            encryptionUtils.decrypt(task.getRunPass()), catFailedRepairFile);
        return result.isOk() ? result.getResult() : "";
    }

    /**
     * special character handling
     *
     * @param str String that needs to be processed.
     * @return processed string
     */
    public String escapeChars(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < str.length(); index++) {
            char con = str.charAt(index);
            // These characters are part of the query syntax and must be escaped
            if (shouldEscape(con) || Character.isWhitespace(con)) {
                sb.append('\\');
            }
            sb.append(con);
        }
        return sb.toString();
    }

    /**
     * determine if a given character needs to be escaped
     *
     * @param con char content
     * @return result
     */
    private boolean shouldEscape(char con) {
        for (char ch : SPECIAL_CHARS) {
            if (ch == con || Character.isWhitespace(con)) {
                return true;
            }
        }
        return false;
    }

    /**
     * get migration task params
     *
     * @param globalParams globalParams
     * @param task migration task
     * @return param map
     */
    private Map<String, String> getTaskParam(MigrationHostPortalInstall installHost,
        List<MigrationTaskGlobalParam> globalParams, MigrationTask task) {
        if (globalParams == null) {
            globalParams = migrationTaskGlobalParamService.selectByMainTaskId(task.getMainTaskId());
        }
        Map<String, String> globalParamMap = globalParams.stream().collect(Collectors.toMap(g -> g.getParamKey(),
                g -> g.getParamKey().startsWith("override_tables") ? g.getParamValue()
                        : escapeChars(g.getParamValue())));
        List<MigrationTaskParam> migrationTaskParams = migrationTaskParamService.selectByTaskId(task.getId());
        if (migrationTaskParams.size() > 0) {
            Map<String, String> taskParamMap = migrationTaskParams.stream()
                    .collect(Collectors.toMap(p -> p.getParamKey(), p -> p.getParamKey()
                            .startsWith("override_tables") ? p.getParamValue() : escapeChars(p.getParamValue())));
            globalParamMap.putAll(taskParamMap);
        }
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("mysql.user.name", task.getSourceDbUser());
        resultMap.put("mysql.user.password", encryptPassword(installHost, task.getSourceDbPass()));
        resultMap.put("mysql.database.host", task.getSourceDbHost());
        resultMap.put("mysql.database.port", task.getSourceDbPort());
        resultMap.put("mysql.database.name", task.getSourceDb());
        resultMap.put("mysql.database.table", task.getSourceTables());
        resultMap.put("opengauss.user.name", task.getTargetDbUser());
        resultMap.put("opengauss.user.password",
            encryptPassword(installHost, encryptionUtils.decrypt(task.getTargetDbPass())));
        resultMap.put("opengauss.database.host", task.getTargetDbHost());
        resultMap.put("opengauss.database.port", task.getTargetDbPort());
        resultMap.put("opengauss.database.name", task.getTargetDb());
        resultMap.put("opengauss.database.schema", task.getSourceDb());
        resultMap.put("migration_mode", task.getMigrationModelId() + "");
        resultMap.put("is_adjustKernel_param", task.getIsAdjustKernelParam() + "");

        if (globalParamMap.keySet().size() > 0) {
            resultMap.putAll(globalParamMap);
        }
        setToolsParams(resultMap, task.getRunHostId());

        setOpengaussClusterParams(resultMap, task.getTargetNodeId());
        setMigrationAlertParams(resultMap, installHost.getHost());
        return resultMap;
    }

    /**
     * Encrypt password (AES) if version is match
     *
     * @param password password
     * @return String
     */
    public String encryptPassword(MigrationHostPortalInstall installHost, String password) {
        if (!installHost.isVersionGreaterThan(6)) {
            return escapeChars(password);
        }
        return encryptionUtils.encrypt(password, AES_SECRETKEY);
    }

    /**
     * set openGauss cluster params
     *
     * @param resultMap params map
     * @param nodeId target node id
     */
    private void setOpengaussClusterParams(Map<String, String> resultMap, String nodeId) {
        resultMap.put("opengauss.database.iscluster", "false");

        OpsClusterVO opsClusterVO = opsFacade.getOpsClusterVOByNodeId(nodeId);

        if (opsClusterVO != null && opsClusterVO.getClusterNodes().size() > 1) {
            List<OpsClusterNodeVO> standbyNodes = opsClusterVO.getOtherNodes(nodeId);

            List<String> hostnames = standbyNodes.stream()
                    .map(OpsClusterNodeVO::getPublicIp)
                    .collect(Collectors.toList());

            List<String> ports = standbyNodes.stream()
                    .map(node -> node.getDbPort().toString())
                    .collect(Collectors.toList());

            resultMap.put("opengauss.database.iscluster", "true");
            resultMap.put("opengauss.database.standby.hostnames", String.join(",", hostnames));
            resultMap.put("opengauss.database.standby.ports", String.join(",", ports));
        }
    }

    /**
     * 组装从页面传入的参数 修改的参数 configId.typeId.parma=parmaValue 删除的参数 删除的配置文件名称=删除的key，删除的key 新增的参数
     *
     * @param runHostId run host id
     * @param resultMap result
     */
    private void setToolsParams(Map<String, String> resultMap, String runHostId) {
        HashMap<String, String> toolsParamsMap = new HashMap<>();
        setModParam(toolsParamsMap, runHostId);
        // 删除的参数
        setDeleteParam(toolsParamsMap, runHostId);
        // 新增的参数
        setNewAddParam(toolsParamsMap, runHostId);
        if (!toolsParamsMap.isEmpty()) {
            resultMap.putAll(toolsParamsMap);
        }
    }

    private void setMigrationAlertParams(Map<String, String> resultMap, String runHostIp) {
        MigrationThirdPartySoftwareConfig mqInstance = migrationMqInstanceService.getOneByKafkaIp(runHostIp);
        if (mqInstance != null) {
            resultMap.put(TaskAlertConstants.Params.ENABLE_ALERT_LOG_COLLECTION, "true");
            resultMap.put(TaskAlertConstants.Params.KAFKA_SEVER,
                    mqInstance.getKafkaIp() + ":" + mqInstance.getKafkaPort());
        }
    }

    private static String aroundApostrophe(String source) {
        return "'" + source + "'";
    }

    private void setModParam(HashMap<String, String> toolsParamsMap, String runHostId) {
        // 修改的参数
        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> toolsParamQueryWrapper = new LambdaQueryWrapper<>();
        toolsParamQueryWrapper.isNotNull(TbMigrationTaskGlobalToolsParam::getParamChangeValue);
        toolsParamQueryWrapper.eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, runHostId);
        toolsParamQueryWrapper.and(record -> record.eq(TbMigrationTaskGlobalToolsParam::getDeleteFlag,
                        TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag())
                .or().isNull(TbMigrationTaskGlobalToolsParam::getDeleteFlag));
        toolsParamQueryWrapper.and(record -> record.ne(TbMigrationTaskGlobalToolsParam::getNewParamFlag,
                        TbMigrationTaskGlobalToolsParam.NewParamFlagEnum.NEW_PARAM.getNewParamFlag())
                .or().isNull(TbMigrationTaskGlobalToolsParam::getNewParamFlag));
        List<TbMigrationTaskGlobalToolsParam> toolsParams = toolsParamService.list(toolsParamQueryWrapper);

        toolsParams.forEach(toolsParam -> {
            if (toolsParam.getConfigId().equals(ToolsConfigEnum.PORTAL_MIGRATION.getType())) {
                toolsParamsMap.put(toolsParam.getParamKey(), aroundApostrophe(toolsParam.getParamChangeValue()));
            } else {
                toolsParamsMap.put(toolsParam.getConfigId() + "." + toolsParam.getParamValueType() + "."
                                + toolsParam.getParamKey(),
                        aroundApostrophe(toolsParam.getParamChangeValue()));
            }
        });
    }

    private void setNewAddParam(HashMap<String, String> toolsParamsMap, String runHostId) {
        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> toolsParamQueryWrapper = new LambdaQueryWrapper<>();
        toolsParamQueryWrapper.clear();
        toolsParamQueryWrapper.eq(TbMigrationTaskGlobalToolsParam::getNewParamFlag,
                        TbMigrationTaskGlobalToolsParam.NewParamFlagEnum.NEW_PARAM.getNewParamFlag())
                .eq(TbMigrationTaskGlobalToolsParam::getDeleteFlag,
                        TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.USED.getDeleteFlag());
        toolsParamQueryWrapper.eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, runHostId);
        List<TbMigrationTaskGlobalToolsParam> newParam = toolsParamService.list(toolsParamQueryWrapper);
        newParam.forEach(toolsParam -> {
            toolsParamsMap.put(NEW_PARAM_PREFIX + toolsParam.getConfigId() + "." + toolsParam.getParamValueType()
                            + "." + toolsParam.getParamKey(),
                    aroundApostrophe(toolsParam.getParamChangeValue() == null ? toolsParam.getParamValue()
                            : toolsParam.getParamChangeValue()));
            toolsParamsMap.put(NEW_DESC_PREFIX + toolsParam.getConfigId() + "." + toolsParam.getParamValueType()
                    + "." + toolsParam.getParamKey(), aroundApostrophe(toolsParam.getParamDesc()));
        });
    }

    private void setDeleteParam(HashMap<String, String> toolsParamsMap, String runHostId) {
        LambdaQueryWrapper<TbMigrationTaskGlobalToolsParam> toolsParamQueryWrapper = new LambdaQueryWrapper<>();
        toolsParamQueryWrapper.eq(TbMigrationTaskGlobalToolsParam::getDeleteFlag,
                TbMigrationTaskGlobalToolsParam.DeleteFlagEnum.DELETE.getDeleteFlag());
        toolsParamQueryWrapper.eq(TbMigrationTaskGlobalToolsParam::getPortalHostID, runHostId);
        List<TbMigrationTaskGlobalToolsParam> deleteParams = toolsParamService.list(toolsParamQueryWrapper);
        if (Objects.nonNull(deleteParams)) {
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.CHAMELEON_CONFIG, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DATA_CHECK_APPLICATION, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DATA_CHECK_APPLICATION_SINK, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DATA_CHECK_APPLICATION_SOURCE, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DEBEZIUM_MYSQL_SINK, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DEBEZIUM_MYSQL_SOURCE, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DEBEZIUM_OPENGAUSS_SINK, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.DEBEZIUM_OPENGAUSS_SOURCE, toolsParamsMap);
            setDeleteToolsParams(deleteParams, ToolsConfigEnum.PORTAL_MIGRATION, toolsParamsMap);
        }
    }

    private void setDeleteToolsParams(List<TbMigrationTaskGlobalToolsParam> deleteParams,
                                      ToolsConfigEnum toolsConfigEnum, HashMap<String, String> toolsParamsMap) {
        List<String> deleteKeys = deleteParams.stream().filter(globalToolsParam ->
                        globalToolsParam.getConfigId().equals(toolsConfigEnum.getType()))
                .map(TbMigrationTaskGlobalToolsParam::getParamKey).collect(Collectors.toList());
        if (deleteKeys.isEmpty()) {
            log.info("{} delete keys is empty", toolsConfigEnum.getConfigName());
            return;
        }
        String deleteKeysStr = String.join(",", deleteKeys);
        toolsParamsMap.put(toolsConfigEnum.getConfigName(), deleteKeysStr.replaceAll(" ", "_"));
    }

    /**
     * Subtask Execution Offline Scheduler
     */
    @Override
    public void doOfflineTaskRunScheduler() {
        while (true) {
            Integer waitRunCount = countTaskByStatus(TaskStatus.WAIT_RESOURCE);
            if (waitRunCount > 0) {
                log.info("waiting for the resource to execute，task count : {}", waitRunCount);
                List<MigrationTask> migrationTasks = listTaskByStatus(TaskStatus.WAIT_RESOURCE);
                migrationTasks.forEach((t -> {
                    List<MigrationTaskHostRef> hosts = migrationTaskHostRefService.listByMainTaskId(t.getMainTaskId());
                    MigrationTaskHostRef selectHost = hosts.stream()
                        .filter(h -> h.getRunnableCount() > 0)
                        .findFirst()
                        .orElse(null);
                    if (selectHost != null) {
                        log.info(
                            "Offline scheduling successfully assigns tasks to host for execution. taskId : {}, HostId"
                                + " : {}, HostIP : {}", t.getId(), selectHost.getRunHostId(), selectHost.getHost());
                        runTask(selectHost, t, null, null);
                    }
                }));
            }
            try {
                Thread.sleep(taskOfflineSchedulerIntervalsMillisecond);
            } catch (InterruptedException e) {
                log.error("offline task scheduler error, message: {}", e.getMessage());
            }
        }
    }
}
