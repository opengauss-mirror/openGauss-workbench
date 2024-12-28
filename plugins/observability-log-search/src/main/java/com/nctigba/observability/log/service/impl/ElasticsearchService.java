
/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ElasticsearchService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/service/ElasticsearchService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.log.config.ElasticsearchProvider;
import com.nctigba.observability.log.enums.AgentStatusEnum;
import com.nctigba.observability.log.model.dto.ElasticSearchInstallDTO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.InstallType;
import com.nctigba.observability.log.model.vo.AgentExceptionVO;
import com.nctigba.observability.log.model.vo.AgentStatusVO;
import com.nctigba.observability.log.service.AbstractInstaller;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.service.AgentService;
import com.nctigba.observability.log.util.CommonUtils;
import com.nctigba.observability.log.util.Download;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.nctigba.observability.log.constants.CommonConstants.AWK_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.DIRECTORY_IS_EMPTY;
import static com.nctigba.observability.log.constants.CommonConstants.DIRECTORY_IS_EXIST;
import static com.nctigba.observability.log.constants.CommonConstants.FILE_IS_EXIST;
import static com.nctigba.observability.log.constants.CommonConstants.JAVA_CLASSPATH;
import static com.nctigba.observability.log.constants.CommonConstants.MAX_MAP_COUNT;
import static com.nctigba.observability.log.constants.CommonConstants.MILLISECOND;
import static com.nctigba.observability.log.constants.CommonConstants.MKDIR_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.MONITOR_CYCLE;
import static com.nctigba.observability.log.constants.CommonConstants.PARENT_PID;
import static com.nctigba.observability.log.constants.CommonConstants.PID_PATH;
import static com.nctigba.observability.log.constants.CommonConstants.PORT_IS_EXIST;
import static com.nctigba.observability.log.constants.CommonConstants.PORT_PID;
import static com.nctigba.observability.log.constants.CommonConstants.RM_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.SED_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.TIMEOUT;
import static com.nctigba.observability.log.constants.CommonConstants.U_LIMIT;

/**
 * ElasticsearchService
 *
 * @author luomeng
 * @since 2024/1/5
 */
@Slf4j
@Service("ElasticsearchService")
public class ElasticsearchService extends AbstractInstaller implements AgentService {
    /**
     * Es download url
     */
    public static final String PATH = "https://artifacts.elastic.co/downloads/elasticsearch/";

    /**
     * Es version
     */
    public static final String VERSION = "8.3.3";

    /**
     * Es file name
     */
    public static final String NAME = "elasticsearch-" + VERSION + "-linux-";

    /**
     * Es SRC
     */
    public static final String SRC = "elasticsearch-" + VERSION;
    private static final String STOP_ELASTICSEARCH = "cd %s && sh run_elasticsearch.sh stop";
    private static final String START_ELASTICSEARCH = "cd %s && sh run_elasticsearch.sh start";
    private static final String CHECK_ELASTICSEARCH = "cd %s && sh run_elasticsearch.sh status";
    private static final String RESTART_FILEBEAT = "cd %s && sh run_filebeat.sh restart";

    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ElasticsearchProvider provider;
    @Autowired
    private CommonUtils utils;

    /**
     * Install elasticsearch
     *
     * @param wsSession  Websocket
     * @param installDTO Install dto
     */
    public void install(WsSession wsSession, ElasticSearchInstallDTO installDTO) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("elastic.install.step1"),
                new Step("elastic.install.step2"),
                new Step("elastic.install.step3"),
                new Step("elastic.install.step4"),
                new Step("elastic.install.step5"),
                new Step("elastic.install.step6"),
                new Step("elastic.install.step7"));
        initWsSessionStepTl(wsSession, steps);
        install(installDTO);
        if (wsSessionStepTl.get() != null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * Install elasticsearch
     *
     * @param installDTO Install dto
     */
    public void install(ElasticSearchInstallDTO installDTO) {
        String path = installDTO.getPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        // step1
        try {
            var env = envMapper
                .selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
            if (env != null) {
                throw new CustomException("elastic.install.exists.tip");
            }
            env = new NctigbaEnvDO().setHostid(installDTO.getHostId()).setPort(installDTO.getPort()).setPath(
                    path).setType(InstallType.ELASTICSEARCH)
                .setPath(path + SRC);
            OpsHostEntity hostEntity = hostFacade.getById(installDTO.getHostId());
            if (hostEntity == null) {
                throw new CustomException("elastic.install.host.tip");
            }
            env.setHost(hostEntity);
            var user = getUser(hostEntity, installDTO.getUsername());
            env.setUsername(installDTO.getUsername());
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                installDTO.getUsername(),
                encryptionUtils.decrypt(user.getPassword()))) {
                String message = session.execute(String.format(PORT_IS_EXIST, env.getPort()));
                if (message.contains("true")) {
                    throw new CustomException("elastic.install.port.tip");
                }
                String installPath = session.execute(String.format(DIRECTORY_IS_EXIST, path));
                if (installPath.contains("false")) {
                    session.execute(String.format(MKDIR_FILE, path));
                } else {
                    String fileIsEmpty = session.execute(String.format(DIRECTORY_IS_EMPTY, path));
                    if (fileIsEmpty.startsWith("/")) {
                        throw new CustomException("elastic.install.folder.tip");
                    }
                }
                var vm = session.execute(MAX_MAP_COUNT);
                if (NumberUtil.parseInt(vm) < 262144) {
                    throw new CustomException("elastic.install.max_map_count.tip");
                }
                String configCheck = session.execute(U_LIMIT);
                if (NumberUtil.parseInt(configCheck) < 65535) {
                    throw new CustomException("elastic.install.nofile.tip");
                }
                String classpathCheck = session.execute(JAVA_CLASSPATH);
                if (!StrUtil.isBlank(classpathCheck)) {
                    String[] classpathList = classpathCheck.split(":");
                    for (String classpath : classpathList) {
                        if (".".equals(classpath)) {
                            continue;
                        }
                        String pathCheck = session.execute(String.format(FILE_IS_EXIST, classpath));
                        if ("false".equals(pathCheck)) {
                            throw new CustomException("elastic.install.classpath.tip");
                        }
                    }
                }
            }
            nextStep();
            // step2
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                installDTO.getUsername(),
                encryptionUtils.decrypt(user.getPassword()))) {
                var arch = session.execute(command.ARCH);
                String name = NAME + arch;
                String tar = name + TAR;
                var pkg = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery()
                    .eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH_PKG).like(
                        NctigbaEnvDO::getPath, tar));
                boolean isDownload = false;
                if (pkg == null) {
                    isDownload = true;
                } else {
                    Path pkgPath = Paths.get(pkg.getPath());
                    boolean isFileExists = Files.exists(pkgPath);
                    if (!isFileExists) {
                        isDownload = true;
                    }
                }
                if (isDownload) {
                    sendMsg(null, "elastic.install.download.start");
                    File f = Download.download(PATH + tar, "pkg/" + tar);
                    pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType(InstallType.ELASTICSEARCH_PKG);
                    sendMsg(null, "elastic.install.download.success");
                    save(pkg);
                }
                nextStep();
                // step3
                session.upload(pkg.getPath(), path + tar);
                session.execute("cd " + path + " && " + command.TAR.parse(tar));
            }
            nextStep();
            // step4
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                installDTO.getUsername(),
                encryptionUtils.decrypt(user.getPassword()))) {
                // config
                var elasticConfigFile = File.createTempFile("elasticsearch", ".yml");
                // @formatter:off
                FileUtil.appendUtf8String(
                    "path.data: " + "data" + System.lineSeparator()
                        + "path.logs: " + "logs" + System.lineSeparator()
                        + "cluster.name: dbTools-es" + System.lineSeparator()
                        + "cluster.initial_master_nodes: [\"node1\"]" + System.lineSeparator()
                        + "discovery.seed_hosts: [\"127.0.0.1\"]" + System.lineSeparator()
                        + "node.name: node1" + System.lineSeparator()
                        + "network.host: 0.0.0.0" + System.lineSeparator()
                        + "http.port: " + installDTO.getPort() + System.lineSeparator()
                        + "http.host: 0.0.0.0" + System.lineSeparator()
                        + "transport.host: 0.0.0.0" + System.lineSeparator()
                        + "http.cors.enabled: true" + System.lineSeparator()
                        + "http.cors.allow-origin: \"*\"" + System.lineSeparator()
                        + "xpack.security.enrollment.enabled: true" + System.lineSeparator()
                        + "xpack.security.enabled: false" + System.lineSeparator()
                        + "xpack.security.http.ssl.enabled: false" + System.lineSeparator()
                        + "xpack.security.transport.ssl.enabled: false", elasticConfigFile);
                // @formatter:on
                session.execute(String.format(MKDIR_FILE, path + SRC + "/config"));
                session.upload(elasticConfigFile.getAbsolutePath(), path + SRC + "/config/elasticsearch.yml");
                Files.delete(elasticConfigFile.toPath());
                // jvm config
                try (InputStream in = loader.getResource("jvm.options").getInputStream()) {
                    session.upload(in, path + SRC + "/config/jvm.options");
                }
                // run shell
                utils.uploadShellScript(session, path + SRC + "/", "run_elasticsearch.sh");
            }
            envMapper.insert(env);
            nextStep();
            // step5
            updateFilebeatSet(hostEntity.getPublicIp(), env.getPort());
            startElasticsearch(env);
            nextStep();
            // step6
            checkHealthStatus(env);
            nextStep();
            // step7
            sendMsg(status.DONE, "");
        } catch (Exception e) {
            sendMsg(status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            log.error("install fail!", e);
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("install fail! " + e.getMessage());
            }
        }
    }

    /**
     * Uninstall elasticsearch
     *
     * @param wsSession Websocket
     * @param id        Unique ID
     */
    public void uninstall(WsSession wsSession, String id) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("elastic.uninstall.step1"),
                new Step("elastic.uninstall.step2"),
                new Step("elastic.uninstall.step3"),
                new Step("elastic.uninstall.step4"),
                new Step("elastic.uninstall.step5"));
        initWsSessionStepTl(wsSession, steps);
        uninstall(id);
        if (wsSessionStepTl.get() != null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * uninstall
     *
     * @param id envId
     */
    public void uninstall(String id) {
        // step1
        try {
            var env = envMapper.selectById(id);
            if (env == null) {
                throw new CustomException("elastic.uninstall.id.tip");
            }
            OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
            if (hostEntity == null) {
                throw new CustomException("elastic.uninstall.host.tip");
            }
            env.setHost(hostEntity);
            // step2
            nextStep();
            uninstallElasticsearch(env);
            // step3
            nextStep();
            clearInstallFolder(env);
            // step4
            nextStep();
            envMapper.deleteById(id);
            provider.clear();
            // step5
            sendMsg(status.DONE, "");
        } catch (Exception e) {
            sendMsg(status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            log.error("uninstall fail! ", e);
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("uninstall fail! " + e.getMessage());
            }
        }
    }

    @Override
    public void start(String id) {
        NctigbaEnvDO env = getAgentInfo(id);
        startElasticsearch(env);
        checkHealthStatus(env);
    }

    @Override
    public void stop(String id) {
        NctigbaEnvDO env = getAgentInfo(id);
        stopElasticsearch(env);
    }

    @Override
    public List<AgentStatusVO> getAgentStatus() {
        List<AgentStatusVO> list = envMapper.selectStatusByType(InstallType.ELASTICSEARCH.name());
        list.forEach(f -> {
            String status = f.getStatus();
            Date updateTime = f.getUpdateTime();
            if (StrUtil.isBlank(status) || updateTime == null) {
                f.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
            } else {
                boolean isStop = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status);
                boolean isRunning =
                        AgentStatusEnum.STARTING.getStatus().equals(status)
                                || AgentStatusEnum.STOPPING.getStatus().equals(
                                status);
                boolean isRunTimeout = isRunning
                        && new Date().getTime() - f.getUpdateTime().getTime() > TIMEOUT * MONITOR_CYCLE * MILLISECOND;
                boolean isTimeout = new Date().getTime() - f.getUpdateTime().getTime() > MONITOR_CYCLE * MILLISECOND;
                boolean isNormalTimeout = !isStop && !isRunning && isTimeout;
                if (isNormalTimeout || isRunTimeout) {
                    f.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
                }
            }
        });
        return list;
    }

    /**
     * Monitor status and auto pull up
     */
    @Scheduled(fixedDelay = MONITOR_CYCLE, timeUnit = TimeUnit.SECONDS)
    public void monitorStatus() {
        List<NctigbaEnvDO> envList = envMapper
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
        envList.forEach(e -> e.setHost(hostFacade.getById(e.getHostid())));
        for (NctigbaEnvDO env : envList) {
            String status = env.getStatus();
            if (StrUtil.isBlank(status)) {
                oldVersionAdapter(env);
            }
            if (env.getUpdateTime() == null) {
                env.setUpdateTime(new Date());
            }
            boolean isTimeout =
                    new Date().getTime() - env.getUpdateTime().getTime() > TIMEOUT * MONITOR_CYCLE * MILLISECOND;
            boolean isDuring =
                    (AgentStatusEnum.STARTING.getStatus().equals(status) || AgentStatusEnum.STOPPING.getStatus().equals(
                            status)) && !isTimeout;
            boolean isSkip = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status) || isDuring;
            if (isSkip) {
                continue;
            }
            AgentExceptionVO check = checkPidStatus(env);
            if (!check.isStatus()) {
                startElasticsearch(env);
            }
            if (getHealthStatus(env)) {
                status = AgentStatusEnum.NORMAL.getStatus();
            } else {
                status = AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus();
            }
            env.setEnvStatus(status);
            envMapper.updateById(env);
        }
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            utils.uploadShellScript(session, env.getPath() + "/", "run_elasticsearch.sh");
            String pid = session.execute(String.format(PORT_PID, env.getPort()));
            if (!StrUtil.isBlank(pid)) {
                String parentPid = session.execute(String.format(PARENT_PID, pid));
                String content = !StrUtil.isBlank(parentPid) && !"1".equals(parentPid.trim()) ? parentPid : pid;
                File pidFile = File.createTempFile("elasticsearch", ".pid");
                String pidPath = session.execute(String.format(PID_PATH, content));
                if (env.getPath().equals(pidPath)) {
                    FileUtil.appendUtf8String(content, pidFile);
                    session.upload(pidFile.getCanonicalPath(), env.getPath() + "/elasticsearch.pid");
                }
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private void startElasticsearch(NctigbaEnvDO env) {
        AgentExceptionVO check = checkPidStatus(env);
        if (check.isStatus()) {
            throw new CustomException(check.getExceptionInfo());
        }
        check = checkRunningEnvironment(env);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        env.setEnvStatus(AgentStatusEnum.STARTING.getStatus());
        envMapper.updateById(env);
        check = execStartCmd(env);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
    }

    private void stopElasticsearch(NctigbaEnvDO env) {
        AgentExceptionVO check = checkPidStatus(env);
        if (!check.isStatus()) {
            if (check.getExceptionInfo().contains("No such file or directory")) {
                killPid(env);
                env.setEnvStatus(AgentStatusEnum.MANUAL_STOP.getStatus());
                envMapper.updateById(env);
                return;
            } else {
                env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
                envMapper.updateById(env);
                throw new CustomException(check.getExceptionInfo());
            }
        }
        stopping(env);
    }

    private void uninstallElasticsearch(NctigbaEnvDO env) {
        if (env.getStatus() == null || "".equals(env.getStatus())) {
            oldVersionAdapter(env);
        }
        AgentExceptionVO check = checkPidStatus(env);
        if (!check.isStatus()) {
            if (check.getExceptionInfo().contains("No such file or directory")) {
                killPid(env);
            }
            return;
        }
        stopping(env);
    }

    private void stopping(NctigbaEnvDO env) {
        env.setEnvStatus(AgentStatusEnum.STOPPING.getStatus());
        envMapper.updateById(env);
        AgentExceptionVO check = execStopCmd(env);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        check = checkPidStatus(env);
        if (check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        env.setEnvStatus(AgentStatusEnum.MANUAL_STOP.getStatus());
        envMapper.updateById(env);
    }

    private void killPid(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            String pid = session.execute(String.format(PORT_PID, env.getPort()));
            if (StrUtil.isNotBlank(pid) && NumberUtil.isLong(pid)) {
                String parentPid = session.execute(String.format(PARENT_PID, pid));
                String actualPid = !StrUtil.isBlank(parentPid) && !"1".equals(parentPid.trim()) ? parentPid : pid;
                String pidPath = session.execute(String.format(PID_PATH, actualPid));
                if (env.getPath().equals(pidPath)) {
                    session.execute(command.KILL.parse(actualPid));
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException("exec failed:" + e.getMessage());
        }
    }

    private AgentExceptionVO checkPidStatus(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
            String message = session.execute(String.format(CHECK_ELASTICSEARCH, env.getPath()));
            agentExceptionVO.setAgentStatus(message.contains("Elasticsearch is running with PID"), message);
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    private boolean getHealthStatus(NctigbaEnvDO env) {
        try {
            HttpUtil.get("http://" + IpUtils.formatIp(env.getHost().getPublicIp()) + ":" + env.getPort());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void checkHealthStatus(NctigbaEnvDO env) {
        for (int i = 0; i < 60; i++) {
            ThreadUtil.sleep(3000L);
            try {
                HttpUtil.get("http://" + IpUtils.formatIp(env.getHost().getPublicIp()) + ":" + env.getPort());
                break;
            } catch (Exception e) {
                if (i == 59) {
                    env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
                    envMapper.updateById(env);
                    throw new CustomException(e.getMessage());
                }
            }
        }
        env.setEnvStatus(AgentStatusEnum.NORMAL.getStatus());
        envMapper.updateById(env);
    }

    private AgentExceptionVO checkRunningEnvironment(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
            String message = session.execute(String.format(PORT_IS_EXIST, env.getPort()));
            if (message.contains("true")) {
                return agentExceptionVO.setAgentStatus(false, "port is exists!");
            }
            String pkgPath = session.execute(String.format(DIRECTORY_IS_EXIST, env.getPath()));
            if (pkgPath.contains("false")) {
                return agentExceptionVO.setAgentStatus(false, "package is not exists!");
            }
        } catch (IOException | RuntimeException e) {
            return agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        agentExceptionVO.setStatus(true);
        return agentExceptionVO;
    }

    private AgentExceptionVO execStartCmd(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
            session.executeNoWait(String.format(START_ELASTICSEARCH, env.getPath()));
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
            return agentExceptionVO;
        }
        agentExceptionVO.setStatus(true);
        return agentExceptionVO;
    }

    private AgentExceptionVO execStopCmd(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
            session.execute(String.format(STOP_ELASTICSEARCH, env.getPath()));
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
            return agentExceptionVO;
        }
        agentExceptionVO.setStatus(true);
        return agentExceptionVO;
    }

    private NctigbaEnvDO getAgentInfo(String id) {
        NctigbaEnvDO env = envMapper.selectById(id);
        if (env == null) {
            throw new CustomException("elasticsearch not found");
        }
        env.setHost(hostFacade.getById(env.getHostid()));
        return env;
    }

    private SshSession connect(NctigbaEnvDO env) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
        if (hostEntity == null) {
            throw new CustomException("host not found");
        }
        env.setHost(hostEntity);
        OpsHostUserEntity user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(
                e -> env.getUsername().equals(e.getUsername())).findFirst().orElse(null);
        if (user == null) {
            throw new CustomException(env.getUsername() + " not found");
        }
        return SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), env.getUsername(),
                encryptionUtils.decrypt(user.getPassword()));
    }

    private void clearInstallFolder(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            int index = env.getPath().lastIndexOf("/");
            String path = env.getPath().substring(0, index);
            String pkgPath = session.execute(String.format(DIRECTORY_IS_EXIST, path));
            if (env.getPath() != null && !pkgPath.contains("false")) {
                Thread.sleep(2000L);
                session.execute(String.format(RM_FILE, path));
            }
        } catch (IOException | InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private void updateFilebeatSet(String publicId, Integer port) {
        List<NctigbaEnvDO> filebeatList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.FILEBEAT));
        for (NctigbaEnvDO envDO : filebeatList) {
            OpsHostEntity hostEntity = hostFacade.getById(envDO.getHostid());
            OpsHostUserEntity opsHostUser = getUser(hostEntity, envDO.getUsername());
            try (SshSession session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                    envDO.getUsername(), encryptionUtils.decrypt(opsHostUser.getPassword()))) {
                String ipAddress = session.execute(String.format(AWK_FILE, envDO.getPath()));
                session.execute(
                    String.format(
                        SED_FILE,
                        ipAddress,
                        IpUtils.formatIp(publicId) + ":" + port, envDO.getPath() + "/filebeat.yml"));
                session.execute(String.format(RESTART_FILEBEAT, envDO.getPath()));
            } catch (IOException e) {
                throw new CustomException(e.getMessage());
            }
        }
    }
}