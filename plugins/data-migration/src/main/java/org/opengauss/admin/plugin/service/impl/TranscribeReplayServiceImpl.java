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
 */

package org.opengauss.admin.plugin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;
import org.opengauss.admin.plugin.config.ConfigTranscribeTcpdumpParams;
import org.opengauss.admin.plugin.config.ConfigTranscribeAttachParams;
import org.opengauss.admin.plugin.config.ConfigTranscribeGeneralParams;
import org.opengauss.admin.plugin.config.ConfigParseParams;
import org.opengauss.admin.plugin.config.ConfigReplayParams;
import org.opengauss.admin.plugin.config.ConfigTcpdumpAndAttachSameParams;
import org.opengauss.admin.plugin.config.ConfigDbStorageParams;
import org.opengauss.admin.plugin.domain.FailSqlModel;
import org.opengauss.admin.plugin.domain.Process;
import org.opengauss.admin.plugin.domain.SlowSqlModel;
import org.opengauss.admin.plugin.domain.SqlDuration;
import org.opengauss.admin.plugin.domain.TranscribeReplayHost;
import org.opengauss.admin.plugin.domain.TranscribeReplayNodeInfo;
import org.opengauss.admin.plugin.domain.TranscribeReplaySlowSql;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskQueryDto;
import org.opengauss.admin.plugin.enums.TranscribeReplaySqlStorageMode;
import org.opengauss.admin.plugin.enums.TranscribeReplayStatus;
import org.opengauss.admin.plugin.enums.TranscribeReplayVersion;
import org.opengauss.admin.plugin.handler.TranscribeReplayHandle;
import org.opengauss.admin.plugin.mapper.TranscribeReplayMapper;
import org.opengauss.admin.plugin.service.TranscribeReplayFailSqlService;
import org.opengauss.admin.plugin.service.TranscribeReplayHostService;
import org.opengauss.admin.plugin.service.TranscribeReplayService;
import org.opengauss.admin.plugin.service.TranscribeReplaySlowSqlService;
import org.opengauss.admin.plugin.utils.FileUtils;
import org.opengauss.admin.plugin.utils.JDBCUtils;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListIterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import java.nio.charset.StandardCharsets;

import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.TCPDUMP;
import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.ATTACH;
import static org.opengauss.admin.plugin.enums.TranscribeReplaySqlTransMode.GENERAL;

/**
 * TranscribeReplay task Service
 *
 * @author zzh
 * @since 2025-02-10
 */
@Service
@Slf4j
public class TranscribeReplayServiceImpl extends ServiceImpl<TranscribeReplayMapper, TranscribeReplayTask>
    implements TranscribeReplayService, TranscribeReplayConstants {
    final String lastedVersionUrl = "https://opengauss.obs.cn-south-1.myhuaweicloud.com/latest/tools/"
            + "transcribe-replay-tool-" + TranscribeReplayVersion.LATEST.getVersion() + ".tar.gz";
    final String configFilePath = "/transcribe-replay-tool/config/";
    final String pluginPath = "/transcribe-replay-tool/plugin";
    final String transcribeConfigFile = "transcribe.properties";
    final String parseConfigFile = "parse.properties";
    final String replayConfigFile = "replay.properties";
    final String formatString = "%s=%s" + System.lineSeparator();
    private Integer downloadId;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private TranscribeReplayHostService transcribeReplayHostService;
    @Autowired
    private TranscribeReplayMapper transcribeReplayMapper;
    @Autowired
    private TranscribeReplaySlowSqlService transcribeReplaySlowSqlService;
    @Autowired
    private TranscribeReplayFailSqlService transcribeReplayFailSqlService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostUserService hostUserService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsJdbcDbClusterNodeService opsJdbcDbClusterNodeService;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;

    @Override
    public List<String> getToolsVersion() {
        return Arrays.stream(TranscribeReplayVersion.values())
            .map(TranscribeReplayVersion::getVersion)
            .collect(Collectors.toList());
    }

    @Override
    public void downloadAndConfig(TranscribeReplayTaskDto tp, Integer id, Map<String, String> config) {
        this.downloadId = id;
        String url = lastedVersionUrl;
        String taskType = tp.getTaskType();
        boolean isTranscribe = taskType.contains("transcribe");
        boolean isReplay = taskType.contains("replay");
        boolean isGeneral = GENERAL.getMode().equals(config.get(SQL_TRANSCRIBE_MODE));
        if (!isTranscribe && !isReplay) {
            throw new OpsException("An unknown error occurred while recording playback.");
        }
        threadPoolTaskExecutor.submit(() -> {
            // In the general mode of transcribe and replay,
            // both the recording and playback processes occur on the source end,
            // thus special handling is required for the following conditions.
            if (isGeneral || isTranscribe) {
                download(tp, createSourceShellInfo(tp), id, url, tp.getSourceInstallPath());
                addSourceConfigList(tp, id, config);
            }
            if (!isGeneral) {
                download(tp, createTargetShellInfo(tp), id, url, tp.getTargetInstallPath());
                addTargetConfigList(tp, id, config);
            }
        });
    }

    private ShellInfoVo createSourceShellInfo(TranscribeReplayTaskDto tp) {
        OpsHostEntity opsHostEntity = hostService.getByPublicIp(tp.getSourceIp());
        OpsHostUserEntity opsHostUserEntity = hostUserService.getHostUserByUsername(opsHostEntity.getHostId(),
            tp.getSourceUser());
        String ip = tp.getSourceIp();
        int port = opsHostEntity.getPort();
        String user = tp.getSourceUser();
        String password = encryptionUtils.decrypt(opsHostUserEntity.getPassword());
        return new ShellInfoVo(ip, port, user, password);
    }

    private ShellInfoVo createTargetShellInfo(TranscribeReplayTaskDto tp) {
        OpsHostEntity opsHostEntity = hostService.getByPublicIp(tp.getTargetIp());
        OpsHostUserEntity opsHostUserEntity = hostUserService.getHostUserByUsername(opsHostEntity.getHostId(),
            tp.getTargetUser());
        String ip = tp.getTargetIp();
        int port = opsHostEntity.getPort();
        String user = tp.getTargetUser();
        String password = encryptionUtils.decrypt(opsHostUserEntity.getPassword());
        return new ShellInfoVo(ip, port, user, password);
    }

    private void download(TranscribeReplayTaskDto tp, ShellInfoVo shellInfo, Integer id, String url,
                          String installPath) {
        String createPathCommand = String.format("mkdir %s", installPath);
        String downloadCommand = String.format("rm -rf %s/%s;source /etc/profile;wget %s -P %s/%s", installPath,
            downloadId, url, installPath, downloadId);
        JschResult createPath = ShellUtil.execCommandGetResult(shellInfo, createPathCommand);
        TranscribeReplayTask transcribeReplayTask = getById(id);
        if (createPath.getResult().contains("Permission denied")) {
            transcribeReplayTask.setErrorMsg("Permission denied.");
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("Permission denied!");
        }
        JschResult download = ShellUtil.execCommandGetResult(shellInfo, downloadCommand);
        if (!download.isOk()) {
            log.info("DownloadCommand:" + downloadCommand);
            transcribeReplayTask.setErrorMsg("Download fail." + download.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("Download fail." + download.getResult());
        }
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
        log.info("Download success!");
        unZip(installPath, shellInfo, tp.getToolVersion(), id);
        createDictory(installPath, shellInfo, id);
    }

    private void createDictory(String path, ShellInfoVo shellInfo, Integer id) {
        String command = String.format("cd %s/%s;mkdir %s %s", path, downloadId, PCAP_FILE_PATH, JSON_PATH);
        JschResult createDir = ShellUtil.execCommandGetResult(shellInfo, command);
        TranscribeReplayTask transcribeReplayTask = getById(id);
        if (!createDir.isOk()) {
            transcribeReplayTask.setErrorMsg("create sql-files tcpdump-files fail!" + createDir.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("create sql-files tcpdump-files fail!" + createDir.getResult());
        }
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
        log.info("create sql-files tcpdump-files success");
    }

    private void unZip(String path, ShellInfoVo shellInfo, String toolVersion, Integer id) {
        String toolsName = String.format("transcribe-replay-tool-%s.tar.gz", toolVersion);
        String uzipFileCommand = String.format("tar -zxvf %s/%s/%s -C %s/%s", path, downloadId,
            toolsName, path, downloadId);
        JschResult unzip = ShellUtil.execCommandGetResult(shellInfo, uzipFileCommand);
        TranscribeReplayTask transcribeReplayTask = getById(id);
        if (!unzip.isOk()) {
            transcribeReplayTask.setErrorMsg("Unzip fail!" + unzip.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("Unzip fail!" + unzip.getResult());
        }
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
        log.info("UnZip success");
    }

    private ShellInfoVo sourceShellInfo(TranscribeReplayTaskDto tp) {
        OpsHostEntity sourceOpsHostEntity = hostService.getByPublicIp(tp.getSourceIp());
        OpsHostUserEntity sourceOpsHostUserEntity = hostUserService.getHostUserByUsername(
            sourceOpsHostEntity.getHostId(), tp.getSourceUser());
        return new ShellInfoVo(tp.getSourceIp(), sourceOpsHostEntity.getPort(), tp.getSourceUser(),
            encryptionUtils.decrypt(sourceOpsHostUserEntity.getPassword()));
    }

    private ShellInfoVo targetShellInfo(TranscribeReplayTaskDto tp) {
        OpsHostEntity targetOpsHostEntity = hostService.getByPublicIp(tp.getTargetIp());
        OpsHostUserEntity targetOpsHostUserEntity = hostUserService.getHostUserByUsername(
            targetOpsHostEntity.getHostId(), tp.getTargetUser());
        return new ShellInfoVo(tp.getTargetIp(), targetOpsHostEntity.getPort(), tp.getTargetUser(),
            encryptionUtils.decrypt(targetOpsHostUserEntity.getPassword()));
    }

    private void addSourceConfigList(TranscribeReplayTaskDto tp, Integer id, Map<String, String> config) {
        String configPath = tp.getSourceInstallPath() + "/" + downloadId + configFilePath;
        String transcribeRemotePath = configPath + transcribeConfigFile;
        String generalReplayRemotePath = configPath + replayConfigFile;
        String fileExistCommand = String.format("[ -e %s ]", transcribeRemotePath);
        JschResult fileExist = ShellUtil.execCommandGetResult(sourceShellInfo(tp), fileExistCommand);
        TranscribeReplayTask transcribeReplayTask = getById(id);
        if (!fileExist.isOk()) {
            transcribeReplayTask.setErrorMsg("The addSourceConfigList fail!" + fileExist.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("The addSourceConfigList fail!" + fileExist.getResult());
        } else {
            ShellUtil.updateFileContent(sourceShellInfo(tp), getTranscribeConfig(tp, config), transcribeRemotePath);
            if (GENERAL.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
                ShellUtil.updateFileContent(sourceShellInfo(tp), getReplayConfig(tp, config), generalReplayRemotePath);
            }
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
            log.info("The addSourceConfigList success!");
        }
    }

    private void addTargetConfigList(TranscribeReplayTaskDto tp, Integer id, Map<String, String> config) {
        String configPath = tp.getTargetInstallPath() + "/" + downloadId + configFilePath;
        if (TCPDUMP.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            String parseRemotePath = configPath + parseConfigFile;
            writeTargetFile(parseRemotePath, tp, id, parseConfigFile, getParseConfig(tp, config));
        }
        if (!GENERAL.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            String replayRemotePath = configPath + replayConfigFile;
            writeTargetFile(replayRemotePath, tp, id, replayConfigFile, getReplayConfig(tp, config));
        }
    }

    private void writeTargetFile(String remotePath, TranscribeReplayTaskDto tp, Integer id, String fileName,
                           String context) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        String fileExistCommand = String.format("[ -e %s ]", remotePath);
        JschResult fileExist = ShellUtil.execCommandGetResult(targetShellInfo(tp), fileExistCommand);
        if (!fileExist.isOk()) {
            transcribeReplayTask.setErrorMsg("The replay.properties is not exist!" + fileExist.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("The " + fileName + " is not exist!" + fileExist.getResult());
        } else {
            ShellUtil.updateFileContent(targetShellInfo(tp), context, remotePath);
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
            log.info("The " + fileName + " file has been successfully processed.");
        }
    }

    private String getTranscribeConfig(TranscribeReplayTaskDto tp, Map<String, String> config) {
        if (TCPDUMP.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            return getTcpDumpConfig(tp, config);
        } else if (ATTACH.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            return getAttachConfig(tp, config);
        } else if (GENERAL.getMode().equals(config.get(SQL_TRANSCRIBE_MODE))) {
            return getGeneralLog(tp, config);
        } else {
            throw new OpsException("sql.transcribe.mode does not match.");
        }
    }

    private String getTcpDumpConfig(TranscribeReplayTaskDto tp, Map<String, String> config) {
        JschResult jschResult = ShellUtil.execCommandGetResult(sourceShellInfo(tp), "which tcpdump");
        String plugPath;
        OpsHostEntity sourceOpsHostEntity = hostService.getByPublicIp(tp.getSourceIp());
        String taskPath = tp.getSourceInstallPath() + "/" + downloadId;
        if (jschResult.getExitCode() == 0) {
            plugPath = jschResult.getResult();
            int lastIndex = plugPath.lastIndexOf('/');
            plugPath = plugPath.substring(0, lastIndex).replaceAll("[\\r\\n]+", "");
        } else {
            plugPath = taskPath + pluginPath + "/" + sourceOpsHostEntity.getCpuArch();
        }
        ConfigTranscribeTcpdumpParams params = new ConfigTranscribeTcpdumpParams();
        params.setTcpDumpConfig(tp, config, plugPath, downloadId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatString, SQL_TRANSCRIBE_MODE, params.getSqlTranscribeMode()));
        sb.append(String.format(formatString, TCPDUMP_NETWORK_INTERFACE, params.getTcpdumpNetworkInterface()));
        sb.append(String.format(formatString, TCPDUMP_CAPTURE_DURATION, params.getTcpdumpCaptureDuration()));
        sb.append(String.format(formatString, TCPDUMP_FILE_NAME, params.getTcpdumpFileName()));
        sb.append(String.format(formatString, TCPDUMP_FILE_SIZE, params.getTcpdumpFileSize()));
        sb.append(String.format(formatString, TCPDUMP_FILE_PATH, params.getTcpdumpFilePath()));
        sb.append(String.format(formatString, FILE_COUNT_LIMIT, params.getFileCountLimit()));
        sb.append(String.format(formatString, TCPDUMP_PLUGIN_PATH, params.getTcpdumpPluginPath()));
        sb.append(String.format(formatString, TCPDUMP_DATABASE_PORT, params.getTcpdumpDatabasePort()));
        sb.append(getTcpdumpAndAttachSameConfig(config, tp));
        return String.valueOf(sb);
    }

    private String getAttachConfig(TranscribeReplayTaskDto tp, Map<String, String> config) {
        StringBuilder sb = new StringBuilder();
        ConfigTranscribeAttachParams params = new ConfigTranscribeAttachParams();
        params.setAttachConfig(tp, config, downloadId);
        sb.append(String.format(formatString, SQL_TRANSCRIBE_MODE, params.getSqlTranscribeMode()));
        sb.append(String.format(formatString, ATTACH_PROCESS_PID, params.getAttachProcessPid()));
        sb.append(String.format(formatString, ATTACH_TARGET_SCHEMA, params.getAttachTargetSchema()));
        sb.append(String.format(formatString, ATTACH_CAPTURE_DURATION, params.getAttachCaptureDuration()));
        sb.append(String.format(formatString, FILE_COUNT_LIMIT, params.getFileCountLimit()));
        sb.append(String.format(formatString, SQL_FILE_NAME, params.getSqlFileName()));
        sb.append(String.format(formatString, SQL_FILE_SIZE, params.getSqlFileSize()));
        sb.append(String.format(formatString, ATTACH_PLUGIN_PATH, params.getAttachPluginPath()));
        sb.append(String.format(formatString, SQL_FILE_PATH, params.getSqlFilePath()));
        sb.append(getTcpdumpAndAttachSameConfig(config, tp));
        return String.valueOf(sb);
    }

    private String getTcpdumpAndAttachSameConfig(Map<String, String> config, TranscribeReplayTaskDto tp) {
        StringBuilder sb = new StringBuilder();
        OpsHostEntity targetOpsHostEntity = hostService.getByPublicIp(tp.getTargetIp());
        OpsHostUserEntity targetOpsHostUserEntity = hostUserService.getHostUserByUsername(
            targetOpsHostEntity.getHostId(), tp.getTargetUser());
        ConfigTcpdumpAndAttachSameParams params = new ConfigTcpdumpAndAttachSameParams();
        params.setTcpAndAttachSameConfig(tp, config, downloadId, targetOpsHostEntity, targetOpsHostUserEntity);
        String remotePassword = encryptionUtils.decrypt(params.getRemoteReceiverPassword());
        sb.append(String.format(formatString, SHOULD_CHECK_SYSTEM, params.getShouldCheckSystem()));
        sb.append(String.format(formatString, MAX_CPU_THRESHOLD, params.getMaxCpuThreshold()));
        sb.append(String.format(formatString, MAX_MEMORY_THRESHOLD, params.getMaxMemoryThreshold()));
        sb.append(String.format(formatString, MAX_DISK_THRESHOLD, params.getMaxDiskThreshold()));
        sb.append(String.format(formatString, SHOULD_SEND_FILE, params.getShouldSendFile()));
        sb.append(String.format(formatString, REMOTE_RETRY_COUNT, params.getRemoteRetryCount()));
        sb.append(String.format(formatString, REMOTE_RECEIVER_NAME, params.getRemoteReceiverName()));
        sb.append(String.format(formatString, REMOTE_RECEIVER_PASSWORD, remotePassword));
        sb.append(String.format(formatString, REMOTE_FILE_PATH, params.getRemoteFilePath()));
        sb.append(String.format(formatString, REMOTE_NODE_IP, params.getRemoteNodeIp()));
        sb.append(String.format(formatString, REMOTE_NODE_PORT, params.getRemoteNodePort()));
        return String.valueOf(sb);
    }

    private String getGeneralLog(TranscribeReplayTaskDto tp, Map<String, String> config) {
        StringBuilder sb = new StringBuilder();
        ConfigTranscribeGeneralParams params = new ConfigTranscribeGeneralParams();
        params.setGeneralConfig(tp, config, downloadId);
        sb.append(String.format(formatString, SQL_TRANSCRIBE_MODE, params.getSqlTranscribeMode()));
        sb.append(String.format(formatString, FILE_COUNT_LIMIT, params.getFileCountLimit()));
        sb.append(String.format(formatString, GENERAL_SQL_BATCH, params.getSqlBatch()));
        sb.append(String.format(formatString, GENERAL_START_TIME, params.getStartTime()));
        if (TranscribeReplaySqlStorageMode.DB.getMode().equals(params.getSqlStorageMode())) {
            sb.append(String.format(formatString, SQL_TABLE_DROP, params.getSqlTableDrop()));
            sb.append(getDbStorageConfig(config));
        } else {
            sb.append(String.format(formatString, SQL_FILE_PATH, params.getSqlFilePath()));
        }
        sb.append(String.format(formatString, GENERAL_DATABASE_IP, params.getDatabaseIp()));
        sb.append(String.format(formatString, GENERAL_DATABASE_PORT, params.getDatabasePort()));
        sb.append(String.format(formatString, GENERAL_DATABASE_USERNAME, params.getDatabaseUsername()));
        String databasePassword = encryptionUtils.decrypt(params.getDatabasePassword());
        sb.append(String.format(formatString, GENERAL_DATABASE_PASSWORD, databasePassword));
        return String.valueOf(sb);
    }

    private String getDbStorageConfig(Map<String, String> config) {
        ConfigDbStorageParams params = new ConfigDbStorageParams();
        params.setDbStorConfig(config);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatString, SQL_DATABASE_IP, params.getSqlDatabaseIp()));
        sb.append(String.format(formatString, SQL_DATABASE_PORT, params.getSqlDatabasePort()));
        sb.append(String.format(formatString, SQL_DATABASE_USERNAME, params.getSqlDatabaseUsername()));
        String databasePassword = params.getSqlDatabasePassword();
        sb.append(String.format(formatString, SQL_DATABASE_PASSWORD, databasePassword));
        sb.append(String.format(formatString, SQL_DATABASE_NAME, params.getSqlDatabaseName()));
        sb.append(String.format(formatString, SQL_TABLE_NAME, params.getSqlTableName()));
        return String.valueOf(sb);
    }

    private String getTaskPathById(TranscribeReplayTaskDto tp, Map<String, String> config) {
        String taskPath;
        if (!StringUtil.isEmpty(config.get(TCPDUMP_FILE_ID))) {
            String passwordObj = config.get(TCPDUMP_FILE_ID);
            TranscribeReplayTask byId = getById(Integer.valueOf(passwordObj));
            int lastIndex = byId.getTargetInstallPath().lastIndexOf('/');
            taskPath = byId.getTargetInstallPath().substring(0, lastIndex);
        } else {
            taskPath = tp.getTargetInstallPath() + "/" + downloadId;
        }
        return taskPath;
    }

    private String getParseConfig(TranscribeReplayTaskDto tp, Map<String, String> config) {
        StringBuilder sb = new StringBuilder();
        ConfigParseParams params = new ConfigParseParams();
        params.setParseConfig(tp, config, downloadId, getTaskPathById(tp, config));
        sb.append(String.format(formatString, PARSE_SELECT_RESULT, params.getParseSelectResult()));
        sb.append(String.format(formatString, SELECT_RESULT_PATH, params.getSelectResultPath()));
        sb.append(String.format(formatString, RESULT_FILE_NAME, params.getResultFileName()));
        sb.append(String.format(formatString, RESULT_FILE_SIZE, params.getResultFileSize()));
        sb.append(String.format(formatString, SQL_STORAGE_MODE, params.getSqlStorageMode()));
        sb.append(String.format(formatString, TCPDUMP_DATABASE_TYPE, params.getTcpdumpDatabaseType()));
        sb.append(String.format(formatString, QUEUE_SIZE_LIMIT, params.getQueueSizeLimit()));
        sb.append(String.format(formatString, PACKET_BATCH_SIZE, params.getPacketBatchSize()));
        sb.append(String.format(formatString, TCPDUMP_FILE_PATH, params.getTcpdumpFilePath()));
        sb.append(String.format(formatString, TCPDUMP_DATABASE_IP, params.getTcpdumpDatabaseIp()));
        sb.append(String.format(formatString, TCPDUMP_DATABASE_PORT, params.getTcpdumpDatabasePort()));
        if (TranscribeReplaySqlStorageMode.JSON.getMode().equals(params.getSqlStorageMode())) {
            sb.append(String.format(formatString, SQL_FILE_PATH, params.getSqlFilePath()));
            sb.append(String.format(formatString, SQL_FILE_SIZE, params.getSqlFileSize()));
            sb.append(String.format(formatString, SQL_FILE_NAME, params.getSqlFileName()));
        } else {
            sb.append(String.format(formatString, SQL_TABLE_DROP, params.getSqlTableDrop()));
            sb.append(getDbStorageConfig(config));
        }
        return String.valueOf(sb);
    }

    private String getReplayConfig(TranscribeReplayTaskDto tp, Map<String, String> config) {
        ConfigReplayParams params = new ConfigReplayParams();
        params.setRepConfig(tp, config, addDbMapSchema(tp.getDbMap()), downloadId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatString, SQL_STORAGE_MODE, params.getSqlStorageMode()));
        sb.append(String.format(formatString, SQL_REPLAY_MULTIPLE, params.getSqlReplayMultiple()));
        sb.append(String.format(formatString, SQL_REPLAY_ONLY_QUERY, params.getSqlReplayOnlyQuery()));
        sb.append(String.format(formatString, SQL_REPLAY_STRATEGY, params.getSqlReplayStrategy()));
        sb.append(String.format(formatString, SQL_REPLAY_PARALLEL_MAX_POOL_SIZE,
            params.getSqlReplayParallelMaxPoolSize()));
        sb.append(String.format(formatString, REPLAY_MAX_TIME, params.getReplayMaxTime()));
        sb.append(String.format(formatString, SQL_REPLAY_SLOW_SQL_RULE, params.getSqlReplaySlowSqlRule()));
        sb.append(String.format(formatString, SQL_REPLAY_SLOW_TIME_DIFFERENCE_THRESHOLD,
            params.getSqlReplaySlowTimeDifferenceThreshold()));
        sb.append(String.format(formatString, SQL_REPLAY_SLOW_SQL_DURATION_THRESHOLD,
            params.getSqlReplaySlowSqlDurationThreshold()));
        sb.append(String.format(formatString, SQL_REPLAY_SLOW_TOP_NUMBER, params.getSqlReplaySlowTopNumber()));
        sb.append(String.format(formatString, SQL_REPLAY_DRAW_INTERVAL, params.getSqlReplayDrawInterval()));
        sb.append(String.format(formatString, SQL_REPLAY_SESSION_WHITE_LIST, params.getSqlReplaySessionWhiteList()));
        sb.append(String.format(formatString, SQL_REPLAY_SESSION_BLACK_LIST, params.getSqlReplaySessionBlackList()));
        sb.append(String.format(formatString, SQL_REPLAY_DATABASE_USERNAME, params.getSqlReplayDatabaseUsername()));
        String databasePassword = encryptionUtils.decrypt(params.getSqlReplayDatabasePassword());
        sb.append(String.format(formatString, SQL_REPLAY_DATABASE_PASSWORD, databasePassword));
        sb.append(String.format(formatString, SQL_REPLAY_SLOW_SQL_CSV_DIR, params.getSqlReplaySlowSqlCsvDir()));
        sb.append(String.format(formatString, SQL_REPLAY_DATABASE_IP, params.getSqlReplayDatabaseIp()));
        sb.append(String.format(formatString, SQL_REPLAY_DATABASE_PORT, params.getSqlReplayDatabasePort()));
        sb.append(String.format(formatString, SQL_REPLAY_DATABASE_SCHEMA_MAP,
            params.getSqlReplayDatabaseSchemaMap()));
        sb.append(String.format(formatString, SQL_FILE_PATH, params.getSqlFilePath()));
        sb.append(String.format(formatString, SQL_FILE_NAME, params.getSqlFileName()));
        return String.valueOf(sb);
    }

    private String addDbMapSchema(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        ListIterator<String> iterator = list.listIterator();
        while (iterator.hasNext()) {
            String original = iterator.next();
            stringBuilder.append(original);
            stringBuilder.append(".");
            stringBuilder.append(original.split(":")[0]);
            stringBuilder.append(";");
        }
        return String.valueOf(stringBuilder);
    }

    /**
     * save transcribe replay task
     *
     * @param taskDto taskDto
     * @return Integer
     */
    @Override
    public Integer saveTask(TranscribeReplayTaskDto taskDto) {
        log.info("Save task info: {}, type: {}", taskDto.getTaskName(), taskDto.getTaskType());
        if (Objects.nonNull(getTaskByName(taskDto.getTaskName()))) {
            throw new OpsException("The task" + taskDto.getTaskName() + "already exists, please rename it.");
        }
        TranscribeReplayTask transcribeReplayTask = new TranscribeReplayTask();
        BeanUtil.copyProperties(taskDto, transcribeReplayTask);
        transcribeReplayTask.setExecutionStatus(TranscribeReplayStatus.DOWNLOADING.getCode());
        String join = String.join(";", taskDto.getDbMap());
        transcribeReplayTask.setDbName(join);
        save(transcribeReplayTask);
        updateInstallPath(transcribeReplayTask);
        saveTranscribeHost(transcribeReplayTask, taskDto.getSourceIp(), taskDto.getSourceUser());
        saveReplayHost(transcribeReplayTask, taskDto.getTargetIp(), taskDto.getTargetUser());
        log.info("Success to save task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
        return transcribeReplayTask.getId();
    }

    @Override
    public AjaxResult startTask(Integer id) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUNNING);
        if (TRANSCRIBE.equalsIgnoreCase(transcribeReplayTask.getTaskType())) {
            threadPoolTaskExecutor.submit(() -> startTranscribe(id, transcribeReplayTask));
        } else if (REPLAY.equalsIgnoreCase(transcribeReplayTask.getTaskType())) {
            threadPoolTaskExecutor.submit(() -> startReplay(id, transcribeReplayTask));
        } else {
            runTask(id, transcribeReplayTask);
        }
        return AjaxResult.success();
    }

    @Override
    public void finishTask(Integer id) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        log.info("Finish task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
        killTask(id);
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.FINISH);
        log.info("Success to finish task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
    }

    @Override
    public void deleteTask(Integer[] ids) {
        Arrays.asList(ids).stream().forEach(id -> {
            TranscribeReplayTask transcribeReplayTask = getById(id);
            ShellInfoVo sourceShell = getShellInfo(id, transcribeReplayTask.getSourceDbType());
            if (Objects.nonNull(sourceShell)) {
                JschResult result1 = TranscribeReplayHandle.removeSourcePath(sourceShell, transcribeReplayTask);
                if (!result1.isOk()) {
                    transcribeReplayTask.setErrorMsg(result1.getResult());
                    updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
                    throw new OpsException(result1.getResult());
                }
            }
            ShellInfoVo targetShell = getShellInfo(id, TARGET_DB);
            if (Objects.nonNull(targetShell)) {
                JschResult result2 = TranscribeReplayHandle.removeTargetPath(targetShell, transcribeReplayTask);
                if (!result2.isOk()) {
                    transcribeReplayTask.setErrorMsg(result2.getResult());
                    updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
                    throw new OpsException(result2.getResult());
                }
            }
            removeById(id);
            transcribeReplayHostService.deleteByTaskId(id);
            killTask(id);
        });
    }

    @Override
    public IPage<TranscribeReplayTask> selectList(Page startPage, TranscribeReplayTaskQueryDto taskQueryDto) {
        LambdaQueryWrapper<TranscribeReplayTask> lambdaQuery = Wrappers.lambdaQuery(TranscribeReplayTask.class);
        LambdaQueryWrapper<TranscribeReplayTask> queryWrapper = lambdaQuery.like(
            StrUtil.isNotEmpty(taskQueryDto.getTaskName()), TranscribeReplayTask::getTaskName,
            taskQueryDto.getTaskName())
            .eq(Objects.nonNull(taskQueryDto.getTaskId()), TranscribeReplayTask::getId, taskQueryDto.getTaskId())
            .eq(Objects.nonNull(taskQueryDto.getExecutionStatus()), TranscribeReplayTask::getExecutionStatus,
                taskQueryDto.getExecutionStatus())
            .ge(Objects.nonNull(taskQueryDto.getTaskDuration()), TranscribeReplayTask::getTaskDuration,
                taskQueryDto.getTaskDuration())
            .ge(Objects.nonNull(taskQueryDto.getTaskStartTime()), TranscribeReplayTask::getTaskStartTime,
                taskQueryDto.getTaskStartTime())
            .le(Objects.nonNull(taskQueryDto.getTaskEndTime()), TranscribeReplayTask::getTaskEndTime,
                taskQueryDto.getTaskEndTime())
            .orderByDesc(TranscribeReplayTask::getId);
        Page page = page(startPage, queryWrapper);
        List<TranscribeReplayTask> records = page.getRecords();
        for (TranscribeReplayTask transcribeReplayTask : records) {
            List<String> list = Arrays.stream(transcribeReplayTask.getDbName().split(";")).collect(Collectors.toList());
            transcribeReplayTask.setDbMap(list);
        }
        return page;
    }

    @Override
    public IPage<TranscribeReplaySlowSql> getSlowSql(Page startPage, Integer id, String sql) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        threadPoolTaskExecutor.submit(() -> syncRefreshSlowSql(transcribeReplayTask));
        threadPoolTaskExecutor.submit(() -> refreshSqlNum(transcribeReplayTask));
        return transcribeReplaySlowSqlService.selectList(startPage, id, sql);
    }

    @Override
    public IPage<FailSqlModel> getFailSql(Page startPage, Integer id, String sql) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        transcribeReplayFailSqlService.syncRefreshFailSql(transcribeReplayTask);
        threadPoolTaskExecutor.submit(() -> refreshSqlNum(transcribeReplayTask));
        return transcribeReplayFailSqlService.selectList(startPage, id, sql);
    }

    @Override
    public List<TranscribeReplayHost> getHostInfo(int taskId) {
        return transcribeReplayHostService.getHostsByTaskId(taskId);
    }

    @Override
    public SqlDuration getSqlDuration(Integer taskId) {
        TranscribeReplayTask transcribeReplayTask = getById(taskId);
        ShellInfoVo shellInfo = getShellInfo(transcribeReplayTask.getId(), TARGET_DB);
        String filePath = String.format("%s/%s", transcribeReplayTask.getTargetInstallPath(), DURATION_FILE);
        boolean isFileExists = FileUtils.isRemoteFileExists(filePath, shellInfo);
        if (!isFileExists) {
            throw new OpsException("Duration.json file is not exists!");
        }
        String durationStr = FileUtils.catRemoteFileContents(filePath, shellInfo);
        if (durationStr.isEmpty()) {
            throw new OpsException("Duration.json file is empty!");
        }
        return JSON.parseObject(durationStr, SqlDuration.class);
    }

    private TranscribeReplayTask getTaskByName(String taskName) {
        LambdaQueryWrapper<TranscribeReplayTask> lambdaQuery = Wrappers.lambdaQuery(TranscribeReplayTask.class);
        lambdaQuery.eq(StrUtil.isNotEmpty(taskName), TranscribeReplayTask::getTaskName, taskName);
        return getOne(lambdaQuery);
    }

    private void updateInstallPath(TranscribeReplayTask transcribeReplayTask) {
        transcribeReplayTask.setSourceInstallPath(
            String.format("%s/%s/%s", transcribeReplayTask.getSourceInstallPath(), transcribeReplayTask.getId(),
                TOOL_PATH));
        transcribeReplayTask.setTargetInstallPath(
            String.format("%s/%s/%s", transcribeReplayTask.getTargetInstallPath(), transcribeReplayTask.getId(),
                TOOL_PATH));
        updateById(transcribeReplayTask);
    }

    private void startTranscribe(Integer id, TranscribeReplayTask transcribeReplayTask) {
        log.info("Start task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
        ShellInfoVo shellInfo1 = getShellInfo(id, transcribeReplayTask.getSourceDbType());
        String jarName = String.format(JAR_NAME, transcribeReplayTask.getToolVersion());
        String sourceJarPath = transcribeReplayTask.getSourceInstallPath() + jarName;
        JschResult result = TranscribeReplayHandle.startTranscribe(shellInfo1, transcribeReplayTask, sourceJarPath);
        if (!result.isOk() || checkResult(result.getResult())) {
            transcribeReplayTask.setErrorMsg(result.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
            killTask(transcribeReplayTask.getId());
            log.error("Failed to tun transcribe, error is {}", result.getResult());
            throw new OpsException(result.getResult());
        }
        log.info("Success to run transcribe.");
        String targetJarPath = transcribeReplayTask.getTargetInstallPath() + jarName;
        ShellInfoVo shellInfo2 = getShellInfo(id, TARGET_DB);
        String transcribeFile = String.format("%s/config/transcribe.properties",
            transcribeReplayTask.getSourceInstallPath());
        if ("tcpdump".equals(getTranscribeMode(FileUtils.catRemoteFileContents(transcribeFile, shellInfo1)))) {
            JschResult parse = TranscribeReplayHandle.startParse(shellInfo2, transcribeReplayTask, targetJarPath);
            if (!parse.isOk()) {
                transcribeReplayTask.setErrorMsg(parse.getResult());
                updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
                killTask(transcribeReplayTask.getId());
                log.error("Failed to tun parse, error is {}", parse.getResult());
                throw new OpsException(parse.getResult());
            }
            log.info("Success to run parse.");
        }
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.FINISH);
        log.info("Success to start task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
    }

    private boolean checkResult(String result) {
        return result.contains("Transcribe will be stopped.");
    }

    private void startReplay(Integer id, TranscribeReplayTask transcribeReplayTask) {
        log.info("Start task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
        ShellInfoVo sourceShell = getShellInfo(id, transcribeReplayTask.getSourceDbType());
        ShellInfoVo targetShell = getShellInfo(id, TARGET_DB);
        String transcribeFile = String.format("%s/config/transcribe.properties",
            transcribeReplayTask.getSourceInstallPath());
        if ("general".equals(getTranscribeMode(FileUtils.catRemoteFileContents(transcribeFile, sourceShell)))) {
            transcribeReplayTask.setTargetInstallPath(transcribeReplayTask.getSourceInstallPath());
            targetShell = sourceShell;
        }
        String jarName = String.format(JAR_NAME, transcribeReplayTask.getToolVersion());
        String jarPath = transcribeReplayTask.getTargetInstallPath() + jarName;
        JschResult result = TranscribeReplayHandle.startReplay(targetShell, transcribeReplayTask, jarPath);
        if (!result.isOk()) {
            transcribeReplayTask.setErrorMsg(result.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
            killTask(transcribeReplayTask.getId());
            log.error("Failed to tun replay, error is {}", result.getResult());
            throw new OpsException(result.getResult());
        }
        setResult(transcribeReplayTask);
        log.info("Success to tun replay.");
        updateStatus(transcribeReplayTask, TranscribeReplayStatus.FINISH);
        log.info("Success to start task info: {}, type: {}", transcribeReplayTask.getTaskName(),
            transcribeReplayTask.getTaskType());
    }

    private void runTask(Integer id, TranscribeReplayTask transcribeReplayTask) {
        log.info("Run task info: {}, type: {}", transcribeReplayTask.getTaskName(), transcribeReplayTask.getTaskType());
        ShellInfoVo sourceShell = getShellInfo(id, transcribeReplayTask.getSourceDbType());
        String jarName = String.format(JAR_NAME, transcribeReplayTask.getToolVersion());
        String sourceJarPath = transcribeReplayTask.getSourceInstallPath() + jarName;
        JschResult result = TranscribeReplayHandle.startTranscribe(sourceShell, transcribeReplayTask, sourceJarPath);
        if (!result.isOk() || checkResult(result.getResult())) {
            transcribeReplayTask.setErrorMsg(result.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
            killTask(transcribeReplayTask.getId());
            log.error("Failed to tun transcribe, error is {}", result.getResult());
            throw new OpsException(result.getResult());
        }
        ShellInfoVo targetShell = getShellInfo(id, TARGET_DB);
        String targetJarPath = transcribeReplayTask.getTargetInstallPath() + jarName;
        String transcribeFile = String.format("%s/config/transcribe.properties",
            transcribeReplayTask.getSourceInstallPath());
        if ("tcpdump".equals(getTranscribeMode(FileUtils.catRemoteFileContents(transcribeFile, sourceShell)))) {
            JschResult result2 = TranscribeReplayHandle.startParse(targetShell, transcribeReplayTask, targetJarPath);
            if (!result2.isOk()) {
                transcribeReplayTask.setErrorMsg(result2.getResult());
                updateStatus(transcribeReplayTask, TranscribeReplayStatus.RUN_FAIL);
                killTask(transcribeReplayTask.getId());
                log.error("Failed to tun parse, error is {}", result2.getResult());
                throw new OpsException(result2.getResult());
            }
        }
        startReplay(id, transcribeReplayTask);
        log.info("End task info: {}", JSON.toJSONString(transcribeReplayTask.getTaskName()));
    }

    private void updateStatus(TranscribeReplayTask task, TranscribeReplayStatus taskStatus) {
        task.setExecutionStatus(taskStatus.getCode());
        if (TranscribeReplayStatus.RUNNING.getCode().equals(taskStatus.getCode())) {
            task.setTaskStartTime(new Date());
        }
        if (TranscribeReplayStatus.FINISH.getCode().equals(taskStatus.getCode())) {
            Date end = new Date();
            task.setTaskEndTime(end);
            long duration = end.getTime() - task.getTaskStartTime().getTime();
            task.setTaskDuration(duration);
        }
        if (TranscribeReplayStatus.RUN_FAIL.getCode().equals(taskStatus.getCode())) {
            task.setTaskEndTime(new Date());
        }
        updateById(task);
    }

    @Override
    public ShellInfoVo getShellInfo(int taskId, String runHostType) {
        List<TranscribeReplayHost> hosts = transcribeReplayHostService.getHostsByTaskId(taskId);
        Optional<TranscribeReplayHost> host = hosts.stream()
            .filter(replayHost -> runHostType.equals(replayHost.getDbType()))
            .findFirst();
        if (host.isPresent()) {
            TranscribeReplayHost replayHost = host.get();
            ShellInfoVo shellInfo = new ShellInfoVo(replayHost.getIp(), replayHost.getPort(), replayHost.getUserName(),
                encryptionUtils.decrypt(replayHost.getPasswd()));
            return shellInfo;
        } else {
            throw new OpsException("Host is not exits.");
        }
    }

    private String getTranscribeMode(String config) {
        Properties properties = new Properties();
        try {
            java.io.InputStream inputStream = new java.io.ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
            properties.load(inputStream);
            return properties.getProperty(SQL_TRANSCRIBE_MODE);
        } catch (IOException e) {
            throw new OpsException(e.getMessage());
        }
    }

    private synchronized void refreshSqlNum(TranscribeReplayTask transcribeReplayTask) {
        ShellInfoVo shellInfo = getShellInfo(transcribeReplayTask.getId(), TARGET_DB);
        String filePath = String.format("%s/%s", transcribeReplayTask.getTargetInstallPath(), PROCESS_FILE);
        boolean isFileExists = FileUtils.isRemoteFileExists(filePath, shellInfo);
        if (!isFileExists) {
            return;
        }
        String processStr = FileUtils.catRemoteFileContents(filePath, shellInfo);
        if (processStr.isEmpty()) {
            return;
        }
        Process process = JSON.parseObject(processStr, Process.class);
        transcribeReplayTask.setParseNum(process.getParseCount());
        transcribeReplayTask.setReplayNum(process.getReplayCount());
        updateById(transcribeReplayTask);
    }

    private void saveTranscribeHost(TranscribeReplayTask transcribeReplayTask, String ip, String username) {
        OpsHostEntity hostEntity = hostService.getByPublicIp(ip);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Not find host:[" + ip + "] info,please add host info.");
        }
        OpsHostUserEntity hostUserEntity = hostUserService.getHostUserByUsername(hostEntity.getHostId(), username);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Not find user:[" + username + "] info,please add user info");
        }
        saveTranscribeReplayHost(transcribeReplayTask, hostEntity, hostUserEntity,
            transcribeReplayTask.getSourceDbType());
    }

    private void saveReplayHost(TranscribeReplayTask transcribeReplayTask, String ip, String username) {
        OpsHostEntity hostEntity = hostService.getByPublicIp(ip);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Not find host:[" + ip + "] info,please add host info.");
        }
        OpsHostUserEntity hostUserEntity = hostUserService.getHostUserByUsername(hostEntity.getHostId(), username);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Not find user:[" + username + "] info,please add user info");
        }
        saveTranscribeReplayHost(transcribeReplayTask, hostEntity, hostUserEntity, TARGET_DB);
    }

    private void saveTranscribeReplayHost(TranscribeReplayTask transcribeReplayTask, OpsHostEntity hostEntity,
        OpsHostUserEntity hostUserEntity, String dbType) {
        TranscribeReplayHost transcribeReplayHost = new TranscribeReplayHost();
        transcribeReplayHost.setIp(hostEntity.getPublicIp());
        transcribeReplayHost.setPort(hostEntity.getPort());
        transcribeReplayHost.setUserName(hostUserEntity.getUsername());
        transcribeReplayHost.setPasswd(hostUserEntity.getPassword());
        transcribeReplayHost.setDbType(dbType);
        transcribeReplayHost.setTaskId(transcribeReplayTask.getId());
        transcribeReplayHostService.save(transcribeReplayHost);
    }

    private synchronized void syncRefreshSlowSql(TranscribeReplayTask transcribeReplayTask) {
        Connection conn = JDBCUtils.getConnection(transcribeReplayTask, getNodeInfo(transcribeReplayTask));
        List<TranscribeReplaySlowSql> slowSqls = new ArrayList<>();
        PreparedStatement pre = null;
        ResultSet res = null;
        String sql = "select * from slow_table;";
        try {
            pre = conn.prepareStatement(sql);
            res = pre.executeQuery();
            List<SlowSqlModel> list = JDBCUtils.populate(res, SlowSqlModel.class);
            for (SlowSqlModel slowSql : list) {
                TranscribeReplaySlowSql replaySlowSql = new TranscribeReplaySlowSql();
                replaySlowSql.setTaskId(transcribeReplayTask.getId());
                replaySlowSql.setUniqueCode(slowSql.getUniqueCode());
                replaySlowSql.setSqlStr(slowSql.getSql());
                replaySlowSql.setExplainStr(slowSql.getExplain());
                replaySlowSql.setCountStr(slowSql.getCount());
                replaySlowSql.setSourceDuration(slowSql.getMysqlDuration());
                replaySlowSql.setTargetDuration(slowSql.getOpgsDuration());
                slowSqls.add(replaySlowSql);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            throw new OpsException(e.getMessage());
        } finally {
            if (conn != null && pre != null) {
                try {
                    conn.close();
                    pre.close();
                } catch (SQLException e) {
                    log.error("Failed to close database connection: {}", e.getMessage(), e);
                }
            }
        }
        transcribeReplaySlowSqlService.saveOrUpdateBatch(slowSqls);
    }

    private TranscribeReplayNodeInfo getNodeInfo(TranscribeReplayTask transcribeReplayTask) {
        String nodeId = transcribeReplayTask.getTargetNodeId();
        log.debug("Fetching node info for nodeId: {}", nodeId);
        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = opsJdbcDbClusterNodeService.getById(nodeId);
        if (opsJdbcDbClusterNodeEntity != null) {
            log.debug("Node info found in OpsJdbcDbClusterNodeEntity for nodeId: {}", nodeId);
            return new TranscribeReplayNodeInfo(opsJdbcDbClusterNodeEntity.getIp(),
                opsJdbcDbClusterNodeEntity.getPort(), opsJdbcDbClusterNodeEntity.getUsername(),
                encryptionUtils.decrypt(opsJdbcDbClusterNodeEntity.getPassword()));
        }
        log.debug("Node info not found in OpsJdbcDbClusterNodeEntity, trying OpsClusterVO for nodeId: {}", nodeId);
        OpsClusterVO opsClusterVO = opsFacade.getOpsClusterVOByNodeId(nodeId);
        if (opsClusterVO == null) {
            log.error("OpsClusterVO not found for nodeId: {}", nodeId);
            terminateTaskWithError(transcribeReplayTask, nodeId);
        }
        Optional<OpsClusterNodeVO> nodeOptional = opsClusterVO.getClusterNodes()
            .stream()
            .filter(node -> nodeId.equals(node.getNodeId()))
            .findFirst();
        if (!nodeOptional.isPresent()) {
            terminateTaskWithError(transcribeReplayTask, nodeId);
        }
        OpsClusterNodeVO clusterNodeVO = nodeOptional.get();
        log.debug("Node info found in OpsClusterVO for nodeId: {}", nodeId);
        return new TranscribeReplayNodeInfo(clusterNodeVO.getPublicIp(), clusterNodeVO.getDbPort().toString(),
            clusterNodeVO.getDbUser(), encryptionUtils.decrypt(clusterNodeVO.getDbUserPassword()));
    }

    private void terminateTaskWithError(TranscribeReplayTask task, String nodeId) {
        String errorMsg = "No corresponding instance exists. nodeId:" + nodeId;
        task.setErrorMsg(errorMsg);
        updateStatus(task, TranscribeReplayStatus.RUN_FAIL);
        killTask(task.getId());
        throw new OpsException(errorMsg);
    }

    private void killTask(Integer id) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
        String jarName = String.format(JAR_NAME, transcribeReplayTask.getToolVersion());
        String sourceJarPath = transcribeReplayTask.getSourceInstallPath() + jarName;
        String targetJarPath = transcribeReplayTask.getTargetInstallPath() + jarName;
        ShellInfoVo sourceShellInfo = getShellInfo(id, transcribeReplayTask.getSourceDbType());
        ShellInfoVo targetShellInfo = getShellInfo(id, TARGET_DB);
        String killSourceProcess = String.format("kill -9 $(ps -ef|grep %s|awk '{print $2}')", sourceJarPath);
        String killTargetProcess = String.format("kill -9 $(ps -ef|grep %s|awk '{print $2}')", targetJarPath);
        if (REPLAY.equalsIgnoreCase(transcribeReplayTask.getTaskType())) {
            ShellUtil.execCommand(targetShellInfo, killTargetProcess);
        } else {
            ShellUtil.execCommand(sourceShellInfo, killSourceProcess);
            ShellUtil.execCommand(targetShellInfo, killTargetProcess);
        }
    }

    private void setResult(TranscribeReplayTask transcribeReplayTask) {
        String summaryPath = String.format("%s/summary.log", transcribeReplayTask.getTargetInstallPath());
        ShellInfoVo shellInfo = getShellInfo(transcribeReplayTask.getId(), TARGET_DB);
        boolean isExists = FileUtils.isRemoteFileExists(summaryPath, shellInfo);
        if (!isExists) {
            return;
        }
        String contents = FileUtils.catRemoteFileContents(summaryPath, shellInfo);
        if (contents.isEmpty()) {
            return;
        }
        int total = extractValue(contents, TOTAL_NUM);
        int slowSql = extractValue(contents, SLOW_SQL_NUM);
        int failSql = extractValue(contents, FAIL_SQL_NUM);
        transcribeReplayTask.setTotalNum(total);
        transcribeReplayTask.setSlowSqlCount(slowSql);
        transcribeReplayTask.setFailedSqlCount(failSql);
        updateById(transcribeReplayTask);
    }

    private static int extractValue(String contents, String key) {
        Pattern pattern = Pattern.compile(key);
        Matcher matcher = pattern.matcher(contents);
        String totalNumber = null;
        while (matcher.find()) {
            totalNumber = matcher.group(1);
        }
        if (totalNumber != null) {
            return Integer.valueOf(totalNumber);
        } else {
            return 0;
        }
    }
}
