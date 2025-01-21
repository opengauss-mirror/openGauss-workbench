/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * MigrationTaskCheckProgressMonitor.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationTaskCheckProgressMonitor.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.impl;

import static org.opengauss.admin.plugin.enums.TaskStatus.FULL_CHECK_FINISH;
import static org.opengauss.admin.plugin.enums.TaskStatus.FULL_CHECK_START;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.context.MigrationTaskContext;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressDetail;
import org.opengauss.admin.plugin.domain.MigrationTaskCheckProgressSummary;
import org.opengauss.admin.plugin.handler.PortalHandle;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressDetailService;
import org.opengauss.admin.plugin.service.MigrationTaskCheckProgressSummaryService;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MigrationTaskCheckProgressMonitor
 * &#064;author:  wangchao
 * &#064;Date:  2024/12/31 10:07
 * &#064;Description:  MigrationTaskCheckProgressMonitor
 *
 * @since 7.0.0
 **/
@Slf4j
public class MigrationTaskCheckProgressMonitor implements Runnable {
    private boolean isRunning = true;
    private boolean isCompleted = false;
    private final Integer taskId;
    private Integer execStatus;
    private final String installPath;
    private final MigrationHostPortalInstall installHost;
    private final EncryptionUtils encryptionUtils;
    private final MigrationTask migrationTask;
    private final MigrationTaskCheckProgressSummaryService summaryService;
    private final MigrationTaskCheckProgressDetailService detailService;

    MigrationTaskCheckProgressMonitor(MigrationTaskContext context) {
        this.taskId = context.getId();
        this.migrationTask = context.getMigrationTask();
        this.encryptionUtils = context.getEncryptionUtils();
        this.summaryService = context.getMigrationTaskCheckProgressSummaryService();
        this.detailService = context.getMigrationTaskCheckProgressDetailService();
        this.installHost = context.getInstallHost();
        this.installPath = installHost.getInstallPath();
        this.execStatus = migrationTask.getExecStatus();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("full-check-progress-monitor-" + taskId);
        while (isRunning) {
            try {
                // when migration task status start full check ,start refresh check result
                // if migration task status init status is bigger than full check start status ,
                // updated task check result,and completed monitor thread.
                if (execStatus >= FULL_CHECK_START.getCode()) {
                    parseFullCheckProgressDetail();
                    parseFullCheckProgress();
                }
                // when migration task status finish full check , stop refresh check result
                if (execStatus > FULL_CHECK_FINISH.getCode()) {
                    isCompleted = true;
                }
                ThreadUtil.safeSleep(1000);
            } catch (Exception exc) {
                log.error("MigrationTaskCheckProgressMonitor parse error ,subTaskId={} ", taskId, exc);
            }
        }
    }

    private void parseFullCheckProgressDetail() {
        String password = encryptionUtils.decrypt(installHost.getRunPassword());
        String successCmd = "cat " + installPath + "portal/workspace/" + taskId + "/check_result/result/success.log";
        String failedCmd = "cat " + installPath + "portal/workspace/" + taskId + "/check_result/result/failed.log";
        Map<String, String> commands = new HashMap<>();
        Map<String, String> checkResultLogStatus = new HashMap<>();
        commands.put("success", successCmd);
        commands.put("failed", failedCmd);
        commands.forEach((key, value) -> {
            JschResult jschResult = ShellUtil.execCommandGetResult(installHost.getHost(), installHost.getPort(),
                installHost.getRunUser(), password, value);
            String resultMsg = getResultMessage(jschResult);
            if (jschResult.isOk()) {
                String message = PortalHandle.replaceAllBlank(resultMsg);
                if (StrUtil.isNotEmpty(message)) {
                    if (message.endsWith(",")) {
                        message = "[" + message.substring(0, message.length() - 1) + "]";
                        List<DataCheckVo> detailList = JSONObject.parseArray(message, DataCheckVo.class);
                        List<MigrationTaskCheckProgressDetail> details = detailList.stream().map(item -> {
                            MigrationTaskCheckProgressDetail tableMsg = new MigrationTaskCheckProgressDetail();
                            tableMsg.setId(taskId + "_" + item.getTable());
                            tableMsg.setTaskId(taskId);
                            tableMsg.setSchemaName(item.getSchema());
                            tableMsg.setSourceName(item.getTable());
                            tableMsg.setSinkName(item.getTable());
                            tableMsg.setStatus(key);
                            tableMsg.setMessage(item.getMessage());
                            tableMsg.setFailedRows(item.getDiffCount());
                            tableMsg.setRepairFileName(getFailedTableRepairName(key, item));
                            return tableMsg;
                        }).collect(Collectors.toList());
                        detailService.saveOrUpdateBatch(details, 100);
                        checkResultLogStatus.put(key, "success");
                        log.info("refresh data check detail information: subTaskId: {}  {}", detailList.size(), taskId);
                    } else {
                        log.warn("fetch data check detail information is invalid , subTaskId: {}", taskId);
                    }
                } else {
                    log.warn("fetch data check detail information empty , subTaskId: {}", taskId);
                }
            } else {
                checkResultLogStatus.put(key, "failed");
                log.warn("refresh data check detail information failed, subTaskId: {} {}", taskId, resultMsg);
            }
        });
    }

    private String getFailedTableRepairName(String key, DataCheckVo item) {
        if (key.equals("failed") && item.diffCount > 0) {
            return "repair_" + item.getSchema() + "_" + item.getTable() + "_" + "0_1.txt";
        }
        return "";
    }

    @NotNull
    private static String getResultMessage(JschResult jschResult) {
        String result = jschResult.getResult();
        return StrUtil.isNotEmpty(result) ? result.trim() : "";
    }

    private void parseFullCheckProgress() {
        String password = encryptionUtils.decrypt(installHost.getRunPassword());
        String summaryCmd = "grep '{.*}' " + installPath + "portal/workspace/" + taskId
            + "/status/full_migration_datacheck.txt | tail -n 1";
        JschResult result = ShellUtil.execCommandGetResult(installHost.getHost(), installHost.getPort(),
            installHost.getRunUser(), password, summaryCmd);
        String resultMsg = getResultMessage(result);
        if (result.isOk()) {
            String portalDataCheckProcess = PortalHandle.replaceAllBlank(resultMsg);
            if (StrUtil.isEmpty(portalDataCheckProcess)) {
                return;
            }
            MigrationTaskCheckProgressSummary summary = JSONObject.parseObject(portalDataCheckProcess,
                MigrationTaskCheckProgressSummary.class);
            summary.setTaskId(taskId);
            summary.setSourceDb(migrationTask.getSourceDb());
            summary.setSinkDb(migrationTask.getTargetDb());
            LambdaUpdateWrapper<MigrationTaskCheckProgressSummary> updateWrapper = Wrappers.lambdaUpdate(
                MigrationTaskCheckProgressSummary.class);
            updateWrapper.eq(MigrationTaskCheckProgressSummary::getTaskId, taskId);
            summaryService.saveOrUpdate(summary, updateWrapper);
            log.info("refresh data check summary information, subTaskId: {} ", taskId);
        } else {
            log.warn("refresh data check summary information failed, subTaskId: {} {}", taskId, resultMsg);
        }
    }

    /**
     * stop progress monitor
     */
    public synchronized void stop() {
        isRunning = false;
        log.info("migration check progress monitor will be stop, taskId: {}", taskId);
    }

    /**
     * check task completed
     *
     * @return boolean
     */
    public synchronized boolean isCheckTaskCompleted() {
        return isCompleted;
    }

    /**
     * refresh migration task status
     *
     * @param execStatus execution status
     */
    public void refresh(Integer execStatus) {
        this.execStatus = execStatus;
    }

    /**
     * DataCheckVo
     */
    @Data
    static class DataCheckVo {
        private String schema;
        private String table;
        private String message;
        private Integer diffCount;
    }
}