/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.admin.plugin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.constants.TaskAlertConstants;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskAlert;
import org.opengauss.admin.plugin.domain.MigrationTaskAlertDetail;
import org.opengauss.admin.plugin.enums.AlertMigrationPhaseEnum;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.mapper.MigrationTaskAlertMapper;
import org.opengauss.admin.plugin.service.MigrationHostPortalInstallHostService;
import org.opengauss.admin.plugin.service.MigrationTaskAlertDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskAlertService;
import org.opengauss.admin.plugin.service.MigrationTaskService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * tb migration alert service impl
 *
 * @since 2024/12/16
 */
@Slf4j
@Service
public class MigrationTaskAlertServiceImpl extends ServiceImpl<MigrationTaskAlertMapper, MigrationTaskAlert>
        implements MigrationTaskAlertService {
    private static final Map<Integer, Long> ALERT_REFRESH_RECORD = new ConcurrentHashMap<>();

    @Autowired
    private MigrationTaskAlertMapper alertMapper;

    @Autowired
    private MigrationTaskAlertDetailService alertDetailService;

    @Autowired
    private MigrationHostPortalInstallHostService portalInstallHostService;

    @Autowired
    private MigrationTaskService migrationTaskService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private Cache<Integer, Integer> resolvedObjectsNumberCache;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Override
    public synchronized void refreshAlertByPortal(MigrationTask task) {
        MigrationHostPortalInstall portalInstall = portalInstallHostService.getOneByHostId(task.getRunHostId());
        String portalHome = PortalHandle.generatePortalHome(portalInstall.getInstallPath());

        Integer taskId = task.getId();
        String alertFileHome = String.format(TaskAlertConstants.ALERT_FILE_HOME_PATH_MODEL, portalHome, taskId);

        Integer cacheNumber = resolvedObjectsNumberCache.asMap().get(taskId);
        int resolvedNumber = cacheNumber == null ? -1 : cacheNumber;
        if (resolvedNumber == -1) {
            resolvedNumber = countAlertByTaskId(taskId);
            resolvedObjectsNumberCache.put(taskId, resolvedNumber);
        }

        String alertFilePath = alertFileHome + String.format(TaskAlertConstants.ALERT_FILE_NAME_MODEL,
                resolvedNumber / TaskAlertConstants.ALERT_FILE_SIZE + 1);
        ShellInfoVo shellInfo = new ShellInfoVo(portalInstall.getHost(), portalInstall.getPort(),
                portalInstall.getRunUser(), encryptionUtils.decrypt(portalInstall.getRunPassword()));

        boolean isFileExists = FileUtils.isRemoteFileExists(alertFilePath, shellInfo);
        if (!isFileExists) {
            return;
        }

        String alertFileContents = FileUtils.catRemoteFileContents(alertFilePath, shellInfo);
        if (alertFileContents.isEmpty()) {
            return;
        }

        List<MigrationTaskAlert> alertList = new ArrayList<>();
        int startObjectIndex = resolvedNumber % 100;
        String[] parts = alertFileContents.split(TaskAlertConstants.OBJECT_SEPARATOR);
        for (int i = startObjectIndex; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                MigrationTaskAlert alert = JSON.parseObject(part, MigrationTaskAlert.class);
                alert.setTaskId(taskId);
                alertList.add(alert);
                resolvedNumber++;
            }
        }
        resolvedObjectsNumberCache.put(taskId, resolvedNumber);

        saveBatch(alertList);
        List<MigrationTaskAlertDetail> alertDetailList = new ArrayList<>();
        for (MigrationTaskAlert alert : alertList) {
            alertDetailList.add(MigrationTaskAlertDetail.builder()
                    .alertId(alert.getId())
                    .detail(alert.generateAlertDetail())
                    .build());
        }
        alertDetailService.saveBatch(alertDetailList);
    }

    @Override
    public IPage<MigrationTaskAlert> selectPage(IPage<MigrationTaskAlert> page, int taskId, int migrationPhase) {
        syncRefreshAlertByPortal(migrationTaskService.getById(taskId));

        LambdaQueryWrapper<MigrationTaskAlert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTaskAlert::getTaskId, taskId);
        queryWrapper.eq(MigrationTaskAlert::getMigrationPhase, migrationPhase);

        IPage<MigrationTaskAlert> alertIPage = alertMapper.selectPage(page, queryWrapper);
        List<MigrationTaskAlert> taskAlerts = alertIPage.getRecords();
        if (taskAlerts != null) {
            taskAlerts.forEach(MigrationTaskAlert::formatDateTime);
        }

        alertIPage.setRecords(taskAlerts);
        return alertIPage;
    }

    @Override
    public int countAlertByTaskId(int taskId) {
        LambdaQueryWrapper<MigrationTaskAlert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTaskAlert::getTaskId, taskId);
        return (int) count(queryWrapper);
    }

    @Override
    public AjaxResult countAlertNumber(int taskId) {
        HashMap<String, Integer> countMap = new HashMap<>();
        countMap.put("total", countAlertByTaskId(taskId));
        countMap.put(AlertMigrationPhaseEnum.FULL_MIGRATION.getPhaseId().toString(),
                countAlertInPhase(taskId, AlertMigrationPhaseEnum.FULL_MIGRATION.getPhaseId()));
        countMap.put(AlertMigrationPhaseEnum.FULL_MIGRATION_CHECK.getPhaseId().toString(),
                countAlertInPhase(taskId, AlertMigrationPhaseEnum.FULL_MIGRATION_CHECK.getPhaseId()));
        countMap.put(AlertMigrationPhaseEnum.INCREMENTAL_MIGRATION.getPhaseId().toString(),
                countAlertInPhase(taskId, AlertMigrationPhaseEnum.INCREMENTAL_MIGRATION.getPhaseId()));
        countMap.put(AlertMigrationPhaseEnum.REVERSE_MIGRATION.getPhaseId().toString(),
                countAlertInPhase(taskId, AlertMigrationPhaseEnum.REVERSE_MIGRATION.getPhaseId()));
        return AjaxResult.success(countMap);
    }

    @Override
    public void deleteAlertByTaskId(int taskId) {
        LambdaQueryWrapper<MigrationTaskAlert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTaskAlert::getTaskId, taskId);

        List<MigrationTaskAlert> taskAlerts = list(queryWrapper);
        if (taskAlerts.isEmpty()) {
            return;
        }

        List<Integer> alertIds = taskAlerts.stream().map(MigrationTaskAlert::getId).collect(Collectors.toList());
        removeBatchByIds(alertIds);
        alertDetailService.removeBatchByIds(alertIds);
    }

    @Override
    public void deleteByMainTaskId(int mainTaskId) {
        List<MigrationTask> migrationTasks = migrationTaskService.listByMainTaskId(mainTaskId);
        if (migrationTasks.isEmpty()) {
            return;
        }
        List<Integer> taskIds = migrationTasks.stream().map(MigrationTask::getId).collect(Collectors.toList());

        LambdaQueryWrapper<MigrationTaskAlert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MigrationTaskAlert::getTaskId, taskIds);

        List<MigrationTaskAlert> taskAlerts = list(queryWrapper);
        if (taskAlerts.isEmpty()) {
            return;
        }

        List<Integer> alertIds = taskAlerts.stream().map(MigrationTaskAlert::getId).collect(Collectors.toList());
        removeBatchByIds(alertIds);
        alertDetailService.removeBatchByIds(alertIds);
    }

    private void syncRefreshAlertByPortal(MigrationTask task) {
        Integer taskId = task.getId();
        Long time = ALERT_REFRESH_RECORD.get(taskId);
        if (time == null
                || DateUtil.date().getTime() > (time + TaskAlertConstants.ALERT_REFRESH_INTERVALS_MILLISECOND)) {
            threadPoolTaskExecutor.submit(() -> {
                try {
                    log.info("Sync refresh task alert, taskId:{}", taskId);
                    refreshAlertByPortal(task);
                    ALERT_REFRESH_RECORD.put(taskId, DateUtil.date().getTime());
                } catch (Exception e) {
                    log.error("Sync refresh task status error. message: {}", e.getMessage());
                }
            });
        }
    }

    private int countAlertInPhase(int taskId, int phaseId) {
        LambdaQueryWrapper<MigrationTaskAlert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MigrationTaskAlert::getTaskId, taskId);
        queryWrapper.eq(MigrationTaskAlert::getMigrationPhase, phaseId);
        return (int) count(queryWrapper);
    }
}
