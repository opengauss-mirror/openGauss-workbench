/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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
 * MigrationMainTaskServiceImpl.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/service/impl/MigrationMainTaskServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.handler;

import com.alibaba.fastjson.JSON;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;
import org.opengauss.admin.plugin.domain.TranscribeReplayPwdInfo;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * TranscribeReplay task Service
 *
 * @since 2025/01/8
 * @author zzh
 */
@Component
@Slf4j
public class TranscribeReplayHandle {
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschUtil jschUtil;

    /**
     * startTranscribe
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     * @param jarPath jarPath
     * @param pwdInfo pwdInfo
     * @return JschResult
     */
    public JschResult startTranscribe(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask,
                                             String jarPath, TranscribeReplayPwdInfo pwdInfo) {
        return executeTask(shellInfoVo, transcribeReplayTask, jarPath, "transcribe", pwdInfo);
    }

    /**
     * startParse
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     * @param jarPath jarPath
     * @param pwdInfo pwdInfo
     * @return JschResult
     */
    public JschResult startParse(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask,
        String jarPath, TranscribeReplayPwdInfo pwdInfo) {
        return executeTask(shellInfoVo, transcribeReplayTask, jarPath, "parse", pwdInfo);
    }

    /**
     * startReplay
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     * @param jarPath jarPath
     * @param pwdInfo pwdInfo
     * @return JschResult
     */
    public JschResult startReplay(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask,
        String jarPath, TranscribeReplayPwdInfo pwdInfo) {
        return executeTask(shellInfoVo, transcribeReplayTask, jarPath, "replay", pwdInfo);
    }

    private JschResult executeTask(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask,
        String jarPath, String taskType, TranscribeReplayPwdInfo pwdInfo) {
        log.info("Task host info: {}", JSON.toJSONString(shellInfoVo));
        StringBuilder commandSb = new StringBuilder();
        String installPath = getInstallPath(transcribeReplayTask, taskType);
        String configFilePath = installPath + "/config/" + taskType + ".properties";
        commandSb.append("cd ").append(installPath);
        commandSb.append(" && (sudo java -Xms256m -Xmx2g -jar ").append(jarPath);
        commandSb.append(" -t ").append(taskType);
        commandSb.append(" -f ").append(configFilePath);
        commandSb.append(" 2>&1 | tee ").append(String.format("%s_result.log", taskType));
        commandSb.append("; exit ${PIPESTATUS[0]})");
        log.info("Start task {}, host: {}, command: {}", taskType, shellInfoVo.getIp(), commandSb.toString());
        SSHBody sshBody = new SSHBody(shellInfoVo.getIp(),
                shellInfoVo.getPort(),
                shellInfoVo.getUsername(),
                shellInfoVo.getPassword(), null);
        Map<String, String> autoResponse = new HashMap<>();
        autoResponse.put("(remote.receiver.password):", pwdInfo.getRemotePassword());
        autoResponse.put("(general.database.password):", pwdInfo.getSourceNodePassword());
        autoResponse.put("(sql.database.password):", pwdInfo.getTargetNodePassword());
        autoResponse.put("(sql.replay.database.password):", pwdInfo.getTargetNodePassword());
        try {
            return jschUtil.executeCommand(commandSb.toString(), sshBody, autoResponse);
        } catch (IOException | InterruptedException e) {
            throw new OpsException(e.getMessage());
        }
    }

    private String getInstallPath(TranscribeReplayTask transcribeReplayTask, String taskType) {
        switch (taskType.toLowerCase(Locale.ROOT)) {
            case "transcribe":
                return transcribeReplayTask.getSourceInstallPath();
            case "parse":
            case "replay":
                return transcribeReplayTask.getTargetInstallPath();
            default:
                throw new IllegalArgumentException("Unsupported task type: " + taskType);
        }
    }

    /**
     * removeSourcePath
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     * @return JschResult
     */
    public JschResult removeSourcePath(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask) {
        return removePath(shellInfoVo, transcribeReplayTask, "source");
    }

    /**
     * removeTargetPath
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     * @return JschResult
     */
    public JschResult removeTargetPath(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask) {
        return removePath(shellInfoVo, transcribeReplayTask, "target");
    }

    /**
     * removeData
     *
     * @param shellInfoVo shellInfoVo
     * @param transcribeReplayTask transcribeReplayTask
     */
    public void removeData(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask) {
        String removeProcess = String.format("rm -rf %s/%s %s/%s", transcribeReplayTask.getTargetInstallPath(),
            TranscribeReplayConstants.PARSE_PROCESS_FILE, transcribeReplayTask.getTargetInstallPath(),
            TranscribeReplayConstants.REPLAY_PROCESS_FILE);
        String failSqlFile = String.format(TranscribeReplayConstants.FAIL_SQL_FILE_NAME, "*");
        String removeFailSql = String.format("rm -rf %s/%s", transcribeReplayTask.getTargetInstallPath(), failSqlFile);
        String tcpDumpFile = String.format("%s/tcpdump-files/*", getPath(transcribeReplayTask, "target"));
        String removeTcpDumpFile = String.format("rm -rf %s", tcpDumpFile);
        ShellUtil.execCommand(shellInfoVo, removeProcess);
        ShellUtil.execCommand(shellInfoVo, removeFailSql);
        ShellUtil.execCommand(shellInfoVo, removeTcpDumpFile);
    }

    private JschResult removePath(ShellInfoVo shellInfoVo, TranscribeReplayTask transcribeReplayTask,
        String pathType) {
        log.info("Remove {} host info: {}", pathType, JSON.toJSONString(shellInfoVo));
        String pathCmd = getPath(transcribeReplayTask, pathType);
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("rm -rf ").append(pathCmd);
        log.info("remove {} install path, host: {}, command: {}", pathType, shellInfoVo.getIp(), commandSb.toString());
        return ShellUtil.execCommandGetResult(shellInfoVo, commandSb.toString());
    }

    private String getPath(TranscribeReplayTask transcribeReplayTask, String pathType) {
        switch (pathType.toLowerCase(Locale.ROOT)) {
            case "source":
                return getParentPath(transcribeReplayTask.getSourceInstallPath());
            case "target":
                return getParentPath(transcribeReplayTask.getTargetInstallPath());
            default:
                throw new IllegalArgumentException("Unsupported path type: " + pathType);
        }
    }

    private String getParentPath(String path) {
        Path fullPath = Paths.get(path);
        Path parentPath = fullPath.getParent();
        if (parentPath != null) {
            return parentPath.toString().replace("\\", "/");
        } else {
            return path;
        }
    }
}
