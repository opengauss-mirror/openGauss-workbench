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

package org.opengauss.tun.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jcraft.jsch.Session;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.tun.cache.CacheFactory;
import org.opengauss.tun.common.FixedTuning;
import org.opengauss.tun.config.LinuxConfig;
import org.opengauss.tun.domain.ParameterRecommendation;
import org.opengauss.tun.domain.ParameterShow;
import org.opengauss.tun.domain.SysConfig;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.domain.TrainingProgress;
import org.opengauss.tun.domain.TuningLog;
import org.opengauss.tun.domain.TuningLogDetails;
import org.opengauss.tun.domain.dto.TuningDto;
import org.opengauss.tun.domain.vo.ApplyVo;
import org.opengauss.tun.domain.vo.LogVo;
import org.opengauss.tun.domain.vo.TaskLogVo;
import org.opengauss.tun.enums.help.TuningHelper;
import org.opengauss.tun.enums.impl.LogPathEnum;
import org.opengauss.tun.enums.impl.ParamEnum;
import org.opengauss.tun.mapper.ParameterRecommendationMapper;
import org.opengauss.tun.mapper.TrainingConfigMpper;
import org.opengauss.tun.mapper.TrainingProgressMapper;
import org.opengauss.tun.mapper.TuningLogMapper;
import org.opengauss.tun.quartz.ExecuteJob;
import org.opengauss.tun.quartz.ProgressJob;
import org.opengauss.tun.service.TuningService;
import org.opengauss.tun.utils.AssertUtil;
import org.opengauss.tun.utils.CommandLineRunner;
import org.opengauss.tun.utils.ConnectionUtils;
import org.opengauss.tun.utils.DateUtil;
import org.opengauss.tun.utils.IdUtils;
import org.opengauss.tun.utils.JschUtil;
import org.opengauss.tun.utils.SchedulerUtils;
import org.opengauss.tun.utils.file.FileLoader;
import org.opengauss.tun.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * TuningServiceImpl
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
@Service
public class TuningServiceImpl implements TuningService {
    @Autowired
    private TrainingConfigMpper configMpper;

    @Autowired
    private TuningLogMapper tuningLogMapper;

    @Autowired
    private ParameterRecommendationMapper recommendationMapper;

    @Autowired
    private TrainingProgressMapper progressMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsJdbcDbClusterNodeService nodeService;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

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
    private SchedulerUtils schedulerUtils;

    @Value("${processCron.autoAdd}")
    private String processCron;

    @Value("${fixedTuning.sysbench-create}")
    private String sysbenchCommand;

    @Value("${fixedTuning.sysbench_add_permissions}")
    private String sysbenchAddPermissions;

    @Override
    public RespBean startTuning(TrainingConfig config) {
        String executePath = String.format(FixedTuning.EXECUTE_FILE_PATH, config.getClusterName());
        String workPath = executePath + FixedTuning.MAIN_PATH;
        // Obtain node information
        OpsJdbcDbClusterNodeEntity node = getNode(config.getClusterName());
        // Verify
        validate(config, FixedTuning.SYSBENCH_FILE_PATH);
        // Parsing custom payloads
        AssertUtil.save(1, configMpper.insert(getConfig(config, node)), "Training task save failed");
        TuningDto dto = new TuningDto(config, executePath, workPath, node, sysbenchCommand, sysbenchAddPermissions);
        Map<String, Object> param = Map.of(FixedTuning.TUN_DTO, dto);
        schedulerUtils.createScheduleJob(ExecuteJob.class, config.getTrainingId() + FixedTuning.EXECUTE_JOB,
                FixedTuning.JOB_DEFAULT, "", param);
        Map<String, Object> id = Map.of(FixedTuning.TRAININGID, config.getTrainingId());
        schedulerUtils.createScheduleJob(ProgressJob.class, config.getTrainingId() + FixedTuning.PROCESS_JOB,
                FixedTuning.JOB_DEFAULT, processCron, id);
        CacheFactory.getLogCache().put(config.getTrainingId(), FixedTuning.INIT_VALUE);
        return RespBean.success("The tuning task is currently in progress. Please go to the task list to view it");
    }

    private void validate(TrainingConfig config, String workPath) {
        log.info("Start verifying training tasks");
        checkTuning(config, workPath);
    }

    private List<String> parsingCustomPayloads(TrainingConfig config, String recordPath) {
        TuningHelper.record("Start parsing custom payloads uploaded by users", recordPath);
        List<String> fileNames = new ArrayList<>();
        List<String> cache = FileLoader.getAllFilenames(FixedTuning.UPLOAD_FILE_PATH);
        if (!config.getBenchmark().equals(FixedTuning.DWG)) {
            return new ArrayList<>();
        }
        MultipartFile[] files = config.getCustomLoad();
        if (files != null && files.length > 0) {
            TuningHelper.record("Start processing custom payloads uploaded by parsing users", recordPath);
            String targetDirectory = FixedTuning.UPLOAD_FILE_PATH;
            try {
                // Check if the directory does not exist and create it if necessary
                Files.createDirectories(Paths.get(targetDirectory));
                for (MultipartFile file : files) {
                    String originalFilename = file.getOriginalFilename();
                    fileNames.add(originalFilename);
                    if (!cache.contains(originalFilename)) {
                        byte[] fileBytes = file.getBytes();
                        Path filePath = Paths.get(targetDirectory, originalFilename);
                        Files.write(filePath, fileBytes);
                        TuningHelper.record("Save files to data/running/files/", recordPath);
                    }
                }
            } catch (IOException e) {
                log.error("Error while processing custom payloads: {}", e.getMessage());
            }
        }

        return fileNames;
    }

    private void checkTuning(TrainingConfig config, String workPath) {
        // check Python version
        AssertUtil.isTrue(!CommandLineRunner.runCommand(FixedTuning.CHECK_PYTHON_VERSION,
                workPath, "", 1), "Python 3 version must be greater than or equal to 3.7.0");
        // If it is sysbench, check if sysbench is installed
        if (FixedTuning.SYSBENCH.equals(config.getBenchmark())) {
            AssertUtil.isTrue(!CommandLineRunner.runCommand(FixedTuning.CHECK_SYSBENCH,
                    workPath, "", 1), "The current pressure testing mode is sysbench. "
                    + "Please install sysbench");
        }
        // Check dependencies postgresql-devel
        String rely = CommandLineRunner.runCommand(FixedTuning.CHECK_RELY,
                workPath, 1).trim();
        AssertUtil.isTrue(StrUtil.isNotEmpty(rely), rely);
        List<TrainingConfig> configs = configMpper.selectList(new QueryWrapper<TrainingConfig>()
                .eq("cluster_name", config.getClusterName()));
        TrainingConfig config1 = configs.stream().filter(item -> item.getOnline().equals(FixedTuning.FALSE))
                .findFirst().orElse(null);
        if (ObjectUtil.isEmpty(config1) && config.getOnline().equals(FixedTuning.TRUE)) {
            throw new ServiceException(config.getClusterName()
                    + "The cluster has not undergone offline training.Please conduct offline training first");
        }
        TrainingConfig status = configs.stream().filter(item -> item.getStatus().equals(FixedTuning.RUNING))
                .findFirst().orElse(null);
        AssertUtil.isTrue(ObjectUtil.isNotEmpty(status), "This cluster has running training tasks, "
                + "please try again later");
    }

    private TrainingConfig getConfig(TrainingConfig config, OpsJdbcDbClusterNodeEntity node) {
        String id = IdUtils.getId();
        config.setTrainingId(id);
        // Set the connection information for the database
        config.setHost(node.getIp());
        config.setPort(node.getPort());
        config.setUser(node.getUsername());
        config.setPassword(node.getPassword());
        // Set server root connection information
        Optional<LinuxConfig> rootOpt = getLinuxConfig(node.getIp(), "root");
        AssertUtil.isTrue(!rootOpt.isPresent(), "Failed to obtain root server information");
        config.setRootPassword(rootOpt.get().getPassword());
        // Set the user connection information for installing the database
        Optional<LinuxConfig> dbOpt = getLinuxConfig(node.getIp(), config.getOsUser());
        AssertUtil.isTrue(!dbOpt.isPresent(), "Failed to obtain opengauss database server information");
        config.setOmmPassword(dbOpt.get().getPassword());
        config.setStartTime(DateUtil.getTimeNow());
        config.setLogPath(getTuningLogPath(id));
        List<String> fileNames = parsingCustomPayloads(config, config.getLogPath());
        if (CollectionUtil.isNotEmpty(fileNames)) {
            config.setFileName(String.join(",", fileNames));
        }
        return config;
    }

    private String getTuningLogPath(String id) {
        // create log file
        String tuningRecordsPath = String.format(FixedTuning.COMMAND_EXECUTE_LOG_PATH, id);
        File file = new File(tuningRecordsPath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException exception) {
                throw new ServiceException(exception.getMessage());
            }
        }
        return tuningRecordsPath;
    }

    private SysConfig buildSysConfig(DbTypeEnum dbType, String url, String username, String password) {
        return SysConfig.builder()
                .userName(username)
                .password(password)
                .driver(dbType.getDriverClass())
                .url(url)
                .build();
    }

    private OpsJdbcDbClusterNodeEntity getNode(String clusterName) {
        Optional<OpsJdbcDbClusterEntity> optional = getClusters().stream()
                .filter(entity -> entity.getName().equals(clusterName))
                .findFirst();
        AssertUtil.isTrue(!optional.isPresent(), "Failed to obtain database cluster");
        Optional<OpsJdbcDbClusterNodeEntity> node = nodeService.list().stream()
                .filter(entity -> entity.getClusterId().equals(optional.get().getClusterId()))
                .findFirst();
        AssertUtil.isTrue(!node.isPresent(), "Failed to obtain database connection information");
        return node.get();
    }

    private Optional<LinuxConfig> getLinuxConfig(String host, String user) {
        List<OpsHostEntity> entities = hostService.list();
        List<OpsHostUserEntity> userEntities = hostUserService.list();
        Map<String, OpsHostUserEntity> userMap = userEntities.stream().filter(item -> item.getUsername().equals(user))
                .collect(Collectors.toMap(OpsHostUserEntity::getHostId, Function.identity()));
        return entities.stream()
                .filter(entity -> entity.getPublicIp().equals(host))
                .findFirst()
                .flatMap(entity -> {
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
    public RespBean getCluster() {
        List<String> list = getClusters().stream().map(OpsJdbcDbClusterEntity::getName).collect(Collectors.toList());
        return RespBean.success("success", list);
    }

    private List<OpsJdbcDbClusterEntity> getClusters() {
        return opsJdbcDbClusterService.list();
    }

    @Override
    public RespBean getDatabase(String clusterName) {
        try (Connection connection = getConnection(clusterName, "")) {
            List<String> databases = retrieveDatabases(connection, FixedTuning.SEARCH_DATABASE, FixedTuning.SEARCH_RES);
            return RespBean.success("success", databases);
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public RespBean getSchema(String clusterName) {
        try (Connection connection = getConnection(clusterName, "")) {
            List<String> schema = retrieveDatabases(connection, FixedTuning.SEARCH, FixedTuning.RES);
            return RespBean.success("success", schema);
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private Connection getConnection(String clusterName, String dbName) {
        OpsJdbcDbClusterNodeEntity node = getNode(clusterName);
        if (StrUtil.isNotEmpty(dbName)) {
            node.setUrl(node.getUrl().replace("postgres", dbName));
        }
        SysConfig gaussConfig = buildSysConfig(DbTypeEnum.OPENGAUSS, node.getUrl(),
                node.getUsername(), node.getPassword());
        return ConnectionUtils.getConnection(gaussConfig);
    }

    private List<String> retrieveDatabases(Connection connection, String sql, String searches) throws SQLException {
        List<String> databases = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String dbName = rs.getString(searches);
                databases.add(dbName);
            }
        }
        return databases;
    }

    @Override
    public RespBean getAllTuning(TrainingConfig trainingConfig, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<TrainingConfig> configs = configMpper.selectAllConfig(trainingConfig.getClusterName(),
                trainingConfig.getDb(), trainingConfig.getStartTime(), trainingConfig.getEndTime());
        PageInfo<TrainingConfig> pageInfo = new PageInfo<>(configs);
        return RespBean.success("success", configs, (int) pageInfo.getTotal());
    }

    @Override
    public RespBean getTuningParm(Long trainingId) {
        List<TrainingProgress> progresses = progressMapper
                .selectList(new QueryWrapper<TrainingProgress>().eq("training_id", trainingId));
        IntStream.range(0, progresses.size())
                .forEach(i -> progresses.get(i).setTuningRound(String.valueOf(i)));
        return RespBean.success("success", progresses);
    }

    @Override
    public int selectCountById(String trainingId) {
        List<TrainingProgress> progresses = progressMapper
                .selectList(new QueryWrapper<TrainingProgress>().eq("training_id", trainingId));
        return progresses.size();
    }

    @Override
    public RespBean getParameterRecommendation(ParameterRecommendation recommendation, int pageNum, int pageSize) {
        List<String> times = recommendation.getTimeInterval();
        if (CollectionUtil.isNotEmpty(times) && times.size() == 2) {
            recommendation.setStartTime(times.get(0));
            recommendation.setEndTime(times.get(1));
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ParameterRecommendation> paginatedList = recommendationMapper.selectRecommendation(recommendation);
        PageInfo<ParameterRecommendation> pageInfo = new PageInfo<>(paginatedList);
        return RespBean.success("success", paginatedList, (int) pageInfo.getTotal());
    }

    @Override
    public RespBean getParameterView(String parameterId) {
        ParameterRecommendation recommendation = recommendationMapper.selectById(parameterId);
        AssertUtil.isTrue(ObjectUtil.isEmpty(recommendation), "There is no record of this");
        String initialParameterJSON = recommendation.getInitialParameterValues();
        String suggestedParameterJSON = recommendation.getAdviseParameterValues();
        // Parsing JSON and obtaining key value pairs
        JSONObject initialParameterObj = JSONObject.parseObject(initialParameterJSON);
        JSONObject suggestedParameterObj = JSONObject.parseObject(suggestedParameterJSON);
        List<ParameterShow> parameterShows = new ArrayList<>();
        for (String key : initialParameterObj.keySet()) {
            String initialParameterValue = initialParameterObj.getString(key);
            String suggestedParameterValue = suggestedParameterObj.getString(key);
            ParameterShow parameterShow = new ParameterShow(key, initialParameterValue, suggestedParameterValue,
                    ParamEnum.valueOf(key.toUpperCase(Locale.ROOT)).getReal());
            parameterShows.add(parameterShow);
        }
        return RespBean.success("success", parameterShows);
    }

    @Override
    public RespBean getOperationLog(LogVo logVo, int pageNum, int pageSize) {
        String startTime = "";
        String endTime = "";
        List<String> times = logVo.getTimeInterval();
        if (CollectionUtil.isNotEmpty(times) && times.size() == 2) {
            startTime = logVo.getTimeInterval().get(0);
            endTime = logVo.getTimeInterval().get(1);
        }
        long count = tuningLogMapper.selectCount(new QueryWrapper<>());
        PageHelper.startPage(pageNum, pageSize);
        List<TuningLog> list = tuningLogMapper.selectAllLog(logVo.getClusterName(), startTime, endTime);
        PageInfo<TuningLog> pageInfo = new PageInfo<>(list);
        list.forEach(this::setLogDetails);
        return RespBean.success("success", list, (int) pageInfo.getTotal());
    }

    @Override
    public RespBean apply(ApplyVo applyVo) {
        AssertUtil.isTrue(CollectionUtil.isEmpty(applyVo.getData()), "The parameter cannot be empty");
        // Obtain information on the server where the OpenGaussian database is located and obtain a session
        TrainingConfig config = configMpper.selectById(applyVo.getTrainingId());
        AssertUtil.isTrue(ObjectUtil.isEmpty(config), "Training record does not exist");
        Optional<LinuxConfig> optional = getLinuxConfig(config.getHost(), config.getOsUser());
        AssertUtil.isTrue(!optional.isPresent(), "Failed to obtain openGauss database server information");
        // 获取omm用户session
        LinuxConfig dbUser = optional.get();
        dbUser.setUserName(config.getOsUser());
        dbUser.setPassword(config.getOmmPassword());
        Session session = JschUtil.obtainSession(dbUser);
        boolean isAll = applyVo.getData().stream().allMatch(s -> s.getRestart()
                .equalsIgnoreCase(FixedTuning.NO));
        boolean isOnLine = FixedTuning.TRUE.equals(config.getOnline());
        String command = getOptCommand(applyVo.getIsOptimization(),
                applyVo.getData(), isOnLine) + FixedTuning.DETERMINE;
        String res = JschUtil.executeCommand(session, command);
        if (res.contains(FixedTuning.SUCCESS_INSTALL) && !isAll && !isOnLine) {
            String restartResult = JschUtil.executeCommand(session, FixedTuning.DATABASE_COMMAND_RESTART);
            log.info("The result of restarting the database is-->{}", restartResult);
        }
        return RespBean.success("success", String.format("Execute command:%s The result is:%s", command, res));
    }

    private String getOptCommand(Boolean isOpt, List<ParameterShow> show, boolean isOnLine) {
        StringBuilder commandBuilder = new StringBuilder();
        for (ParameterShow param : show) {
            String parameterName = param.getParameterName();
            String parameterValues = isOpt
                    ? param.getSuggestedParameterValues() : param.getInitialParameterValues();
            if (StrUtil.isNotEmpty(parameterValues)) {
                commandBuilder.append("\"").append(parameterName).append("=").append(parameterValues).append("\"")
                        .append(" -c ");
            }
        }
        String core = commandBuilder.toString();
        String applyToDatabaseCommand = isOnLine
                ? FixedTuning.APPLY_TO_DATABASE_COMMAND_ONLINE : FixedTuning.APPLY_TO_DATABASE_COMMAND_OFFLINE;
        String command = String.format(applyToDatabaseCommand, core);
        int lastCPos = command.lastIndexOf(" -c");
        return command.substring(0, lastCPos).trim();
    }

    private void setLogDetails(TuningLog log) {
        log.setDetails(Arrays.asList(
                createLogDetail("DB", log.getDbStartTime(), log.getDbSize(), log.getDbPath()),
                createLogDetail("TUNE", log.getTuningStartTime(), log.getTuningSize(), log.getTuningPath()),
                createLogDetail("BENCHMARK", log.getPressureStartTime(), log.getPressureSize(),
                        log.getPressurePath())));
    }

    private TuningLogDetails createLogDetail(String name, String startTime, String size, String path) {
        return new TuningLogDetails(name, startTime, size, path);
    }

    @Override
    public void downloadLog(String trainingId, String type, HttpServletResponse response) {
        try {
            File file = new File(LogPathEnum.valueOf(type).getLogPath(trainingId));
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + file.getName());
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
    public RespBean getOperationClusterName() {
        List<String> names = tuningLogMapper.selectList(new QueryWrapper<>()).stream()
                .map(TuningLog::getClusterName).distinct().collect(Collectors.toList());
        return RespBean.success("success", names);
    }

    @Override
    public RespBean getrRecommendationDatabase() {
        return RespBean.success("success", recommendationMapper.selectdbs());
    }

    @Override
    public RespBean getrRecommendationClusterName() {
        return RespBean.success("success", recommendationMapper.selectClusterNames());
    }

    @Override
    public RespBean getTaskclusterName() {
        return RespBean.success("success", configMpper.selectClusterNames());
    }

    @Override
    public RespBean getTaskDatabase() {
        return RespBean.success("success", configMpper.selectdbs());
    }

    @Override
    public RespBean preview(String trainingId, String type) {
        try {
            File file = new File(LogPathEnum.valueOf(type).getLogPath(trainingId));
            if (file.exists() && file.length() > 0) {
                StringBuilder contentBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        contentBuilder.append(line).append(StrUtil.LF);
                    }
                }
                return RespBean.success("success", contentBuilder.toString());
            } else {
                return RespBean.success("success", "");
            }
        } catch (IOException exception) {
            throw new ServiceException("The download failed because of an abnormal IO stream.");
        }
    }

    @Override
    public RespBean viewTaskExecutionLogs(TaskLogVo logVo) {
        try {
            byte[] contentBytes = Files.readAllBytes(Paths.get(logVo.getLogPath()));
            String content = new String(contentBytes, StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                return RespBean.success("success", "");
            } else {
                return RespBean.success("success", content);
            }
        } catch (IOException exception) {
            throw new ServiceException("The download failed because of an abnormal IO stream.");
        }
    }

    @Override
    public void downloadTaskExecutionLogs(TaskLogVo logVo, HttpServletResponse response) {
        try {
            File file = new File(logVo.getLogPath());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + file.getName());
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
    public RespBean getFilesById(String id) {
        String path = FixedTuning.UPLOAD_FILE_PATH;
        TrainingConfig config = configMpper.selectById(id);
        if (StrUtil.isNotEmpty(config.getFileName())) {
            List<String> names = Arrays.asList(config.getFileName().split(","));
            List<Map<String, Object>> files = FileLoader.getFilesInDirectory(path, names);
            return RespBean.success("success", files);
        } else {
            return RespBean.success("success", new ArrayList<>());
        }
    }

    @Override
    public TrainingConfig selectById(String id) {
        return configMpper.selectById(id);
    }

    @Override
    public RespBean deleteTasks(List<String> ids) {
        CommandLineRunner.runCommand(FixedTuning.STOP, FixedTuning.WORK_PATH, "", FixedTuning.TIME_OUT);
        return RespBean.success("success", configMpper.deleteBatchIds(ids));
    }

    @Override
    public RespBean stopTask(List<String> ids) {
        // 更新状态,每次只会更新一个id,不会有多个
        String killSysbench = "pkill sysbench";
        ids.forEach(item -> {
            TrainingConfig config = configMpper.selectById(item);
            AssertUtil.isTrue(!config.getStatus().equals(FixedTuning.RUNING), "You cannot perform this operation"
                    + " on non running tasks");
            String path = String.format(FixedTuning.EXECUTE_FILE_PATH, config.getClusterName()) + FixedTuning.MAIN_PATH;
            CommandLineRunner.runCommand(FixedTuning.STOP, path, "", FixedTuning.TIME_OUT);
            CommandLineRunner.runCommand(killSysbench, path, "", FixedTuning.TIME_OUT);
            TuningHelper.updateStatusFailed(item);
        });
        return RespBean.success("Stopped successfully");
    }
}
