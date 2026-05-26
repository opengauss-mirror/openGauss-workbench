/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.google.common.collect.Lists;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
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
import org.opengauss.admin.plugin.service.FullMigrationProgressService;
import org.opengauss.admin.plugin.service.FullMigrationSummaryDataService;
import org.opengauss.admin.plugin.service.IncrementalMigrationProgressService;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskParamService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.opengauss.admin.plugin.service.ReverseMigrationProgressService;
import org.opengauss.admin.plugin.utils.PageHelper;
import org.opengauss.admin.plugin.vo.FullMigrationProgressVo;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.plugin.vo.TaskProcessStatus;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            return getPgsqlFullMigCurrentTypeInfo(task, currentInfoType, info);
        } else if (DbTypeEnum.MILVUS.equals(sourceDbType) || DbTypeEnum.ELASTICSEARCH.equals(sourceDbType)) {
            return getMilvusFullMigCurrentTypeInfo(task, currentInfoType, info);
        } else {
            throw new MigrationTaskException("Unsupported database type to get full migration type info");
        }
    }

    private Map<String, Object> getPgsqlFullMigCurrentTypeInfo(
            MigrationTask task, String currentInfoType, MigrationCurrentCheckInfoDto info
    ) {
        FullMigrationDbObjEnum objectType = Arrays.stream(FullMigrationDbObjEnum.values())
                .filter(e -> e.getObjectType().equals(currentInfoType))
                .findFirst()
                .orElseThrow(() -> new MigrationTaskException("Unsupported object type"));
        List<FullMigrationProgress> objectProgressList =
                fullProgressService.getListByTaskIdAndObjectType(task.getId(), objectType);

        Map<String, String> schemaMapping = getSchemaMapping(task);
        List<FullMigrationProgressVo> filterList = objectProgressList.stream()
                .filter(objectProgress -> objectProgress.getName().contains(info.getTableName()))
                .filter(objectProgress -> objectProgress.getSchema().contains(info.getSchemaName()))
                .map(progress -> new FullMigrationProgressVo(progress, schemaMapping))
                .collect(Collectors.toList());

        Map<String, Object> filteredMap = new HashMap<>();
        List<List<FullMigrationProgressVo>> pageList = Lists.partition(filterList, info.getPageSize());
        if (!pageList.isEmpty()) {
            filteredMap.put(currentInfoType, pageList.get(info.getPageNum() - 1));
        }
        filteredMap.put("total", filterList.size());
        return filteredMap;
    }

    private Map<String, Object> getMilvusFullMigCurrentTypeInfo(
            MigrationTask task, String currentInfoType, MigrationCurrentCheckInfoDto info
    ) {
        if (!FullMigrationDbObjEnum.TABLE.getObjectType().equals(currentInfoType)) {
            throw new MigrationTaskException("Only table is supported, currentInfoType: " + currentInfoType);
        }
        List<FullMigrationProgress> objectProgressList = fullProgressService.getListByTaskId(task.getId());

        Map<String, String> tableMapping = getTableMapping(task);
        List<FullMigrationProgressVo> filterList = objectProgressList.stream()
                .filter(objectProgress -> objectProgress.getName().contains(info.getTableName()))
                .map(progress -> new FullMigrationProgressVo(progress, tableMapping.get(progress.getName())))
                .toList();

        Map<String, Object> filteredMap = new HashMap<>();
        Page<FullMigrationProgressVo> page = PageHelper.getPageFromList(
                filterList, new Page<>(info.getPageNum(), info.getPageSize()));
        filteredMap.put(currentInfoType, page.getRecords());
        filteredMap.put("total", page.getTotal());
        return filteredMap;
    }

    private Map<String, String> getTableMapping(MigrationTask task) {
        String paramKey = "table.mappings";
        List<MigrationTaskParam> migrationTaskParamList = migrationTaskParamService.selectByTaskId(task.getId())
                .stream().filter(param -> paramKey.equals(param.getParamKey()))
                .toList();
        String tableMappingStr = null;
        if (!migrationTaskParamList.isEmpty()) {
            tableMappingStr = migrationTaskParamList.get(0).getParamValue();
        }
        return parseMapping(task.getSourceTables(), tableMappingStr);
    }

    private Map<String, String> getSchemaMapping(MigrationTask task) {
        String paramKey = "schema.mappings";
        List<MigrationTaskParam> migrationTaskParamList = migrationTaskParamService.selectByTaskId(task.getId())
                .stream().filter(param -> paramKey.equals(param.getParamKey()))
                .toList();
        String schemaMappingStr = null;
        if (!migrationTaskParamList.isEmpty()) {
            schemaMappingStr = migrationTaskParamList.get(0).getParamValue();
        }

        return parseMapping(task.getSourceSchemas(), schemaMappingStr);
    }

    private Map<String, String> parseMapping(String sourceObjects, String mappingStr) {
        Map<String, String> mappings = new HashMap<>();
        List<String> sourceObjectsList = Arrays.asList(sourceObjects.split(","));
        for (String sourceObject : sourceObjectsList) {
            mappings.put(sourceObject, sourceObject);
        }

        if (!ObjectUtils.isEmpty(mappingStr)) {
            String[] configMappingArray = mappingStr.split(",");
            for (String s : configMappingArray) {
                if (ObjectUtils.isEmpty(s)) {
                    continue;
                }

                String[] parts = s.split(":");
                if (parts.length != 2) {
                    log.error("Invalid table mapping: {}", s);
                    continue;
                }

                String sourceTable = parts[0];
                if (sourceObjectsList.contains(sourceTable)) {
                    mappings.put(sourceTable, parts[1]);
                }
            }
        }

        return mappings;
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
            List<String> pageRecords = pageList.get(info.getPageNum() - 1);
            pageRecords = pageRecords.stream().map(FilenameUtils::normalize).toList();
            resultMap.put("logs", pageRecords);
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
        wsInfo.setIsAutoFinish(task.getIsAutoFinish());
        wsInfo.setExceptionAlertTotalCount(0L);
        setExecuteTime(wsInfo, task);
        if (DbTypeEnum.POSTGRESQL.equals(task.getSourceDbType())) {
            setProcessDetailAndStatusRecord(wsInfo, task);
            setFullMigrationObjectCounter(wsInfo, task);
        } else {
            setMilvusFullMigrationTableCounter(wsInfo, task);
        }
        wsInfo.setTask(task);
        return wsInfo;
    }

    private void setExecuteTime(MigrationTaskWebsocketInfoDto wsInfo, MigrationTask task) {
        if (task.getExecTime() == null) {
            log.error("task exec time is null, taskId={}", task.getId());
            return;
        }

        Integer execStatus = task.getExecStatus();
        if (TaskStatus.MIGRATION_FINISH.getCode().equals(execStatus)
                || TaskStatus.MIGRATION_ERROR.getCode().equals(execStatus)
                || TaskStatus.CHECK_ERROR.getCode().equals(execStatus)
                || TaskStatus.WAIT_RESOURCE.getCode().equals(execStatus)) {
            if (task.getFinishTime() == null) {
                log.error("task finish time is null, taskId={}", task.getId());
                return;
            }
            wsInfo.setExecutedTime(Duration.between(task.getExecTime(), task.getFinishTime()).toSeconds());
        } else {
            wsInfo.setExecutedTime(Duration.between(task.getExecTime(), Instant.now()).toSeconds());
        }
    }

    /**
     * check status of incremental or reverse migration task
     *
     * @param migrationTask migration task
     * @return TaskProcessStatus task process status
     */
    public TaskProcessStatus checkStatusOfIncrementalOrReverseMigrationTask(MigrationTask migrationTask) {
        DbTypeEnum sourceDbType = migrationTask.getSourceDbType();
        if (DbTypeEnum.MILVUS.equals(sourceDbType) || DbTypeEnum.ELASTICSEARCH.equals(sourceDbType)) {
            throw new MigrationTaskException("Milvus/Elasticsearch do not support incremental or reverse migration.");
        }

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

    private void setMilvusFullMigrationTableCounter(MigrationTaskWebsocketInfoDto wsInfo, MigrationTask task) {
        Integer taskId = task.getId();
        List<FullMigrationProgress> tableProgressList = fullProgressService.getListByTaskIdAndObjectType(
                taskId, FullMigrationDbObjEnum.TABLE);
        FullMigrationSubProcessCounter tableCounter = new FullMigrationSubProcessCounter(tableProgressList);
        wsInfo.setTableCounts(tableCounter);

        int successCount = tableCounter.getSuccessCount();
        int errorCount = tableCounter.getErrorCount();
        wsInfo.setTotalSuccessCount(successCount);
        wsInfo.setTotalErrorCount(errorCount);

        int concurrentThreads = 4;
        String paramKey = "migration.concurrent.threads";
        Optional<MigrationTaskParam> sourceTableParam = migrationTaskParamService.selectByTaskId(taskId).stream()
                .filter(param -> paramKey.equals(param.getParamKey()))
                .findFirst();
        if (sourceTableParam.isPresent()) {
            concurrentThreads = Integer.parseInt(sourceTableParam.get().getParamValue());
        }

        int total = (int) Arrays.stream(task.getSourceTables().split(",")).filter(table -> !table.isEmpty()).count();
        int remainingCount = total - successCount - errorCount;
        if (remainingCount > concurrentThreads) {
            wsInfo.setTotalRunningCount(concurrentThreads);
            wsInfo.setTotalWaitCount(remainingCount - concurrentThreads);
        } else {
            wsInfo.setTotalRunningCount(remainingCount);
            wsInfo.setTotalWaitCount(0);
        }
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
        Integer taskId = task.getId();
        HashMap<String, Object> fullProcessEntry = new HashMap<>();
        fullProcessEntry.put("total", fullMigrationSummaryDataService.getOneByTaskId(taskId));
        wsInfo.setFullProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                JSON.toJSONString(fullProcessEntry)).build());
        fullProcessEntry.clear();
        wsInfo.setDataCheckProcess(new MigrationTaskExecResultDetail());
        if (MigrationMode.hasIncrementalAndReverse(task.getMigrationModelId())) {
            wsInfo.setIncrementalProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                    JSON.toJSONString(incrementalMigrationProgressService.getOneByTaskId(taskId))).build());
            wsInfo.setReverseProcess(MigrationTaskExecResultDetail.builder().execResultDetail(
                    JSON.toJSONString(reverseMigrationProgressService.getOneByTaskId(taskId))).build());

            List<MigrationTaskStatusRecord> statusRecords = migrationTaskStatusRecordService.selectByTaskId(taskId);
            Map<String, List<MigrationTaskStatusRecord>> recordMap = statusRecords.stream()
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
