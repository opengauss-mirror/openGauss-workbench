/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.github.benmanes.caffeine.cache.Cache;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.Assessment;
import org.opengauss.collect.domain.CollectPeriod;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.collect.domain.builder.AssessmentBuilder;
import org.opengauss.collect.enums.AssessEnum;
import org.opengauss.collect.manager.MonitorManager;
import org.opengauss.collect.mapper.AssessmentMapper;
import org.opengauss.collect.mapper.CollectPeriodMapper;
import org.opengauss.collect.proloading.SchedulerService;
import org.opengauss.collect.quartz.HeartbeatJob;
import org.opengauss.collect.quartz.ModifyStateJob;
import org.opengauss.collect.quartz.PileInsertionJob;
import org.opengauss.collect.service.SqlOperation;
import org.opengauss.collect.utils.AssertUtil;
import org.opengauss.collect.utils.CommandLineRunner;
import org.opengauss.collect.utils.ConnectionUtils;
import org.opengauss.collect.utils.DateUtil;
import org.opengauss.collect.utils.FileCopy;
import org.opengauss.collect.utils.IdUtils;
import org.opengauss.collect.utils.JschUtil;
import org.opengauss.collect.utils.StringUtil;
import org.opengauss.collect.utils.response.RespBean;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * SqlOperationImpl
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
@Service
public class SqlOperationImpl implements SqlOperation {
    @Autowired
    private Cache<String, Assessment> assessmentCache;

    @Autowired
    private CollectPeriodMapper periodMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostService hostService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IHostUserService hostUserService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;

    @Autowired
    private AssessmentMapper assessmentMapper;

    @Autowired
    private SchedulerService schedulerService;

    @Override
    public RespBean getAllPids(String host, String hostUser) {
        Optional<LinuxConfig> config = getLinuxConfig(host, hostUser);
        AssertUtil.isTrue(!config.isPresent(), "Host does not exist");
        Session session = JschUtil.obtainSession(config.get());
        String mes = JschUtil.executeCommand(session, Constant.FIND_USER_ALL_JAVA);
        List<String> list = mes.lines().collect(Collectors.toList());
        AssertUtil.isTrue(CollectionUtil.isEmpty(list), "The server has no Java process");
        return RespBean.success("success", list);
    }

    @Override
    public RespBean getUploadPath(Integer userId) {
        return RespBean.success("success", getSystemPath(userId));
    }

    @Override
    public void downloadChrome(String host, String hostUser, String filePath, HttpServletResponse response) {
        OpsAssert.isTrue(StrUtil.isNotEmpty(host), "Host cannot be empty");
        OpsAssert.isTrue(StrUtil.isNotEmpty(hostUser), "HostUser cannot be empty");
        Optional<LinuxConfig> config = getLinuxConfig(host, hostUser);
        AssertUtil.isTrue(!config.isPresent(), "Host does not exist");
        Session session = JschUtil.obtainSession(config.get());
        OpsAssert.nonNull(session, "Host Session create failed");
        // 获得文件路径
        List<String> fileNames = JschUtil.getFileNamesByPath(session, filePath, true);
        List<File> files = new ArrayList<>();
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + "sql_stack.zip");
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (String name : fileNames) {
                Future<File> future = MonitorManager.mine().workExecute(new Callable<File>() {
                    @SneakyThrows
                    @Override
                    public File call() {
                        try (OutputStream out = new FileOutputStream(name)) {
                            JschUtil.downLoad(session, filePath + name, out);
                        }
                        return new File(name);
                    }
                });
                files.add(future.get());
            }
            addToZipFile(files, zipOut);
        } catch (IOException | ExecutionException | InterruptedException exception) {
            log.error(exception.getMessage());
        }
        deleteFile(fileNames);
        // close session
        JschUtil.closeSession(session);
    }

    private void addToZipFile(List<File> files, ZipOutputStream zipOut) throws IOException {
        for (File file : files) {
            FileInputStream fileInputStream = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[2048];
            int length;
            while ((length = fileInputStream.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fileInputStream.close();
        }
    }

    private void deleteFile(List<String> fileNames) {
        fileNames.forEach(item -> {
            File file = new File(item);
            if (file.exists() && file.delete()) {
                log.info("Deleted file: " + item);
            }
        });
    }

    /**
     * getLinuxConfig
     *
     * @param host     host
     * @param hostUser hostUser
     * @return Optional<LinuxConfig>  Optional
     */
    public Optional<LinuxConfig> getLinuxConfig(String host, String hostUser) {
        List<OpsHostEntity> entities = hostService.list();
        List<OpsHostUserEntity> userEntities = hostUserService.list();
        Map<String, OpsHostUserEntity> userMap = userEntities.stream()
                .filter(item -> item.getUsername().equals(hostUser))
                .collect(Collectors.toMap(OpsHostUserEntity::getHostId, Function.identity()));
        return entities.stream().filter(entity -> entity.getPublicIp().equals(host)).findFirst().flatMap(entity -> {
            LinuxConfig config = new LinuxConfig();
            config.setHost(host);
            config.setPort(entity.getPort());
            OpsHostUserEntity userEntity = userMap.get(entity.getHostId());
            if (userEntity != null) {
                config.setUserName(userEntity.getUsername());
                config.setPassword(encryptionUtils.decrypt(userEntity.getPassword()));
            }
            return Optional.of(config);
        });
    }

    @Override
    public RespBean startAssessmentSql(Assessment assessment, String sqlInputType, Integer userId) {
        checkAssessment(assessment, sqlInputType);
        // 创建一个执行环境 data/gs_assessment/fileName  目前是这个
        String envPath = Constant.ENV_PATH;
        // 下载评估文件
        String version = getVersion();
        String downCommand = String.format(Constant.DOWN_LOAD_PATH, version, Constant.ASSESS_PATH,
                version);
        boolean isSucc = CommandLineRunner.runCommand(downCommand, envPath, Constant.DOWN_TIME_OUT);
        log.info("Download evaluation files---->{}", isSucc);
        // 推送评估文件到envPath
        if (!isSucc) {
            log.info("Download evaluation file failed, start copying local resources");
            FileCopy.copyFilesToDirectory(Constant.FILE_NAMES, envPath);
        }
        // 获得系统上传文件路径
        String dataKitPath = getSystemPath(userId);
        AssertUtil.isTrue(StrUtil.isEmpty(dataKitPath), "Failed to obtain system path");
        AssessEnum.valueOf(Constant.ASSESS_MAP.get(sqlInputType)).startAssessment(assessment, userId, dataKitPath);
        // 判断sql输入类型. file类型，文件上传,解压,上传文件到系统设置的路径加上/assess, 采集和文件上传都放到系统路劲下加/assess目录下
        // proccessId下载文件传到datakit目录  当前是uploadPath = /ops/files/assess/
        // 用户上传的文件，上传到datakit目录
        if (sqlInputType.equals(Constant.ASSESS_FILE) || sqlInputType.equals(Constant.ASSESS_PID)) {
            assessment.setFiledir(dataKitPath);
        }
        // 写assessment.properties path = assess/assessment.properties
        writeAssess(assessment, envPath + Constant.ASSESS_PATH + Constant.ASSESS_PROPERTIES);
        String command = getAssessCommand(sqlInputType);
        // 执行command  在真实路径下执行
        String actPath = envPath + Constant.ASSESS_PATH;
        boolean isSuccess = CommandLineRunner.runCommand(command, actPath, Constant.TIME_OUT);
        AssertUtil.isTrue(!isSuccess, "Evaluation failed");
        long id = IdUtils.SNOWFLAKE.nextId();
        String reportFileName = id + "_" + Constant.ASSESS_REPORT;
        // 保存评估信息到数据库,复制此时的report.html文件到 目录库便于下载
        FileCopy.copyFile(Constant.ASSESS_DOWNLOAD, Constant.TEMPORARY_SAVE + reportFileName);
        assessment.setAssessmentId(id);
        assessment.setStartTime(DateUtil.getTimeNow());
        assessment.setReportFileName(reportFileName);
        AssertUtil.save(1, assessmentMapper.insert(assessment), "Evaluation record save failed");
        return RespBean.success("success");
    }

    private String getSystemPath(Integer userId) {
        return Optional.ofNullable(sysSettingFacade.getSysSetting(userId))
                .map(sysSetting -> sysSetting.getUploadPath() + Constant.ACT_PATH)
                .orElse("");
    }

    /**
     * getVersion
     *
     * @return String
     */
    public String getVersion() {
        Properties properties = new Properties();
        String version = null;
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
            if (properties != null) {
                version = properties.getProperty("app.version");
            }
        } catch (IOException exception) {
            throw new ServiceException("Failed to retrieve version number");
        }
        if (version == null || version.isEmpty()) {
            throw new ServiceException("Version number not found in the properties file.");
        }
        return version;
    }

    /**
     * runCommand
     *
     * @param session session
     * @param command command
     * @return String
     */
    public Future<String> runCommand(Session session, String command) {
        String message = JschUtil.executeCommand(session, command);
        JschUtil.closeSession(session);
        log.info("Execution Command Result {}", message);
        return new AsyncResult<>(message);
    }

    private void checkAssessment(Assessment assessment, String sqlInputType) {
        // 校验opengauss数据库
        String gaussUrl = "jdbc:opengauss://" + assessment.getOpengaussHost() + ":" + assessment.getOpengaussPort()
                + "/" + assessment.getOpengaussDbname() + "?batchMode=off";
        ConnectionUtils.getConnection(DbTypeEnum.OPENGAUSS.getDriverClass(), gaussUrl, assessment.getOpengaussUser(),
                assessment.getOpengaussPassword());
        if (sqlInputType.equals(Constant.ASSESS_COLLECT)) {
            // 校验mysql数据库
            String mysqlUrl = "jdbc:mysql://" + assessment.getMysqlHost() + ":" + assessment.getMysqlPort() + "/"
                    + assessment.getMysqlDbname() + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/"
                    + "Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
            ConnectionUtils.getConnection(DbTypeEnum.MYSQL.getDriverClass(), mysqlUrl, assessment.getMysqlUser(),
                    assessment.getMysqlPassword());
        }
    }

    private String getAssessCommand(String sqlInputType) {
        String suffix = " && echo execution succeeded || echo false";
        String command = "";
        if (sqlInputType.equals(Constant.ASSESS_FILE) || sqlInputType.equals(Constant.ASSESS_PID)) {
            command = "sh start.sh -d file -c assessment.properties -o report.html" + suffix;
        }
        if (sqlInputType.equals(Constant.ASSESS_COLLECT)) {
            command = "sh start.sh -d collect -c assessment.properties -o report.html" + suffix;
        }
        return command;
    }

    private void writeAssess(Assessment assessment, String filePath) {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            AssessmentBuilder builder = new AssessmentBuilder();
            builder.appendProperty("assessmenttype", assessment.getAssessmenttype())
                    .appendProperty("filedir", assessment.getFiledir())
                    .appendProperty("sqltype", assessment.getSqltype())
                    .appendSectionBreak()
                    .appendProperty("mysql.password", assessment.getMysqlPassword())
                    .appendProperty("mysql.user", assessment.getMysqlUser())
                    .appendProperty("mysql.port", assessment.getMysqlPort())
                    .appendProperty("mysql.host", assessment.getMysqlHost())
                    .appendProperty("mysql.dbname", assessment.getMysqlDbname())
                    .appendSectionBreak()
                    .appendProperty("opengauss.user", assessment.getOpengaussUser())
                    .appendProperty("opengauss.password", assessment.getOpengaussPassword())
                    .appendProperty("opengauss.port", assessment.getOpengaussPort())
                    .appendProperty("opengauss.host", assessment.getOpengaussHost())
                    .appendProperty("opengauss.dbname", assessment.getOpengaussDbname());
            writer.write(builder.build());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void downloadAssessmentSql(String reportFileName, HttpServletResponse response) {
        try {
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + "report.html");
            File file = new File(Constant.TEMPORARY_SAVE + reportFileName);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.flush();
            out.close();
        } catch (IOException exception) {
            throw new ServiceException("The download failed because of an abnormal IO stream.");
        }
    }

    @Override
    public RespBean sqlAssessInit() {
        Optional<Assessment> optionalAssessment = Optional.ofNullable(
                assessmentCache.getIfPresent(Constant.ASSESS_PATH));
        optionalAssessment.ifPresent(assessment -> {
            assessment.setMysqlPassword("");
            assessment.setOpengaussPassword("");
            assessment.setFiledir("");
            assessment.setSqltype("");
        });
        return optionalAssessment.map(assessment -> RespBean.success("success", assessment))
                .orElse(RespBean.success("success"));
    }

    @Override
    public RespBean startCollectingSql(CollectPeriod task) {
        CollectPeriod period = periodMapper.selectById(task.getTaskId());
        // Stop the task if it is already runnin
        if (!task.isSwitchStatus()) {
            stopCollectingSql(task);
            return RespBean.success("Stop success");
        }
        // Ensure the task is completed before starting it
        AssertUtil.isTrue(Constant.TASK_COMPLETED.equals(period.getCurrentStatus()),
                "Task is completed, cannot start");
        String host = task.getHost();
        LinuxConfig config = getLinuxConfig(host,
                task.getHostUser()).orElseThrow(() -> new ServiceException("Host not found"));
        try {
            createAndStartScheduler(task, config);
        } catch (SchedulerException e) {
            log.error("Error creating or starting scheduler: {}", e.getMessage());
            return RespBean.error("Error creating or starting scheduler");
        }
        task.setSwitchStatus(true);
        task.setCurrentStatus(Constant.TASK_RUN);
        updateCurrentStatus(task);
        return RespBean.success("Start collecting SQL");
    }

    private void stopCollectingSql(CollectPeriod task) {
        Map<String, String> timeMap = StringUtil.handleString(task.getTimeInterval());
        timeMap.forEach((key, value) -> {
            removeSchedulerJobs(key);
        });
        task.setCurrentStatus(Constant.TASK_STOP);
        task.setSwitchStatus(false);
        updateCurrentStatus(task);
    }

    private void removeSchedulerJobs(String key) {
        schedulerService.removeJob(key);
        schedulerService.removeJob(key + "Heartbeat");
        schedulerService.removeJob(key + "status");
    }

    private void createAndStartScheduler(CollectPeriod task, LinuxConfig config) throws SchedulerException {
        Map<String, String> timeMap = StringUtil.handleString(task.getTimeInterval());
        AssertUtil.isTrue(checkStartTime(timeMap), "Start time cannot be earlier than current server time");
        for (Map.Entry<String, String> entry : timeMap.entrySet()) {
            String startTime = entry.getKey();
            String endTime = entry.getValue();
            String executionTime = DateUtil.calculateMinutes(startTime, endTime);
            Map<String, Object> jobDataMap = new HashMap<>();
            jobDataMap.put(Constant.EXECUTE_TIME, executionTime);
            jobDataMap.put(Constant.TASK, task);
            jobDataMap.put(Constant.CONFIG, config);
            scheduleJob(startTime, jobDataMap, executionTime, startTime, endTime);
        }
    }

    private void scheduleJob(String startTime, Map<String, Object> jobDataMap, String executionTime,
                             String start, String end) {
        // Schedule main job
        JobDetail jobDetail = schedulerService.getJobDetail(startTime, jobDataMap, PileInsertionJob.class);
        Trigger trigger = schedulerService.getTrigger(startTime, start, "", 0);
        schedulerService.checkDetail(jobDetail);
        schedulerService.startJob(jobDetail, trigger);
        // Schedule heartbeat job
        JobDetail heartbeatJob = schedulerService.getJobDetail(startTime + "Heartbeat",
                jobDataMap, HeartbeatJob.class);
        Trigger heartbeatTrigger = schedulerService.getTrigger(startTime + "Heartbeat", start, end,
                DateUtil.getInterval(executionTime));
        schedulerService.checkDetail(heartbeatJob);
        schedulerService.startJob(heartbeatJob, heartbeatTrigger);
        // Schedule status job
        JobDetail statusJob = schedulerService.getJobDetail(startTime + "status", jobDataMap,
                ModifyStateJob.class);
        Trigger statusTrigger = schedulerService.getTrigger(startTime + "status", end, "", 0);
        schedulerService.checkDetail(statusJob);
        schedulerService.startJob(statusJob, statusTrigger);
    }

    private boolean checkStartTime(Map<String, String> timeMap) {
        // 创建DateTimeFormatter用于解析时间字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.TIME_FORMAT);
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 检查timeMap中是否有值小于当前时间的情况
        return timeMap.keySet().stream().anyMatch(key -> {
            LocalDateTime time = LocalDateTime.parse(key, formatter);
            return time.isBefore(now);
        });
    }

    /**
     * updateCurrentStatus
     *
     * @param period period
     */
    public void updateCurrentStatus(CollectPeriod period) {
        periodMapper.updateById(period);
    }
}
