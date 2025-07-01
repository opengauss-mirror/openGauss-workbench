/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.github.benmanes.caffeine.cache.Cache;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.FullMigrationProgress;
import org.opengauss.admin.plugin.domain.FullMigrationSummaryData;
import org.opengauss.admin.plugin.domain.IncrementalMigrationProgress;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.ReverseMigrationProgress;
import org.opengauss.admin.plugin.dto.MultiDbPortalMigrationStatus;
import org.opengauss.admin.plugin.enums.FullMigrationDbObjEnum;
import org.opengauss.admin.plugin.enums.TaskStatus;
import org.opengauss.admin.plugin.service.FullMigrationProgressService;
import org.opengauss.admin.plugin.service.FullMigrationSummaryDataService;
import org.opengauss.admin.plugin.service.IncrementalMigrationProgressService;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.service.MigrationTaskStatusRecordService;
import org.opengauss.admin.plugin.service.ReverseMigrationProgressService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
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
        MigrationHostPortalInstall portalInfo = portalInstallHostService.getOneByHostId(task.getRunHostId());
        List<Map<String, Object>> migrationStatusList = loadMigrationStatus(portalInfo, task);
        if (ObjectUtils.isEmpty(migrationStatusList)) {
            return;
        }
        Map<String, Object> lastStatus = migrationStatusList.get(migrationStatusList.size() - 1);
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
        MigrationTask update = MigrationTask.builder().id(task.getId()).build();
        if (task.getExecStatus() < TaskStatus.MIGRATION_FINISH.getCode()) {
            update.setExecStatus(latestStatus);
        }

        if ((TaskStatus.FULL_CHECK_FINISH.getCode().equals(latestStatus)
                || TaskStatus.MIGRATION_FINISH.getCode().equals(latestStatus))
                && task.getMigrationModelId().equals(1)) {
            update.setExecStatus(TaskStatus.MIGRATION_FINISH.getCode());
            update.setFinishTime(Instant.now());
        }
        if (TaskStatus.MIGRATION_ERROR.getCode().equals(latestStatus)) {
            String msg = MapUtil.getStr(lastStatus, "msg");
            update.setStatusDesc(msg);
        } else {
            if (!TaskStatus.CHECK_ERROR.getCode().equals(task.getExecStatus())) {
                update.setStatusDesc("");
            }
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
        for (MultiDbPortalMigrationStatus multiDbPortalMigrationStatus : migrationStatusEntryList) {
            Map<String, Object> datakitSatusCodeMap = new HashMap<>();
            int datakitStatus = multiDbPortalMigrationStatus.getStatus().getDatakitStatus();
            datakitSatusCodeMap.put("status", datakitStatus);
            long timestamp = multiDbPortalMigrationStatus.getTimestamp();
            datakitSatusCodeMap.put("timestamp", timestamp);
            dataKitStatusCodeList.add(datakitSatusCodeMap);
        }

        List<Map<String, Object>> statusResultList = dataKitStatusCodeList.stream()
                .sorted(Comparator.comparing(m -> MapUtil.getLong(m, "timestamp")))
                .collect(Collectors.toList());
        migrationTaskStatusRecordService.saveTaskRecord(task.getId(), statusResultList);
        return statusResultList;
    }

    private ShellInfoVo createShellInfo(MigrationHostPortalInstall portalInfo) {
        return new ShellInfoVo(portalInfo.getHost(), portalInfo.getPort(), portalInfo.getRunUser(),
                encryptionUtils.decrypt(portalInfo.getRunPassword()));
    }
}
