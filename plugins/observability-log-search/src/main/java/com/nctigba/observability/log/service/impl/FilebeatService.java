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
 *  FilebeatService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/service/FilebeatService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.enums.AgentStatusEnum;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.InstallType;
import com.nctigba.observability.log.model.vo.AgentExceptionVO;
import com.nctigba.observability.log.model.vo.AgentStatusVO;
import com.nctigba.observability.log.service.AbstractInstaller;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.service.AgentService;
import com.nctigba.observability.log.service.ClusterManager;
import com.nctigba.observability.log.util.CommonUtils;
import com.nctigba.observability.log.util.Download;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
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

import static com.nctigba.observability.log.constants.CommonConstants.CP_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.DIRECTORY_IS_EMPTY;
import static com.nctigba.observability.log.constants.CommonConstants.DIRECTORY_IS_EXIST;
import static com.nctigba.observability.log.constants.CommonConstants.FILEBEAT_HEALTH_STATUS;
import static com.nctigba.observability.log.constants.CommonConstants.MILLISECOND;
import static com.nctigba.observability.log.constants.CommonConstants.MKDIR_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.MONITOR_CYCLE;
import static com.nctigba.observability.log.constants.CommonConstants.PID_PATH;
import static com.nctigba.observability.log.constants.CommonConstants.RM_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.RM_F_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.TAR_XVF_FILE;
import static com.nctigba.observability.log.constants.CommonConstants.TIMEOUT;

/**
 * FilebeatService
 *
 * @author luomeng
 * @since 2024/1/5
 */
@Service("FilebeatService")
@Slf4j
public class FilebeatService extends AbstractInstaller implements AgentService {
    /**
     * Filebeat download url
     */
    public static final String PATH = "https://artifacts.elastic.co/downloads/beats/filebeat/";

    /**
     * Filebeat file name
     */
    public static final String NAME = "filebeat-8.3.3-linux-";
    private static final String STOP_FILEBEAT = "cd %s && sh run_filebeat.sh stop";
    private static final String START_FILEBEAT = "cd %s && sh run_filebeat.sh start";
    private static final String CHECK_FILEBEAT = "cd %s && sh run_filebeat.sh status";
    private static final String CONF_FILE_NAME = "filebeat_conf";
    private static final String FILE_TYPE = ".tar.gz";

    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private CommonUtils utils;

    /**
     * Install filebeat
     *
     * @param wsSession Websocket
     * @param path      Install path
     * @param nodeId    Install node
     * @param obj       Log info
     */
    public void install(WsSession wsSession, String path, String nodeId, JSONObject obj) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("filebeat.install.step1"),
                new Step("filebeat.install.step2"),
                new Step("filebeat.install.step3"),
                new Step("filebeat.install.step4"),
                new Step("filebeat.install.step5"),
                new Step("filebeat.install.step6"),
                new Step("filebeat.install.step7"));
        initWsSessionStepTl(wsSession, steps);
        install(path, nodeId, obj);
        if (wsSessionStepTl.get() != null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * install
     *
     * @param path Install path
     * @param nodeId Install node
     * @param obj Log info
     */
    public void install(String path, String nodeId, JSONObject obj) {
        // @formatter:on
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        // step1
        try {
            var node = clusterManager.getOpsNodeById(nodeId);
            if (node == null) {
                throw new CustomException("filebeat.install.nodeId.tip");
            }
            var hostId = node.getHostId();
            var env = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getHostid, hostId)
                .eq(NctigbaEnvDO::getType, InstallType.FILEBEAT).eq(NctigbaEnvDO::getNodeid, nodeId));
            if (env != null) {
                throw new CustomException("filebeat.install.exists.tip");
            }
            env = new NctigbaEnvDO().setHostid(hostId).setType(InstallType.FILEBEAT).setNodeid(nodeId).setPath(path);
            var esEnv = envMapper
                .selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
            if (esEnv == null) {
                throw new CustomException("filebeat.install.elastic.tip");
            }
            if (StrUtil.isBlank(esEnv.getUsername())) {
                throw new CustomException("filebeat.install.elastic.installing.tip");
            }
            var opsClusterNodeEntity = opsClusterNodeService.getById(nodeId);
            if (opsClusterNodeEntity == null) {
                throw new CustomException("filebeat.install.nodeId.tip");
            }
            String installUserId = opsClusterNodeEntity.getInstallUserId();
            OpsHostUserEntity user = hostUserFacade.getById(installUserId);
            env.setUsername(user.getUsername());
            OpsHostEntity hostEntity = hostFacade.getById(hostId);
            if (hostEntity == null) {
                throw new CustomException("filebeat.install.host.tip");
            }
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                user.getUsername(), encryptionUtils.decrypt(user.getPassword()))) {
                String installPath = session.execute(String.format(DIRECTORY_IS_EXIST, path));
                if (installPath.contains("false")) {
                    session.execute(String.format(MKDIR_FILE, path));
                } else {
                    String fileIsEmpty = session.execute(String.format(DIRECTORY_IS_EMPTY, path));
                    if (fileIsEmpty.contains("false")) {
                        throw new CustomException("filebeat.install.folder.tip");
                    }
                }
            }
            nextStep();
            // step2
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                user.getUsername(), encryptionUtils.decrypt(user.getPassword()))) {
                String logPath = obj.getStr("logPath");
                if (!session.test(command.STAT.parse(logPath))) {
                    throw new CustomException("log path err:" + logPath);
                }
                var arch = session.execute(command.ARCH);
                String ver;
                switch (arch) {
                    case "aarch64":
                        ver = "arm64";
                        break;
                    case "x86_64":
                        ver = "x86_64";
                        break;
                    default:
                        ver = "x86_64";
                }
                String name = NAME + ver;
                env.setPath(path + name);
                String tar = name + TAR;
                var pkg = envMapper
                    .selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().like(NctigbaEnvDO::getPath, tar));
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
                    sendMsg(null, "filebeat.install.download.start");
                    var f = Download.download(PATH + tar, "pkg/" + tar);
                    pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType(InstallType.FILEBEAT_PKG);
                    sendMsg(null, "filebeat.install.download.success");
                    save(pkg);
                }
                nextStep();
                // step3
                session.upload(pkg.getPath(), path + tar);
                session.execute("cd " + path + " && " + command.TAR.parse(tar));
                nextStep();
                // step4
                // run shell
                utils.uploadShellScript(session, path + name + "/", "run_filebeat.sh");
                // filebeat_conf
                File f = File.createTempFile(CONF_FILE_NAME, FILE_TYPE);
                try (var in = loader.getResource(CONF_FILE_NAME + FILE_TYPE).getInputStream();
                     var out = new FileOutputStream(f)) {
                    IoUtil.copy(in, out);
                }
                session.upload(f.getCanonicalPath(), path + CONF_FILE_NAME + FILE_TYPE);
                Files.delete(f.toPath());
                session.execute("cd " + path + " && " + String.format(TAR_XVF_FILE, CONF_FILE_NAME + FILE_TYPE));
                var esHost = hostFacade.getById(esEnv.getHostid());
                var cluster = clusterManager.getOpsClusterByNodeId(nodeId);
                // upload config script
                utils.uploadShellScript(session, path + "/" + CONF_FILE_NAME + "/", "conf.sh");
                // @formatter:off
                session.execute("cd " + path + CONF_FILE_NAME + " && sh conf.sh"
                    + " --eshost " + esHost.getPublicIp() + ":" + esEnv.getPort()
                    + " --nodeid " + opsClusterNodeEntity.getClusterNodeId()
                    + " --clusterid " + cluster.getClusterId()
                    + " --opengausslog " + StrUtil.removeSuffix(logPath, "/")
                    + " --opengaussslowlog " + StrUtil.removeSuffix(obj.getStr("slowlogPath"), "/")
                    + " --opengausserrorlog " + StrUtil.removeSuffix(obj.getStr("errorlogPath"), "/")
                    + (StrUtil.isNotBlank(obj.getStr("gsCtlLogPath")) ? " --gsCtlLogPath "
                    + StrUtil.removeSuffix(obj.getStr("gsCtlLogPath"), "/") : "")
                    + (StrUtil.isNotBlank(obj.getStr("gsGucLogPath")) ? " --gsGucLogPath "
                    + StrUtil.removeSuffix(obj.getStr("gsGucLogPath"), "/") : "")
                    + (StrUtil.isNotBlank(obj.getStr("gsOmLogPath")) ? " --gsOmLogPath "
                    + StrUtil.removeSuffix(obj.getStr("gsOmLogPath"), "/") : "")
                    + (StrUtil.isNotBlank(obj.getStr("gsInstallLogPath")) ? " --gsInstallLogPath "
                    + StrUtil.removeSuffix(obj.getStr("gsInstallLogPath"), "/") : "")
                    + (StrUtil.isNotBlank(obj.getStr("gsLocalLogPath")) ? " --gsLocalLogPath "
                    + StrUtil.removeSuffix(obj.getStr("gsLocalLogPath"), "/") : "")
                    + (StrUtil.isNotBlank(obj.getStr("cmLogPath")) ? " --cmLogPath "
                    + StrUtil.removeSuffix(obj.getStr("cmLogPath"), "/") : ""));
                // @formatter:on
                session.execute(String.format(CP_FILE, path + CONF_FILE_NAME + "/*", path + name + "/"));
                session.execute(String.format(RM_FILE, path + CONF_FILE_NAME));
                session.execute(String.format(RM_F_FILE, path + CONF_FILE_NAME + FILE_TYPE));
            }
            envMapper.insert(env);
            nextStep();
            // step5
            startFilebeat(env);
            nextStep();
            // step6
            checkHealthStatus(env);
            nextStep();
            // step7
            sendMsg(status.DONE, "");
        } catch (Exception e) {
            log.error("install fail!", e);
            sendMsg(status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("install fail! " + e.getMessage());
            }
        }
    }

    /**
     * Uninstall filebeat
     *
     * @param wsSession Websocket
     * @param nodeId    install node
     */
    public void uninstall(WsSession wsSession, String nodeId) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("filebeat.uninstall.step1"),
                new Step("filebeat.uninstall.step2"),
                new Step("filebeat.uninstall.step3"),
                new Step("filebeat.uninstall.step4"),
                new Step("filebeat.uninstall.step5"));
        initWsSessionStepTl(wsSession, steps);
        uninstall(nodeId);
        if (wsSessionStepTl.get() != null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * uninstall
     *
     * @param nodeId String
     */
    public void uninstall(String nodeId) {
        // step1
        try {
            var node = clusterManager.getOpsNodeById(nodeId);
            var env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.FILEBEAT)
                    .eq(NctigbaEnvDO::getNodeid, node.getNodeId()));
            if (env == null) {
                throw new CustomException("filebeat.uninstall.id.tip");
            }
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (hostEntity == null) {
                throw new CustomException("filebeat.uninstall.host.tip");
            }
            env.setHost(hostEntity);
            // step2
            nextStep();
            uninstallFilebeat(env);
            // step3
            nextStep();
            clearInstallFolder(env);
            // step4
            nextStep();
            envMapper.deleteById(env);
            // step5
            sendMsg(status.DONE, "");
        } catch (Exception e) {
            sendMsg(status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            log.error("uninstall fail!", e);
            WsSessionStep wsSessionStep = wsSessionStepTl.get();
            if (wsSessionStep == null) {
                throw new CustomException("uninstall fail! " + e.getMessage());
            }
        }
    }

    private void clearInstallFolder(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            int index = env.getPath().lastIndexOf("/");
            String path = env.getPath().substring(0, index);
            String pkgPath = session.execute(String.format(DIRECTORY_IS_EXIST, path));
            if (env.getPath() != null && !pkgPath.contains("false")) {
                session.execute(String.format(RM_FILE, path));
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void start(String id) {
        NctigbaEnvDO env = getAgentInfo(id);
        startFilebeat(env);
        checkHealthStatus(env);
    }

    private void startFilebeat(NctigbaEnvDO env) {
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

    @Override
    public void stop(String id) {
        NctigbaEnvDO env = getAgentInfo(id);
        stopFilebeat(env);
    }

    private void uninstallFilebeat(NctigbaEnvDO env) {
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

    private void stopFilebeat(NctigbaEnvDO env) {
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
        String pid = getPid(env);
        try (SshSession session = connect(env)) {
            if (!"".equals(pid)) {
                session.execute(command.KILL.parse(pid));
            }
        } catch (IOException e) {
            throw new CustomException("exec failed:" + e.getMessage());
        }
    }

    @Override
    public List<AgentStatusVO> getAgentStatus() {
        List<AgentStatusVO> list = envMapper.selectStatusByType(InstallType.FILEBEAT.name());
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
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.FILEBEAT));
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
                startFilebeat(env);
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

    private String getPid(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            String nodePid = session.execute(command.PS.parse("filebeat"));
            if (StrUtil.isBlank(nodePid)) {
                return "";
            }
            List<String> pidList = StrUtil.split(nodePid, '\n');
            String currentPid = null;
            for (String pid : pidList) {
                var path = session.execute(String.format(PID_PATH, pid));
                if (path.startsWith(env.getPath())) {
                    currentPid = pid;
                    break;
                }
            }
            if (currentPid != null) {
                return currentPid;
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException(e.getMessage());
        }
        return "";
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        String pid = getPid(env);
        try (SshSession session = connect(env);
             InputStream in = loader.getResource("run_filebeat.sh").getInputStream()) {
            session.upload(in, env.getPath() + "/run_filebeat.sh");
            if (!"".equals(pid)) {
                File pidFile = File.createTempFile("filebeat", ".pid");
                FileUtil.appendUtf8String(pid, pidFile);
                session.upload(pidFile.getCanonicalPath(), env.getPath() + "/filebeat.pid");
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private AgentExceptionVO checkPidStatus(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
            String message = session.execute(String.format(CHECK_FILEBEAT, env.getPath()));
            agentExceptionVO.setAgentStatus(message.contains("Filebeat is running with PID"), message);
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    private boolean getHealthStatus(NctigbaEnvDO env) {
        String pid = getPid(env);
        return !StrUtil.isBlank(pid);
    }

    private void checkHealthStatus(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            for (int i = 0; i < 30; i++) {
                ThreadUtil.sleep(10000L);
                String testRun = session.execute("cd " + env.getPath() + " && " + FILEBEAT_HEALTH_STATUS);
                if (testRun.contains("true")) {
                    break;
                } else {
                    if (i == 29) {
                        env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
                        envMapper.updateById(env);
                        throw new CustomException("filebeat is not healthy:" + testRun + FILEBEAT_HEALTH_STATUS);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
            envMapper.updateById(env);
            throw new CustomException(e.getMessage());
        }
        env.setEnvStatus(AgentStatusEnum.NORMAL.getStatus());
        envMapper.updateById(env);
    }

    private AgentExceptionVO checkRunningEnvironment(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSession session = connect(env)) {
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
            session.executeNoWait(String.format(START_FILEBEAT, env.getPath()));
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
            session.execute(String.format(STOP_FILEBEAT, env.getPath()));
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
            throw new CustomException("filebeat not found");
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
}