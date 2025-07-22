/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.portal;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * MULTI_DB portal directory helper
 *
 * @since 2025/06/23
 */
@Slf4j
public class MultiDbPortalDirHelper {
    /**
     * Get task port file path
     *
     * @param portalInfo portal info
     * @param taskId     task id
     * @return workspace directory path
     */
    public static String getTaskPortFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/config/port", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * get portal log path list
     *
     * @param shellInfo shell info
     * @param portalInfo portal info
     * @param taskId task id
     * @return log path list
     */
    public static List<String> getPortalLogPathList(
            ShellInfoVo shellInfo, MigrationHostPortalInstall portalInfo, int taskId) {
        String command = String.format("find %s | xargs ls -ld | grep '^-' | awk -F ' ' '{print $9}'",
                getPortalLogsDirPath(portalInfo, taskId));
        JschResult logsResult = ShellUtil.execCommandGetResult(shellInfo, command);
        if (logsResult.isOk() && StringUtils.isNotBlank(logsResult.getResult())) {
            String[] pathArr = logsResult.getResult().trim().split("\n");
            if (pathArr.length > 0) {
                return Arrays.stream(pathArr).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    /**
     * Get portal logs directory path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return portal logs directory path
     */
    public static String getPortalLogsDirPath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/logs/", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get reverse status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return reverse status file path
     */
    public static String getReverseStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/reverse/reverse.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get incremental status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return incremental status file path
     */
    public static String getIncrementalStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/incremental/incremental.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full status file path
     */
    public static String getFullTotalStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/total.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full table status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full table status file path
     */
    public static String getFullTableStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/table.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full view status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full view status file path
     */
    public static String getFullViewStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/view.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full function status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full function status file path
     */
    public static String getFullFunctionStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/function.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full trigger status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full trigger status file path
     */
    public static String getFullTriggerStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/trigger.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get full procedure status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return full procedure status file path
     */
    public static String getFullProcedureStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/full/procedure.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get portal status file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return portal status file path
     */
    public static String getPortalStatusFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/migration-status.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get verify result file path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return verify result file path
     */
    public static String getVerifyResultFilePath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format("%s/status/verify.txt", getTaskWorkspaceDirPath(portalInfo, taskId));
    }

    /**
     * Get portal home directory path
     *
     * @param portalInfo portal info
     * @return portal home directory path
     */
    public static String getPortalHomeDirPath(MigrationHostPortalInstall portalInfo) {
        return String.format("%s/portal/", portalInfo.getInstallPath());
    }

    /**
     * Get task workspace directory path
     *
     * @param portalInfo portal info
     * @param taskId task id
     * @return task workspace directory path
     */
    public static String getTaskWorkspaceDirPath(MigrationHostPortalInstall portalInfo, int taskId) {
        return String.format(Locale.ROOT, "%s/workspace/task_%d", getPortalHomeDirPath(portalInfo), taskId);
    }
}
