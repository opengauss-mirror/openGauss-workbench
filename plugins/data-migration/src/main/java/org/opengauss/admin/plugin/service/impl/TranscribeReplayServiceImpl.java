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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListIterator;
import java.util.HashMap;
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
    final String transcribeConfigFile = "transcribe.properties";
    final String parseConfigFile = "parse.properties";
    final String replayConfigFile = "replay.properties";
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
    public void downloadAndConfig(TranscribeReplayTaskDto tp, Integer id, Map<String, Object> config) {
        this.downloadId = id;
        String url = lastedVersionUrl;
        String taskType = tp.getTaskType();
        boolean isTranscribe = taskType.contains("transcribe");
        boolean isReplay = taskType.contains("replay");
        if (!isTranscribe && !isReplay) {
            throw new OpsException("An unknown error occurred while recording playback.");
        }
        threadPoolTaskExecutor.submit(() -> {
            if (isTranscribe) {
                download(tp, createSourceShellInfo(tp), id, url, tp.getSourceInstallPath());
                addSourceConfigList(tp, id, config);
            }
            download(tp, createTargetShellInfo(tp), id, url, tp.getTargetInstallPath());
            addTargetConfigList(tp, id, config);
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

    private void addSourceConfigList(TranscribeReplayTaskDto tp, Integer id, Map<String, Object> config) {
        String transcribeRemotePath = tp.getSourceInstallPath() + File.separator + downloadId + configFilePath
            + transcribeConfigFile;
        String generalReplayRemotePath = tp.getSourceInstallPath() + File.separator + downloadId + configFilePath
            + replayConfigFile;
        String fileExistCommand = String.format("[ -e %s ]", transcribeRemotePath);
        JschResult fileExist = ShellUtil.execCommandGetResult(sourceShellInfo(tp), fileExistCommand);
        TranscribeReplayTask transcribeReplayTask = getById(id);
        if (!fileExist.isOk()) {
            transcribeReplayTask.setErrorMsg("The addSourceConfigList fail!" + fileExist.getResult());
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.DOWNLOAD_FAIL);
            throw new OpsException("The addSourceConfigList fail!" + fileExist.getResult());
        } else {
            ShellUtil.updateFileContent(sourceShellInfo(tp), getTranscribeConfig(tp, config), transcribeRemotePath);
            if (GENERAL.getMode().equals(config.get("sql.transcribe.mode"))) {
                ShellUtil.updateFileContent(sourceShellInfo(tp), getReplayConfig(tp, config), generalReplayRemotePath);
            }
            updateStatus(transcribeReplayTask, TranscribeReplayStatus.NOT_RUN);
            log.info("The addSourceConfigList success!");
        }
    }

    private void addTargetConfigList(TranscribeReplayTaskDto tp, Integer id, Map<String, Object> config) {
        if (TCPDUMP.getMode().equals(config.get("sql.transcribe.mode"))) {
            String parseRemotePath = tp.getTargetInstallPath() + File.separator + downloadId + configFilePath
                + parseConfigFile;
            writeTargetFile(parseRemotePath, tp, id, parseConfigFile, getParseConfig(tp, config));
        }
        if (!GENERAL.getMode().equals(config.get("sql.transcribe.mode"))) {
            String replayRemotePath = tp.getTargetInstallPath() + File.separator + downloadId + configFilePath
                + replayConfigFile;
            writeTargetFile(replayRemotePath, tp, id, replayConfigFile, getReplayConfig(tp, config));
        }
    }

    private void writeTargetFile(String remotePath, TranscribeReplayTaskDto tp, Integer id, String fileName,
                           Map<String, Object> context) {
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

    private Map<String, Object> getTranscribeConfig(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        if (TCPDUMP.getMode().equals(config.get("sql.transcribe.mode"))) {
            return getTcpDumpConfig(tp, config);
        } else if (ATTACH.getMode().equals(config.get("sql.transcribe.mode"))) {
            return getAttachConfig(tp, config);
        } else if (GENERAL.getMode().equals(config.get("sql.transcribe.mode"))) {
            return getGeneralLog(tp, config);
        } else {
            throw new OpsException("sql.transcribe.mode does not match.");
        }
    }

    private Map<String, Object> getTcpDumpConfig(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("sql.transcribe.mode", config.get("sql.transcribe.mode"));
        result.put("tcpdump.network.interface", config.get("tcpdump.network.interface"));
        result.put("tcpdump.capture.duration", config.get("tcpdump.capture.duration"));
        result.put("tcpdump.file.name", config.get("tcpdump.file.name"));
        result.put("tcpdump.file.size", config.get("tcpdump.file.size"));
        getTcpdumpAndAttachSameConfig(config, result, tp);
        JschResult jschResult = ShellUtil.execCommandGetResult(sourceShellInfo(tp), "which tcpdump");
        String tcpdumpPath;
        OpsHostEntity sourceOpsHostEntity = hostService.getByPublicIp(tp.getSourceIp());
        if (jschResult.getExitCode() == 0) {
            tcpdumpPath = jschResult.getResult();
            int lastIndex = tcpdumpPath.lastIndexOf('/');
            tcpdumpPath = tcpdumpPath.substring(0, lastIndex).replaceAll(System.lineSeparator(), "");
        } else {
            tcpdumpPath = tp.getSourceInstallPath() + File.separator + downloadId + "/transcribe-replay-tool/plugin/"
                + sourceOpsHostEntity.getCpuArch();
        }
        result.put("tcpdump.plugin.path", tcpdumpPath);
        result.put("tcpdump.database.port", tp.getSourcePort());
        result.put("tcpdump.file.path", tp.getSourceInstallPath() + File.separator + downloadId);
        return result;
    }

    private Map<String, Object> getAttachConfig(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("sql.transcribe.mode", config.get("sql.transcribe.mode"));
        result.put("attach.process.pid", config.get("attach.process.pid"));
        result.put("attach.target.schema", config.get("attach.target.schema"));
        result.put("attach.capture.duration", config.get("attach.capture.duration"));
        result.put("file.count.limit", config.get("file.count.limit"));
        result.put("sql.file.name", config.get("sql.file.name"));
        result.put("sql.file.size", config.get("sql.file.size"));
        getTcpdumpAndAttachSameConfig(config, result, tp);
        result.put("attach.plugin.path", tp.getSourceInstallPath() + File.separator + downloadId
            + "/transcribe-replay-tool/plugin");
        result.put("sql.file.path", tp.getSourceInstallPath() + File.separator + downloadId);
        return result;
    }

    private void getTcpdumpAndAttachSameConfig(Map<String, Object> config, Map<String, Object> result,
                                               TranscribeReplayTaskDto tp) {
        OpsHostEntity targetOpsHostEntity = hostService.getByPublicIp(tp.getTargetIp());
        OpsHostUserEntity targetOpsHostUserEntity = hostUserService.getHostUserByUsername(
            targetOpsHostEntity.getHostId(), tp.getTargetUser());
        result.put("should.check.system", config.get("should.check.system"));
        result.put("max.cpu.threshold", config.get("max.cpu.threshold"));
        result.put("max.memory.threshold", config.get("max.memory.threshold"));
        result.put("max.disk.threshold", config.get("max.disk.threshold"));
        result.put("should.send.file", config.get("should.send.file"));
        result.put("remote.retry.count", config.get("remote.retry.count"));
        result.put("remote.receiver.password", encryptionUtils.decrypt(targetOpsHostUserEntity.getPassword()));
        result.put("remote.file.path", tp.getTargetInstallPath() + File.separator + downloadId);
        result.put("remote.receiver.name", tp.getTargetUser());
        result.put("remote.node.ip", tp.getTargetIp());
        result.put("remote.node.port", targetOpsHostEntity.getPort());
    }

    private Map<String, Object> getGeneralLog(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("sql.transcribe.mode", config.get("sql.transcribe.mode"));
        result.put("general.database.username", config.get("general.database.username"));
        Object passwordObj = config.get("general.database.password");
        result.put("general.database.password", encryptionUtils.decrypt(passwordObj instanceof String
            ? (String) passwordObj : ""));
        result.put("general.sql.batch", config.get("general.sql.batch"));
        result.put("general.start.time", config.get("general.start.time"));
        result.put("sql.storage.mode", config.get("sql.storage.mode"));
        if (TranscribeReplaySqlStorageMode.DB.getMode().equals(config.get("sql.storage.mode"))) {
            result.put("sql.table.drop", config.get("sql.table.drop"));
            getDbStorageConfig(config, result);
        } else {
            result.put("sql.file.path", tp.getSourceInstallPath() + File.separator + downloadId);
        }
        result.put("general.database.ip", tp.getSourceIp());
        result.put("general.database.port", tp.getSourcePort());
        return result;
    }

    private void getDbStorageConfig(Map<String, Object> config, Map<String, Object> result) {
        result.put("sql.database.ip", config.get("sql.database.ip"));
        result.put("sql.database.port", config.get("sql.database.port"));
        result.put("sql.database.username", config.get("sql.database.username"));
        result.put("sql.database.name", config.get("sql.database.name"));
        Object passwordObj = config.get("sql.database.password");
        result.put("sql.database.password", encryptionUtils.decrypt(passwordObj instanceof String
            ? (String) passwordObj : ""));
        result.put("sql.table.name", config.get("sql.table.name"));
    }

    private String getTaskPathById(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        String taskPath;
        if (config.get("tcpdump.file.id") != null && !"".equals(config.get("tcpdump.file.id"))) {
            Object passwordObj = config.get("tcpdump.file.id");
            TranscribeReplayTask byId = getById(Integer.valueOf(passwordObj instanceof String
                ? (String) passwordObj : ""));
            int lastIndex = byId.getTargetInstallPath().lastIndexOf('/');
            taskPath = byId.getTargetInstallPath().substring(0, lastIndex);
        } else {
            taskPath = tp.getTargetInstallPath() + File.separator + downloadId;
        }
        return taskPath;
    }

    private Map<String, Object> getParseConfig(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("parse.select.result", config.get("parse.select.result"));
        result.put("select.result.path", tp.getTargetInstallPath() + File.separator + downloadId);
        result.put("result.file.name", config.get("result.file.name"));
        result.put("result.file.size", config.get("result.file.size"));
        result.put("sql.storage.mode", config.get("sql.storage.mode"));
        result.put("tcpdump.database.type", tp.getSourceDbType().toLowerCase());
        result.put("queue.size.limit", config.get("queue.size.limit"));
        result.put("packet.batch.size", config.get("packet.batch.size"));
        result.put("tcpdump.file.path", getTaskPathById(tp, config));
        result.put("tcpdump.database.ip", tp.getSourceIp());
        result.put("tcpdump.database.port", tp.getSourcePort());
        if (TranscribeReplaySqlStorageMode.JSON.getMode().equals(config.get("sql.storage.mode"))) {
            result.put("sql.file.path", tp.getTargetInstallPath() + File.separator + downloadId);
            result.put("sql.file.size", config.get("sql.file.size"));
            result.put("sql.file.name", config.get("sql.file.name"));
        } else if (TranscribeReplaySqlStorageMode.DB.getMode().equals(config.get("sql.storage.mode"))) {
            result.put("sql.table.drop", config.get("sql.table.drop"));
            getDbStorageConfig(config, result);
        } else {
            throw new OpsException("Parse sql.storage.mode does not match.");
        }
        return result;
    }

    private Map<String, Object> getReplayConfig(TranscribeReplayTaskDto tp, Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();
        result.put("sql.storage.mode", config.get("sql.storage.mode"));
        result.put("sql.replay.multiple", config.get("sql.replay.multiple"));
        result.put("sql.replay.only.query", config.get("sql.replay.only.query"));
        if (TCPDUMP.getMode().equals(config.get("sql.transcribe.mode"))) {
            result.put("sql.replay.strategy", config.get("sql.replay.strategy"));
            result.put("sql.replay.parallel.max.pool.size", config.get("sql.replay.parallel.max.pool.size"));
        } else {
            result.put("sql.replay.strategy", "serial");
            result.put("sql.replay.parallel.max.pool.size", 1);
        }
        result.put("replay.max.time", config.get("replay.max.time"));
        result.put("sql.replay.slow.sql.rule", config.get("sql.replay.slow.sql.rule"));
        result.put("sql.replay.slow.time.difference.threshold",
            config.get("sql.replay.slow.time.difference.threshold"));
        result.put("sql.replay.slow.sql.duration.threshold", config.get("sql.replay.slow.sql.duration.threshold"));
        result.put("sql.replay.slow.top.number", config.get("sql.replay.slow.top.number"));
        result.put("sql.replay.draw.interval", config.get("sql.replay.draw.interval"));
        result.put("sql.replay.session.white.list", config.get("sql.replay.session.white.list"));
        result.put("sql.replay.session.black.list", config.get("sql.replay.session.black.list"));
        result.put("sql.replay.database.username", config.get("sql.replay.database.username"));
        Object passwordObj = config.get("sql.replay.database.password");
        result.put("sql.replay.database.password",
                encryptionUtils.decrypt(passwordObj instanceof String ? (String) passwordObj : ""));
        if (GENERAL.getMode().equals(config.get("sql.transcribe.mode"))) {
            result.put("sql.replay.slow.sql.csv.dir", tp.getSourceInstallPath() + File.separator + downloadId);
        } else {
            result.put("sql.replay.slow.sql.csv.dir", tp.getTargetInstallPath() + File.separator + downloadId);
        }
        result.put("sql.replay.database.ip", tp.getTargetIp());
        result.put("sql.replay.database.port", tp.getTargetPort());
        result.put("sql.replay.database.schema.map", addDbMapSchema(tp.getDbMap()));
        if (TranscribeReplaySqlStorageMode.DB.getMode().equals(config.get("sql.storage.mode"))) {
            getDbStorageConfig(config, result);
        } else {
            if (GENERAL.getMode().equals(config.get("sql.transcribe.mode"))) {
                result.put("sql.file.path", tp.getSourceInstallPath() + File.separator + downloadId);
            } else {
                result.put("sql.file.path", getTaskPathById(tp, config));
            }
            result.put("sql.file.name", config.get("sql.file.name"));
        }
        return result;
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
