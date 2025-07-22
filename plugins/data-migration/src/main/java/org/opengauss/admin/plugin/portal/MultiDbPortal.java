/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import com.alibaba.fastjson.JSON;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;
import org.opengauss.admin.plugin.domain.FullMigrationSubProcessCounter;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskExecResultDetail;
import org.opengauss.admin.plugin.domain.MigrationTaskParam;
import org.opengauss.admin.plugin.domain.MigrationTaskStatusRecord;
import org.opengauss.admin.plugin.dto.MigrationCurrentCheckInfoDto;
import org.opengauss.admin.plugin.dto.MigrationLogsInfoDto;
import org.opengauss.admin.plugin.dto.MigrationTaskWebsocketInfoDto;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;
import org.opengauss.admin.plugin.enums.MigrationMode;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.exception.MigrationTaskException;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.service.FullMigrationProgressService;
import org.opengauss.admin.plugin.service.FullMigrationSummaryDataService;
import org.opengauss.admin.plugin.service.IncrementalMigrationProgressService;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationMainTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskParamService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.opengauss.admin.plugin.service.ReverseMigrationProgressService;
import org.opengauss.admin.plugin.vo.FullMigrationProgressVo;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.plugin.vo.TaskProcessStatus;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MULTI_DB portal
 *
 * @since 2025/06/23
 */
@Slf4j
@Component
public class MultiDbPortal extends MigrationPortal {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Resource
    private MigrationHostPortalInstallHostService portalInstallHostService;

    @Autowired
    private MigrationTaskStatusRecordService migrationTaskStatusRecordService;

    @Autowired
    private MigrationMainTaskService migrationMainTaskService;

    @Autowired
    private FullMigrationSummaryDataService fullMigrationSummaryDataService;

    @Autowired
    private FullMigrationProgressService fullProgressService;

    @Autowired
    private IncrementalMigrationProgressService incrementalMigrationProgressService;

    @Autowired
    private ReverseMigrationProgressService reverseMigrationProgressService;

    @Autowired
    private MultiDbPortalInstaller portalInstaller;

    @Autowired
    private MultiDbPortalProgressLoader portalProgressLoader;

    @Autowired
    private MultiDbPortalMigrationController portalMigrationController;

    @Autowired
    private MigrationTaskParamService migrationTaskParamService;

    /**
     * get portal install info
     *
     * @param task task
     * @param currentInfoType object type
     * @param info search info
     * @return Map<String, Object>  full migration process info
     */
    public Map<String, Object> getFullMigCurrentTypeInfo(
            MigrationTask task, String currentInfoType, MigrationCurrentCheckInfoDto info) {
        FullMigrationDbObjEnum objectType = Arrays.stream(FullMigrationDbObjEnum.values())
                .filter(e -> e.getObjectType().equals(currentInfoType))
                .findFirst()
                .orElseThrow(() -> new MigrationTaskException("Unsupported object type"));
        List<FullMigrationProgress> objectProgressList =
                fullProgressService.getListByTaskIdAndObjectType(task.getId(), objectType);

        List<FullMigrationProgressVo> filterList = objectProgressList.stream()
                .filter(objectProgress -> objectProgress.getName().contains(info.getTableName()))
                .filter(objectProgress -> objectProgress.getSchema().contains(info.getSchemaName()))
                .map(progress -> new FullMigrationProgressVo(progress, getSchemaMapping(task)))
                .collect(Collectors.toList());

        Map<String, Object> filteredMap = new HashMap<>();
        List<List<FullMigrationProgressVo>> pageList = Lists.partition(filterList, info.getPageSize());
        if (!pageList.isEmpty()) {
            filteredMap.put(currentInfoType, pageList.get(info.getPageNum() - 1));
        }
        filteredMap.put("total", filterList.size());
        return filteredMap;
    }

    private Map<String, String> getSchemaMapping(MigrationTask task) {
        Map<String, String> schemaMappings = new HashMap<>();
        List<String> sourceSchemas = Arrays.asList(task.getSourceSchemas().split(","));
        for (String configSchema : sourceSchemas) {
            schemaMappings.put(configSchema, configSchema);
        }

        List<MigrationTaskParam> migrationTaskParamList = migrationTaskParamService.selectByTaskId(task.getId())
                .stream().filter(param -> param.getParamKey().equals("schema.mappings"))
                .toList();
        String schemaMappingStr = null;
        if (!migrationTaskParamList.isEmpty()) {
            schemaMappingStr = migrationTaskParamList.get(0).getParamValue();
        }

        String[] configMappingArray = null;
        if (!ObjectUtils.isEmpty(schemaMappingStr)) {
            configMappingArray = schemaMappingStr.split(",");
        }

        if (configMappingArray != null) {
            for (String s : configMappingArray) {
                if (ObjectUtils.isEmpty(s)) {
                    continue;
                }

                String[] parts = s.split(":");
                if (parts.length != 2) {
                    log.error("Invalid schema mapping: {}", s);
                    continue;
                }

                String sourceSchema = parts[0];
                if (sourceSchemas.contains(sourceSchema)) {
                    schemaMappings.put(sourceSchema, parts[1]);
                }
            }
        }
        return schemaMappings;
    }

    /**
     * get migration logs info
     *
     * @param task task
     * @param info search info
     * @return Map<String, Object>  migration logs info
     */
    public Map<String, Object> getMigLogsInfo(MigrationTask task, MigrationLogsInfoDto info) {
        Map<String, Object> resultMap = new HashMap<>();
        if (task.getExecStatus().equals(TaskStatus.NOT_RUN.getCode())) {
            return resultMap;
        }

        MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(task.getRunHostId());
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        List<String> logPathList = MultiDbPortalDirHelper.getPortalLogPathList(shellInfo, portalInfo, task.getId());

        List<String> logsList = new ArrayList<>();
        String searchFileName = info.getFileName();
        if (ObjectUtils.isEmpty(searchFileName)) {
            logsList.addAll(logPathList);
        } else {
            String fileName;
            for (String path : logPathList) {
                fileName = Paths.get(path).getFileName().toString();
                if (fileName.contains(searchFileName)) {
                    logsList.add(path);
                }
            }
        }

        List<List<String>> pageList = Lists.partition(logsList, info.getPageSize());
        if (!pageList.isEmpty()) {
            resultMap.put("logs", pageList.get(info.getPageNum() - 1));
        }
        resultMap.put("total", logsList.size());
        return resultMap;
    }

    /**
     * get ws data
     *
     * @param task task
     * @return ws data
     */
    public MigrationTaskWebsocketInfoDto getWsData(MigrationTask task) {
        portalProgressLoader.refreshStatusAndProcess(task);
        MigrationTaskWebsocketInfoDto wsInfo = new MigrationTaskWebsocketInfoDto();
        Integer taskId = task.getId();
        MigrationTaskStatusRecord lastTaskStatus = migrationTaskStatusRecordService.getLagerStatusByTaskId(taskId);
        wsInfo.setCurrentExecStatus(lastTaskStatus.getStatusId());

        Integer execStatus = task.getExecStatus();
        wsInfo.setExecStatus(execStatus);
        wsInfo.setExceptionAlertTotalCount(0L);
        if (task.getFinishTime() != null || TaskStatus.MIGRATION_FINISH.getCode().equals(execStatus)
                || TaskStatus.MIGRATION_ERROR.getCode().equals(execStatus)
                || TaskStatus.CHECK_ERROR.getCode().equals(execStatus)
                || TaskStatus.WAIT_RESOURCE.getCode().equals(execStatus)) {
            wsInfo.setExecutedTime(Duration.between(task.getExecTime(), task.getFinishTime()).toSeconds());
        } else {
            wsInfo.setExecutedTime(Duration.between(task.getExecTime(), Instant.now()).toSeconds());
        }
        setProcessDetailAndStatusRecord(wsInfo, task);
        setFullMigrationObjectCounter(wsInfo, task);
        wsInfo.setTask(task);

        List<String> logPaths = new ArrayList<>();
        if (!execStatus.equals(TaskStatus.NOT_RUN.getCode())) {
            MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(task.getRunHostId());
            logPaths = PortalHandle.getPortalLogPath(portalInfo.getHost(), portalInfo.getPort(),
                    portalInfo.getRunUser(), encryptionUtils.decrypt(portalInfo.getRunPassword()),
                    portalInfo.getInstallPath(), task);
        }
        wsInfo.setLogs(logPaths);
        return wsInfo;
    }

    /**
     * check status of incremental or reverse migration task
     *
     * @param migrationTask migration task
     * @return TaskProcessStatus task process status
     */
    public TaskProcessStatus checkStatusOfIncrementalOrReverseMigrationTask(MigrationTask migrationTask) {
        TaskProcessStatus taskProcessStatus = new TaskProcessStatus();
        if (TaskStatus.INCREMENTAL_PAUSE.getCode().equals(migrationTask.getExecStatus())
                || TaskStatus.REVERSE_PAUSE.getCode().equals(migrationTask.getExecStatus())) {
            taskProcessStatus.setSink(false);
            taskProcessStatus.setSource(false);
        } else {
            taskProcessStatus.setSink(true);
            taskProcessStatus.setSource(true);
        }
        return taskProcessStatus;
    }

    /**
     * refresh task status
     *
     * @param task migration task
     */
    public void refreshStatusAndProcess(MigrationTask task) {
        portalProgressLoader.refreshStatusAndProcess(task);
    }

    /**
     * load migration status
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void startTask(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.startTask(portalInfo, task);
    }

    /**
     * stop incremental migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void stopIncremental(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.stopIncremental(portalInfo, task);
    }

    /**
     * resume incremental migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void resumeIncremental(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.resumeIncremental(portalInfo, task);
    }

    /**
     * start reverse migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void startReverse(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.startReverse(portalInfo, task);
    }

    /**
     * resume reverse migration
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void resumeReverse(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.resumeReverse(portalInfo, task);
    }

    /**
     * stop task
     *
     * @param portalInfo portal info
     * @param task task
     */
    public void stopTask(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        portalMigrationController.stopTask(portalInfo, task);
    }

    /**
     * check task status
     *
     * @param migrationTask task
     * @return Map<String, Object>  task status
     */
    public TaskProcessStatus resumeMigrationProcess(MigrationTask migrationTask) {
        return portalMigrationController.resumeMigrationProcess(migrationTask);
    }

    /**
     * install portal
     *
     * @param hostId host id
     * @param install install info
     * @param isReinstall is retry install
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult install(String hostId, MigrationHostPortalInstall install, boolean isReinstall) {
        return portalInstaller.install(hostId, install, isReinstall);
    }

    /**
     * delete portal
     *
     * @param hostId host id
     * @return AjaxResult.success
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deletePortal(String hostId) {
        return portalInstaller.deletePortal(hostId);
    }

    private void setFullMigrationObjectCounter(MigrationTaskWebsocketInfoDto wsInfo, MigrationTask task) {
        Integer taskId = task.getId();
        List<FullMigrationProgress> tableProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.TABLE);
        FullMigrationSubProcessCounter tableCounter = new FullMigrationSubProcessCounter(tableProgressList);
        wsInfo.setTableCounts(tableCounter);

        List<FullMigrationProgress> viewProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.VIEW);
        FullMigrationSubProcessCounter viewCounter = new FullMigrationSubProcessCounter(viewProgressList);
        wsInfo.setViewCounts(viewCounter);

        List<FullMigrationProgress> funcProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.FUNCTION);
        FullMigrationSubProcessCounter funcCounter = new FullMigrationSubProcessCounter(funcProgressList);
        wsInfo.setFuncCounts(new FullMigrationSubProcessCounter(funcProgressList));

        List<FullMigrationProgress> triggerProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.TRIGGER);
        FullMigrationSubProcessCounter triggerCounter = new FullMigrationSubProcessCounter(triggerProgressList);
        wsInfo.setTriggerCounts(triggerCounter);

        List<FullMigrationProgress> produceProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.PROCEDURE);
        FullMigrationSubProcessCounter produceCounter = new FullMigrationSubProcessCounter(produceProgressList);
        wsInfo.setProduceCounts(produceCounter);

        wsInfo.setTotalWaitCount(tableCounter.getWaitCount() + viewCounter.getWaitCount()
                + funcCounter.getWaitCount() + triggerCounter.getWaitCount() + produceCounter.getWaitCount());
        wsInfo.setTotalRunningCount(tableCounter.getRunningCount() + viewCounter.getRunningCount()
                + funcCounter.getRunningCount() + triggerCounter.getRunningCount() + produceCounter.getRunningCount());
        wsInfo.setTotalSuccessCount(tableCounter.getSuccessCount() + viewCounter.getSuccessCount()
                + funcCounter.getSuccessCount() + triggerCounter.getSuccessCount() + produceCounter.getSuccessCount());
        wsInfo.setTotalErrorCount(tableCounter.getErrorCount() + viewCounter.getErrorCount()
                + funcCounter.getErrorCount() + triggerCounter.getErrorCount() + produceCounter.getErrorCount());
    }

    private void setProcessDetailAndStatusRecord(MigrationTaskWebsocketInfoDto wsInfo, MigrationTask task) {
        HashMap<String, Object> fullProcessEntry = new HashMap<>();
        fullProcessEntry.put("total", fullMigrationSummaryDataService.getOneByTaskId(task.getId()));
        wsInfo.setFullProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                JSON.toJSONString(fullProcessEntry)).build());
        fullProcessEntry.clear();
        wsInfo.setDataCheckProcess(new MigrationTaskExecResultDetail());
        if (task.getMigrationModelId().equals(MigrationMode.ONLINE.getCode())) {
            wsInfo.setIncrementalProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                    JSON.toJSONString(incrementalMigrationProgressService.getOneByTaskId(task.getId()))).build());
            wsInfo.setReverseProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                    JSON.toJSONString(reverseMigrationProgressService.getOneByTaskId(task.getId()))).build());

            List<MigrationTaskStatusRecord> migrationTaskStatusRecords
                    = migrationTaskStatusRecordService.selectByTaskId(task.getId());
            Map<String, List<MigrationTaskStatusRecord>> recordMap = migrationTaskStatusRecords.stream()
                    .filter(record -> Objects.nonNull(record.getOperateType()))
                    .collect(Collectors.groupingBy(record -> record.getOperateType().toString()));
            wsInfo.setStatusRecords(recordMap);
        }
    }

    private ShellInfoVo createShellInfo(MigrationHostPortalInstall portalInfo) {
        return new ShellInfoVo(portalInfo.getHost(), portalInfo.getPort(), portalInfo.getRunUser(),
                encryptionUtils.decrypt(portalInfo.getRunPassword()));
    }
}
