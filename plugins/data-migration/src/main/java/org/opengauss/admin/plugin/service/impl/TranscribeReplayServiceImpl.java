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
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.constants.TranscribeReplayConstants;
import org.opengauss.admin.plugin.domain.FailSqlModel;
import org.opengauss.admin.plugin.domain.Process;
import org.opengauss.admin.plugin.domain.SlowSqlModel;
import org.opengauss.admin.plugin.domain.SqlDuration;
import org.opengauss.admin.plugin.domain.TranscribeReplayHost;
import org.opengauss.admin.plugin.domain.TranscribeReplaySlowSql;
import org.opengauss.admin.plugin.domain.TranscribeReplayTask;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskDto;
import org.opengauss.admin.plugin.dto.TranscribeReplayTaskQueryDto;
import org.opengauss.admin.plugin.enums.TranscribeReplayStatus;
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
        String jarName = String.format(JAR_NAME, transcribeReplayTask.getToolVersion());
        String sourceJarPath = transcribeReplayTask.getSourceInstallPath() + jarName;
        String targetJarPath = transcribeReplayTask.getTargetInstallPath() + jarName;
        ShellInfoVo shellInfo1 = getShellInfo(id, transcribeReplayTask.getSourceDbType());
        ShellInfoVo shellInfo2 = getShellInfo(id, TARGET_DB);
        String killSourceProcess = String.format("kill -9 $(ps -ef|grep %s|awk '{print $2}')", sourceJarPath);
        String killTargetProcess = String.format("kill -9 $(ps -ef|grep %s|awk '{print $2}')", targetJarPath);
        if (REPLAY.equalsIgnoreCase(transcribeReplayTask.getTaskType())) {
            ShellUtil.execCommand(shellInfo2, killTargetProcess);
        } else {
            ShellUtil.execCommand(shellInfo1, killSourceProcess);
            ShellUtil.execCommand(shellInfo2, killTargetProcess);
        }
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
            log.error("Failed to tun replay, error is {}", result.getResult());
            throw new OpsException(result.getResult());
        }
        setResult(id);
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
        String nodeId = transcribeReplayTask.getTargetNodeId();
        OpsJdbcDbClusterNodeEntity opsJdbcDbClusterNodeEntity = opsJdbcDbClusterNodeService.getById(nodeId);
        opsJdbcDbClusterNodeEntity.setPassword(encryptionUtils.decrypt(opsJdbcDbClusterNodeEntity.getPassword()));
        Connection conn = JDBCUtils.getConnection(transcribeReplayTask, opsJdbcDbClusterNodeEntity);
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

    private void setResult(Integer id) {
        TranscribeReplayTask transcribeReplayTask = getById(id);
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
