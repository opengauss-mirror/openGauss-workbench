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
import cn.hutool.json.JSONUtil;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private static final long MONITOR_CYCLE = 60L;

    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ClusterManager clusterManager;

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
        // @formatter:on
        int curr = 0;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        // step1
        try {
            var node = clusterManager.getOpsNodeById(nodeId);
            if (node == null) {
                throw new CustomException("node not found");
            }
            var hostId = node.getHostId();
            var env = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getHostid, hostId)
                    .eq(NctigbaEnvDO::getType, InstallType.FILEBEAT).eq(NctigbaEnvDO::getNodeid, nodeId));
            if (env != null) {
                throw new CustomException("filebeat exists");
            }
            env = new NctigbaEnvDO().setHostid(hostId).setType(InstallType.FILEBEAT).setNodeid(nodeId).setPath(path);
            var esEnv = envMapper
                    .selectOne(
                            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.ELASTICSEARCH));
            if (esEnv == null) {
                throw new CustomException("elasticsearch not exist");
            }
            if (StrUtil.isBlank(esEnv.getUsername())) {
                throw new CustomException("elasticsearch installing");
            }
            var opsClusterNodeEntity = opsClusterNodeService.getById(nodeId);
            if (opsClusterNodeEntity == null) {
                throw new CustomException("No cluster node information found");
            }
            String installUserId = opsClusterNodeEntity.getInstallUserId();
            OpsHostUserEntity user = hostUserFacade.getById(installUserId);
            env.setUsername(user.getUsername());
            OpsHostEntity hostEntity = hostFacade.getById(hostId);
            if (hostEntity == null) {
                throw new CustomException("host not found");
            }
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                    user.getUsername(), encryptionUtils.decrypt(user.getPassword()))) {
                String installPath = session.execute("[ -e " + path + " ] && echo 'true' || echo 'false'");
                if (installPath.contains("false")) {
                    session.execute("mkdir -p " + path);
                } else {
                    String fileIsEmpty = session.execute("[ ! -z " + path + " ] && echo 'true' || echo 'false'");
                    if (fileIsEmpty.contains("false")) {
                        throw new CustomException("The installation folder is not empty!");
                    }
                }
            }
            curr = nextStep(wsSession, steps, curr);
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
                    String fileIsExists = session.execute(
                            "[ -f " + pkg.getPath() + " ] && echo 'true' || echo 'false'");
                    if (fileIsExists.contains("false")) {
                        isDownload = true;
                    }
                }
                if (isDownload) {
                    var f = Download.download(PATH + tar, "pkg/" + tar);
                    pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType(InstallType.FILEBEAT_PKG);
                    addMsg(wsSession, steps, curr, "filebeat.install.download.success");
                    save(pkg);
                }
                curr = nextStep(wsSession, steps, curr);
                // step3
                session.upload(pkg.getPath(), path + tar);
                session.execute("cd " + path + " && " + command.TAR.parse(tar));
                curr = nextStep(wsSession, steps, curr);
                // step4
                // run shell
                try (InputStream in = loader.getResource("run_filebeat.sh").getInputStream()) {
                    session.upload(in, path + name + "/run_filebeat.sh");
                }
                // filebeat_conf
                File f = File.createTempFile("filebeat_conf", ".tar.gz");
                try (var in = loader.getResource("filebeat_conf.tar.gz").getInputStream();
                     var out = new FileOutputStream(f)) {
                    IoUtil.copy(in, out);
                }
                session.upload(f.getCanonicalPath(), path + "filebeat_conf.tar.gz");
                Files.delete(f.toPath());
                session.execute("cd " + path + " && tar -xvf filebeat_conf.tar.gz");
                var esHost = hostFacade.getById(esEnv.getHostid());
                var cluster = clusterManager.getOpsClusterByNodeId(nodeId);
                // @formatter:off
                session.execute("cd " + path + "filebeat_conf && sh conf.sh"
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
                session.execute("cp -fr " + path + "filebeat_conf/* " + path + name + "/");
                session.execute("rm -rf " + path + "filebeat_conf");
                session.execute("rm -f " + path + "filebeat_conf.tar.gz");

            }
            envMapper.insert(env);
            curr = nextStep(wsSession, steps, curr);
            // step5
            startFilebeat(env);
            curr = nextStep(wsSession, steps, curr);
            // step6
            checkHealthStatus(env);
            curr = nextStep(wsSession, steps, curr);
            // step7
            sendMsg(wsSession, steps, curr, status.DONE);
        } catch (Exception e) {
            log.error("", e);
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
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
        // @formatter:on
        var curr = 0;

        try {
            curr = nextStep(wsSession, steps, curr);
            var node = clusterManager.getOpsNodeById(nodeId);
            var env = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, InstallType.FILEBEAT)
                            .eq(NctigbaEnvDO::getNodeid, node.getNodeId()));
            if (env == null) {
                throw new CustomException("filebeat not found");
            }
            curr = nextStep(wsSession, steps, curr);
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (hostEntity == null) {
                throw new CustomException("host not found");
            }
            env.setHost(hostEntity);
            uninstallFilebeat(env);
            curr = nextStep(wsSession, steps, curr);
            clearInstallFolder(env);
            envMapper.deleteById(env);
            sendMsg(wsSession, steps, curr, status.DONE);
        } catch (Exception e) {
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
    }

    private void clearInstallFolder(NctigbaEnvDO env) {
        try (SshSession session = connect(env)) {
            String pkgName = session.execute("find " + env.getPath());
            if (env.getPath() != null && !pkgName.contains("No such file or directory")) {
                String cd = "cd " + env.getPath() + " && ";
                session.execute(cd + "rm -rf " + env.getPath());
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
            boolean isStop = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status);
            boolean isRunning =
                    AgentStatusEnum.STARTING.getStatus().equals(status) || AgentStatusEnum.STOPPING.getStatus().equals(
                            status);
            boolean isRunTimeout = isRunning
                    && new Date().getTime() - f.getUpdateTime().getTime() > 3 * MONITOR_CYCLE * 1000L;
            boolean isTimeout = new Date().getTime() - f.getUpdateTime().getTime() > MONITOR_CYCLE * 1000L;
            if (StrUtil.isBlank(status) || (!isStop && !isRunTimeout && isTimeout)) {
                f.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
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
            boolean isTimeout = new Date().getTime() - env.getUpdateTime().getTime() > 3 * MONITOR_CYCLE * 1000L;
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
                continue;
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
                var path = session.execute("ls -l /proc/" + pid + "|grep cwd|awk '{print $11}'");
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
            String cd = "cd " + env.getPath() + " && ";
            String message = session.execute(cd + " sh run_filebeat.sh status");
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
            for (int i = 0; i < 10; i++) {
                ThreadUtil.sleep(3000L);
                String testRun = session.execute("cd " + env.getPath()
                        + " && grep -qE \"Connection to backoff" + File.separator + "(elasticsearch" + File.separator
                        + "(http://[0-9]+" + File.separator + ".[0-9]+" + File.separator + ".[0-9]+" + File.separator
                        + ".[0-9]+:[0-9]+" + File.separator + ")" + File.separator + ") established\" filebeat.log "
                        + "&& echo \"true\" || echo \"false\" ");
                if (testRun.contains("true")) {
                    break;
                } else {
                    if (i == 9) {
                        env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
                        envMapper.updateById(env);
                        throw new CustomException("filebeat is not healthy:" + testRun);
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
            String pkgPath = session.execute("[ -e " + env.getPath() + " ] && echo 'true' || echo 'false'");
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
            String cd = "cd " + env.getPath() + " && ";
            session.executeNoWait(cd + " sh run_filebeat.sh start");
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
            String cd = "cd " + env.getPath() + " && ";
            session.execute(cd + "sh run_filebeat.sh stop");
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