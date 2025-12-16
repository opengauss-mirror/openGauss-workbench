/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.github.benmanes.caffeine.cache.Cache;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import cn.hutool.core.map.MapUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;
import org.opengauss.admin.plugin.domain.FullMigrationSummaryData;
import org.opengauss.admin.plugin.domain.IncrementalMigrationProgress;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.ReverseMigrationProgress;
import org.opengauss.admin.plugin.dto.MultiDbPortalMigrationStatus;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;
import org.opengauss.admin.plugin.enums.MainTaskStatus;
import org.opengauss.admin.plugin.enums.MultiDbPortalStatusEnum;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.service.FullMigrationProgressService;
import org.opengauss.admin.plugin.service.FullMigrationSummaryDataService;
import org.opengauss.admin.plugin.service.IncrementalMigrationProgressService;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationMainTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.opengauss.admin.plugin.service.ReverseMigrationProgressService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MULTI_DB portal installer
 *
 * @since 2025/6/30
 */
@Slf4j
@Component
public class MultiDbPortalProgressLoader {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Resource
    private MigrationHostPortalInstallHostService portalInstallHostService;

    @Autowired
    private MigrationTaskStatusRecordService migrationTaskStatusRecordService;

    @Autowired
    @Lazy
    private MigrationMainTaskService migrationMainTaskService;

    @Autowired
    private MigrationTaskService migrationTaskService;

    @Autowired
    private FullMigrationSummaryDataService fullMigrationSummaryDataService;

    @Autowired
    private FullMigrationProgressService fullProgressService;

    @Autowired
    private IncrementalMigrationProgressService incrementalMigrationProgressService;

    @Autowired
    private ReverseMigrationProgressService reverseMigrationProgressService;

    @Autowired
    private Cache<String, Long> fileLastModifiedCache;

    /**
     * refresh task status
     *
     * @param task migration task
     */
    public void refreshStatusAndProcess(MigrationTask task) {
        Integer execStatus = task.getExecStatus();
        if (TaskStatus.CHECK_ERROR.getCode().equals(execStatus)
                || TaskStatus.MIGRATION_FINISH.getCode().equals(execStatus)
                || TaskStatus.MIGRATION_ERROR.getCode().equals(execStatus)) {
            return;
        }

        MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(task.getRunHostId());
        List<Map<String, Object>> migrationStatusList = loadMigrationStatus(portalInfo, task);
        if (ObjectUtils.isEmpty(migrationStatusList)) {
            return;
        }
        Map<String, Object> lastStatus = migrationStatusList.get(migrationStatusList.size() - 1);

        DbTypeEnum sourceDbType = task.getSourceDbType();
        if (DbTypeEnum.POSTGRESQL.equals(sourceDbType)) {
            refreshPostgresqlStatusAndProcess(task, portalInfo, lastStatus);
        } else if (DbTypeEnum.MILVUS.equals(sourceDbType) || DbTypeEnum.ELASTICSEARCH.equals(sourceDbType)) {
            refreshMilvusStatusAndProcess(task, portalInfo, lastStatus);
        } else {
            throw new UnsupportedOperationException("DbTypeEnum " + sourceDbType + " is not supported");
        }
    }

    private void refreshPostgresqlStatusAndProcess(
            MigrationTask task, MigrationHostPortalInstall portalInfo, Map<String, Object> lastStatus
    ) {
        Integer latestStatus = MapUtil.getInt(lastStatus, "status");
        IncrementalMigrationProgress incrementalMigrationProgress =
                incrementalMigrationProgressService.getOneByTaskId(task.getId());
        if (TaskStatus.FULL_START.getCode() <= latestStatus && incrementalMigrationProgress == null) {
            loadFullMigrationProgress(portalInfo, task);
        }

        ReverseMigrationProgress reverseMigrationProgress =
                reverseMigrationProgressService.getOneByTaskId(task.getId());
        if (TaskStatus.INCREMENTAL_START.getCode() <= latestStatus && reverseMigrationProgress == null) {
            loadIncrementalMigrationProgress(portalInfo, task);
        }

        if (TaskStatus.REVERSE_START.getCode() <= latestStatus) {
            loadReverseMigrationProgress(portalInfo, task);
        }
        updateStatus(task, lastStatus);
    }

    private void refreshMilvusStatusAndProcess(
            MigrationTask task, MigrationHostPortalInstall portalInfo, Map<String, Object> lastStatus
    ) {
        loadMilvusFullMigrationProgress(portalInfo, task.getId());
        updateStatus(task, lastStatus);

        Integer execStatus = migrationTaskService.getById(task.getId()).getExecStatus();
        if (TaskStatus.MIGRATION_FINISH.getCode().equals(execStatus)
                || TaskStatus.MIGRATION_ERROR.getCode().equals(execStatus)) {
            loadMilvusFullMigrationProgress(portalInfo, task.getId());
        }
    }

    private void loadMilvusFullMigrationProgress(MigrationHostPortalInstall portalInfo, Integer taskId) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        try {
            List<FullMigrationProgress> fullMigrationProgressList = new ArrayList<>();
            String successPath = MultiDbPortalDirHelper.getMilvusFullSuccessStatusFilePath(portalInfo, taskId);
            if (FileUtils.isRemoteFileExists(successPath, shellInfo)
                    && isFileModified(successPath, FileUtils.getRemoteFileLastModified(shellInfo, successPath))) {
                String fileContents = FileUtils.catRemoteFileContents(successPath, shellInfo);
                List<String> tableList = fileContents.trim().lines().toList();
                for (String table : tableList) {
                    FullMigrationProgress fullMigrationProgress = new FullMigrationProgress();
                    fullMigrationProgress.setTaskId(taskId);
                    fullMigrationProgress.setName(table);
                    fullMigrationProgress.setSchema("-");
                    fullMigrationProgress.setPercent(100.0);
                    fullMigrationProgress.setStatus(3);
                    fullMigrationProgress.setObjectType(FullMigrationDbObjEnum.TABLE.getObjectType());
                    fullMigrationProgressList.add(fullMigrationProgress);
                }
                fullProgressService.deleteByTaskIdAndStatus(taskId, 3);
            }

            String failedPath = MultiDbPortalDirHelper.getMilvusFullFailedStatusFilePath(portalInfo, taskId);
            if (FileUtils.isRemoteFileExists(failedPath, shellInfo)
                    && isFileModified(failedPath, FileUtils.getRemoteFileLastModified(shellInfo, failedPath))) {
                String fileContents = FileUtils.catRemoteFileContents(failedPath, shellInfo);
                List<String> tableList = fileContents.trim().lines().toList();
                for (String table : tableList) {
                    FullMigrationProgress fullProgress = new FullMigrationProgress();
                    fullProgress.setTaskId(taskId);
                    fullProgress.setName(table);
                    fullProgress.setSchema("-");
                    fullProgress.setPercent(0.0);
                    fullProgress.setStatus(6);
                    fullProgress.setError("Please check the log for details, log path: " + FilenameUtils.normalize(
                            MultiDbPortalDirHelper.getMilvusTableLogPath(portalInfo, taskId, table)));
                    fullProgress.setObjectType(FullMigrationDbObjEnum.TABLE.getObjectType());
                    fullMigrationProgressList.add(fullProgress);
                }
                fullProgressService.deleteByTaskIdAndStatus(taskId, 6);
            }

            fullProgressService.saveBatch(fullMigrationProgressList);
        } catch (JSchException | SftpException e) {
            log.error("Failed to load full migration progress", e);
        }
    }

    private void updateStatus(MigrationTask task, Map<String, Object> lastStatus) {
        Integer execStatus = task.getExecStatus();
        if (execStatus >= TaskStatus.MIGRATION_FINISH.getCode()) {
            return;
        }

        Integer latestStatus = MapUtil.getInt(lastStatus, "status");
        MigrationTask update = MigrationTask.builder().id(task.getId()).build();
        if (TaskStatus.INCREMENTAL_PAUSE.getCode().equals(execStatus)
                || TaskStatus.REVERSE_PAUSE.getCode().equals(execStatus)
                || latestStatus > execStatus) {
            update.setExecStatus(latestStatus);
        } else {
            return;
        }

        if ((TaskStatus.FULL_CHECK_FINISH.getCode().equals(latestStatus) && task.getMigrationModelId() == 1)
                || TaskStatus.MIGRATION_FINISH.getCode().equals(latestStatus)) {
            update.setExecStatus(TaskStatus.MIGRATION_FINISH.getCode());
            update.setFinishTime(Instant.now());
        }
        if (TaskStatus.MIGRATION_ERROR.getCode().equals(latestStatus)) {
            String msg = MapUtil.getStr(lastStatus, "msg");
            update.setExecStatus(latestStatus);
            update.setFinishTime(Instant.now());
            update.setStatusDesc(msg);
        }
        migrationTaskService.updateById(update);
    }

    private void loadFullMigrationProgress(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        try {
            String totalStatusFilePath = MultiDbPortalDirHelper.getFullTotalStatusFilePath(portalInfo, task.getId());
            if (FileUtils.isRemoteFileExists(totalStatusFilePath, shellInfo) && isFileModified(totalStatusFilePath,
                    FileUtils.getRemoteFileLastModified(shellInfo, totalStatusFilePath))) {
                String fileContents = FileUtils.catRemoteFileContents(totalStatusFilePath, shellInfo);
                FullMigrationSummaryData fullSummaryData =
                        JSON.parseObject(fileContents, FullMigrationSummaryData.class, Feature.IgnoreNotMatch);
                fullSummaryData.setTaskId(task.getId());
                fullMigrationSummaryDataService.updateByTaskId(fullSummaryData);
            }

            processMigrationProgressFile(portalInfo, task, shellInfo, FullMigrationDbObjEnum.TABLE);
            processMigrationProgressFile(portalInfo, task, shellInfo, FullMigrationDbObjEnum.VIEW);
            processMigrationProgressFile(portalInfo, task, shellInfo, FullMigrationDbObjEnum.FUNCTION);
            processMigrationProgressFile(portalInfo, task, shellInfo, FullMigrationDbObjEnum.TRIGGER);
            processMigrationProgressFile(portalInfo, task, shellInfo, FullMigrationDbObjEnum.PROCEDURE);
        } catch (JSchException | SftpException e) {
            log.error("Failed to get file last modified", e);
        }
    }

    private void processMigrationProgressFile(
            MigrationHostPortalInstall portalInfo, MigrationTask task, ShellInfoVo shellInfo,
            FullMigrationDbObjEnum objectType) throws JSchException, SftpException {
        String statusFilePath = getStatusFilePath(portalInfo, task.getId(), objectType);
        if (!FileUtils.isRemoteFileExists(statusFilePath, shellInfo)
                || !isFileModified(statusFilePath, FileUtils.getRemoteFileLastModified(shellInfo, statusFilePath))) {
            return;
        }

        String fileContents = FileUtils.catRemoteFileContents(statusFilePath, shellInfo);
        List<FullMigrationProgress> progressList = JSON.parseArray(fileContents, FullMigrationProgress.class);
        for (FullMigrationProgress progress : progressList) {
            progress.setTaskId(task.getId());
            progress.setObjectType(objectType.getObjectType());
        }

        fullProgressService.deleteByTaskIdAndObjectType(task.getId(), objectType);
        fullProgressService.saveBatch(progressList);
    }

    private String getStatusFilePath(
            MigrationHostPortalInstall portalInfo, Integer taskId, FullMigrationDbObjEnum objectType) {
        switch (objectType) {
            case TABLE:
                return MultiDbPortalDirHelper.getFullTableStatusFilePath(portalInfo, taskId);
            case VIEW:
                return MultiDbPortalDirHelper.getFullViewStatusFilePath(portalInfo, taskId);
            case FUNCTION:
                return MultiDbPortalDirHelper.getFullFunctionStatusFilePath(portalInfo, taskId);
            case TRIGGER:
                return MultiDbPortalDirHelper.getFullTriggerStatusFilePath(portalInfo, taskId);
            case PROCEDURE:
                return MultiDbPortalDirHelper.getFullProcedureStatusFilePath(portalInfo, taskId);
            default:
                throw new IllegalArgumentException("Unsupported object type: " + objectType);
        }
    }

    private void loadIncrementalMigrationProgress(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        try {
            String incrementalStatusFilePath =
                    MultiDbPortalDirHelper.getIncrementalStatusFilePath(portalInfo, task.getId());
            if (FileUtils.isRemoteFileExists(incrementalStatusFilePath, shellInfo)
                    && isFileModified(incrementalStatusFilePath,
                    FileUtils.getRemoteFileLastModified(shellInfo, incrementalStatusFilePath))) {
                String fileContents = FileUtils.catRemoteFileContents(incrementalStatusFilePath, shellInfo);
                IncrementalMigrationProgress incrementalMigrationProgress =
                        JSON.parseObject(fileContents, IncrementalMigrationProgress.class);
                incrementalMigrationProgress.setTaskId(task.getId());
                incrementalMigrationProgressService.updateByTaskId(incrementalMigrationProgress);
            }
        } catch (JSchException | SftpException e) {
            log.error("Failed to get file last modified", e);
        }
    }

    private void loadReverseMigrationProgress(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);
        try {
            String reverseStatusFilePath = MultiDbPortalDirHelper.getReverseStatusFilePath(portalInfo, task.getId());
            if (FileUtils.isRemoteFileExists(reverseStatusFilePath, shellInfo) && isFileModified(
                    reverseStatusFilePath, FileUtils.getRemoteFileLastModified(shellInfo, reverseStatusFilePath))) {
                String fileContents = FileUtils.catRemoteFileContents(reverseStatusFilePath, shellInfo);
                ReverseMigrationProgress reverseMigrationProgress =
                        JSON.parseObject(fileContents, ReverseMigrationProgress.class);
                reverseMigrationProgress.setTaskId(task.getId());
                reverseMigrationProgressService.updateByTaskId(reverseMigrationProgress);
            }
        } catch (JSchException | SftpException e) {
            log.error("Failed to get file last modified", e);
        }
    }

    private boolean isFileModified(String filePath, Long lastModified) {
        Long cacheModified = fileLastModifiedCache.getIfPresent(filePath);
        if (!lastModified.equals(cacheModified)) {
            fileLastModifiedCache.put(filePath, lastModified);
            return true;
        }
        return false;
    }

    private List<Map<String, Object>> loadMigrationStatus(MigrationHostPortalInstall portalInfo, MigrationTask task) {
        ShellInfoVo shellInfo = createShellInfo(portalInfo);

        String portalStatusFilePath = MultiDbPortalDirHelper.getPortalStatusFilePath(portalInfo, task.getId());
        String portalStatusContents = FileUtils.catRemoteFileContents(portalStatusFilePath, shellInfo);
        List<MultiDbPortalMigrationStatus> migrationStatusEntryList =
                JSON.parseArray(portalStatusContents, MultiDbPortalMigrationStatus.class);
        List<Map<String, Object>> dataKitStatusCodeList = new ArrayList<>();
        boolean isPreMigrationVerifyFailed = false;
        for (MultiDbPortalMigrationStatus multiDbPortalMigrationStatus : migrationStatusEntryList) {
            Map<String, Object> datakitSatusCodeMap = new HashMap<>();
            int datakitStatus = multiDbPortalMigrationStatus.getStatus().getDatakitStatus();
            if (datakitStatus == MultiDbPortalStatusEnum.PRE_MIGRATION_VERIFY_FAILED.getDatakitStatus()) {
                isPreMigrationVerifyFailed = true;
            }

            datakitSatusCodeMap.put("status", datakitStatus);
            long timestamp = multiDbPortalMigrationStatus.getTimestamp();
            datakitSatusCodeMap.put("timestamp", timestamp);
            dataKitStatusCodeList.add(datakitSatusCodeMap);
        }

        List<Map<String, Object>> statusResultList = dataKitStatusCodeList.stream()
                .sorted(Comparator.comparing(m -> MapUtil.getLong(m, "timestamp")))
                .collect(Collectors.toList());
        migrationTaskStatusRecordService.saveTaskRecord(task.getId(), statusResultList);
        if (isPreMigrationVerifyFailed) {
            migrationMainTaskService.updateStatus(task.getMainTaskId(), MainTaskStatus.CHECK_MIGRATION);

            String verifyResultFilePath = MultiDbPortalDirHelper.getVerifyResultFilePath(portalInfo, task.getId());
            String verifyResultContents = FileUtils.catRemoteFileContents(verifyResultFilePath, shellInfo);
            MigrationTask update = MigrationTask.builder().id(task.getId()).build();
            update.setStatusDesc(verifyResultContents);
            update.setFinishTime(Instant.now());
            update.setExecStatus(TaskStatus.CHECK_ERROR.getCode());
            migrationTaskService.updateById(update);
        }
        return statusResultList;
    }

    private ShellInfoVo createShellInfo(MigrationHostPortalInstall portalInfo) {
        return new ShellInfoVo(portalInfo.getHost(), portalInfo.getPort(), portalInfo.getRunUser(),
                encryptionUtils.decrypt(portalInfo.getRunPassword()));
    }
}
