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
 *  AgentServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/AgentServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.enums.AgentStatusEnum;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.vo.AgentExceptionVO;
import com.nctigba.observability.sql.model.vo.AgentStatusVO;
import com.nctigba.observability.sql.service.AgentService;
import com.nctigba.observability.sql.service.impl.AbstractInstaller.Step.status;
import com.nctigba.observability.sql.util.DownloadUtils;
import com.nctigba.observability.sql.util.EbpfUtils;
import com.nctigba.observability.sql.util.SshSessionUtils;
import com.nctigba.observability.sql.util.SshSessionUtils.Command;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AgentServiceImpl
 *
 * @author luomeng
 * @since 2024/1/5
 */
@Slf4j
@Service
public class AgentServiceImpl extends AbstractInstaller implements AgentService {
    private static final String AGENT_USER = "root";
    private static final String NAME = "opengauss-ebpf.jar";
    private static final String JDK = "https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/";
    private static final String JDKPKG = "bisheng-jdk-8u392-linux-{0}.tar.gz";
    private static final long MONITOR_CYCLE = 60L;

    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private EbpfUtils utils;

    /**
     * Install agent
     *
     * @param wsSession    Websocket
     * @param nodeId       Install node
     * @param path         Install path
     * @param callbackPath Callback path
     * @param rootPassword Root password
     * @param port         Install port
     */
    public void install(WsSession wsSession, String nodeId, int port, String rootPassword, String path,
            String callbackPath) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("agent.install.step1"),
                new Step("agent.install.step2"),
                new Step("agent.install.step3"),
                new Step("agent.install.step4"),
                new Step("agent.install.step5"),
                new Step("agent.install.step6"),
                new Step("agent.install.step7"));
        // @formatter:on
        var curr = 0;
        // step1
        try {
            if (StrUtil.isBlank(callbackPath)) {
                throw new CustomException("callback host null");
            }
            var node = clusterManager.getOpsNodeById(nodeId);
            var hostId = node.getHostId();
            var env = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.AGENT)
                            .eq(NctigbaEnvDO::getNodeid, nodeId));
            if (env != null) {
                throw new CustomException("agent exists");
            }
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            env = new NctigbaEnvDO().setHostid(hostId).setNodeid(nodeId).setPort(port).setUsername(AGENT_USER)
                    .setType(NctigbaEnvDO.envType.AGENT);
            env.setParam(callbackPath);
            env.setPath(path);
            try (var session = connect(env, rootPassword)) {
                String message = session.execute(
                        "netstat -tuln | grep -q " + env.getPort() + " && echo \"true\" || echo \"false\"");
                if (message.contains("true")) {
                    throw new CustomException("port is exists");
                }
                String installPath = session.execute("[ -e " + path + " ] && echo 'true' || echo 'false'");
                if (installPath.contains("false")) {
                    session.execute("mkdir -p " + path);
                } else {
                    String fileIsEmpty = session.execute("[ ! -z " + path + " ] && echo 'true' || echo 'false'");
                    if (fileIsEmpty.contains("false")) {
                        throw new CustomException("The installation folder is not empty!");
                    }
                }
                curr = nextStep(wsSession, steps, curr);
                // step2
                if (!session.test("unzip -v")) {
                    session.execute("yum install -y unzip zip");
                }
                var python_v = session.execute("python --version");
                if (!python_v.toLowerCase().startsWith("python ")) {
                    throw new CustomException("python version err, curr:" + python_v);
                }
                addMsg(wsSession, steps, curr, python_v);
                // bcc-tool
                if (!session.test(Command.STAT.parse("/usr/share/bcc/tools"))) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            session.execute("yum -y install bcc-tools");
                            break;
                        } catch (Exception e) {
                            if (i == 2) {
                                throw new CustomException("bcc install fail");
                            }
                            addMsg(wsSession, steps, curr, "bcc install fail " + i + ", retrying");
                        }
                    }
                } else {
                    addMsg(wsSession, steps, curr, "bcc exists");
                }
                // FlameGraph
                File graph = File.createTempFile("FlameGraph", ".zip");
                try (var in = loader.getResource("FlameGraph.zip").getInputStream();
                     var out = new FileOutputStream(graph)) {
                    IoUtil.copy(in, out);
                }
                session.execute("mkdir -p /opt/software");
                session.execute("rm -rf /opt/software/FlameGraph*");
                session.upload(graph.getCanonicalPath(), "/opt/software/FlameGraph.zip");
                session.execute("cd /opt/software && unzip FlameGraph.zip && cd FlameGraph && chmod +x flamegraph.pl");
                // jdk version
                String jdkVersion = getJavaVersion(env, rootPassword);
                addMsg(wsSession, steps, curr, jdkVersion);
                curr = nextStep(wsSession, steps, curr);
                // step3
                // upload
                File f = File.createTempFile("opengauss-ebpf-1.0.0-SNAPSHOT", ".jar");
                try (var in = loader.getResource(NAME).getInputStream(); var out = new FileOutputStream(f)) {
                    IoUtil.copy(in, out);
                }
                session.upload(f.getCanonicalPath(), path + NAME);
                Files.delete(f.toPath());
                curr = nextStep(wsSession, steps, curr);
                // step4
                // run shell
                uploadShell(env, rootPassword, jdkVersion);
                env.setStatus(AgentStatusEnum.UNKNOWN.getStatus());
                env.setUpdateTime(new Date());
                envMapper.insert(env);
                curr = nextStep(wsSession, steps, curr);
                // step5
                execStartCmd(env, rootPassword);
                curr = nextStep(wsSession, steps, curr);
                // step6
                checkHealthStatus(env);
                curr = nextStep(wsSession, steps, curr);
                // step7
                sendMsg(wsSession, steps, curr, status.DONE);
            }
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

    private SshSessionUtils connect(NctigbaEnvDO env, String rootPassword) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
        if (hostEntity == null) {
            throw new CustomException("host not found");
        }
        env.setHost(hostEntity);
        var user = getUser(hostEntity, AGENT_USER, rootPassword);
        return SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(), AGENT_USER,
                encryptionUtils.decrypt(user.getPassword()));
    }

    /**
     * Uninstall agent
     *
     * @param wsSession    Websocket
     * @param nodeId       Install node
     * @param rootPassword Root password
     */
    public void uninstall(WsSession wsSession, String nodeId, String rootPassword) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("agent.uninstall.step1"),
                new Step("agent.uninstall.step2"),
                new Step("agent.uninstall.step3"),
                new Step("agent.uninstall.step4"),
                new Step("agent.uninstall.step5"));
        // @formatter:on
        var curr = 0;

        try {
            curr = nextStep(wsSession, steps, curr);
            var node = clusterManager.getOpsNodeById(nodeId);
            var env = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.AGENT)
                            .eq(NctigbaEnvDO::getNodeid, nodeId));
            if (env == null) {
                throw new CustomException("agent not found");
            }
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (hostEntity == null) {
                throw new CustomException("host not found");
            }
            env.setHost(hostEntity);
            curr = nextStep(wsSession, steps, curr);
            execStopCmd(env, rootPassword);
            curr = nextStep(wsSession, steps, curr);
            clearInstallFolder(env, rootPassword);
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

    private String getJavaVersion(NctigbaEnvDO env, String rootPwd) {
        String java = "java";
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String javaVersion = session.execute(
                    "source /etc/profile && java -version 2>&1 | awk -F '\"' '/version/ {print $2}'");
            log.info("jdk version is:" + javaVersion);
            if (!StrUtil.isBlank(javaVersion)) {
                String[] parts = javaVersion.split("\\.");
                int majorVersion = Integer.parseInt(parts[0]);
                int minorVersion = Integer.parseInt(parts[1]);
                if (majorVersion > 1 || (majorVersion == 1 && minorVersion > 8)) {
                    return java;
                }
            }
            String jdkIsExists = session.execute(
                    "[ -f /etc/jdk8/bin/java ] && echo 'true' || echo 'false'");
            if (jdkIsExists.contains("true")) {
                return "/etc/jdk8/bin/java";
            }
            String arch = session.execute(Command.ARCH);
            String v = "aarch64".equals(arch) ? "aarch64" : "x64";
            String tar = MessageFormat.format(JDKPKG, v);
            NctigbaEnvDO pkg = envMapper.selectOne(
                    Wrappers.<NctigbaEnvDO>lambdaQuery().like(NctigbaEnvDO::getPath, tar));
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
                File f = DownloadUtils.download(JDK + tar, "pkg/" + tar);
                pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType(NctigbaEnvDO.envType.AGENT_PKG);
                save(pkg);
            }
            session.upload(pkg.getPath(), tar);
            session.execute("tar zxvf " + tar + " -C /etc/");
            session.execute("mv /etc/bisheng-jdk1.8.0_392 /etc/jdk8");
            session.execute("rm -rf " + tar);
            java = "/etc/jdk8/bin/java";
        } catch (IOException e) {
            throw new CustomException("exec failed:" + e.getMessage());
        }
        return java;
    }

    private void clearInstallFolder(NctigbaEnvDO env, String rootPwd) {
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String pkgPath = session.execute("[ -e " + env.getPath() + " ] && echo 'true' || echo 'false'");
            if (env.getPath() != null && !pkgPath.contains("false")) {
                String cd = "cd " + env.getPath() + " && ";
                session.execute(cd + "rm -rf " + env.getPath());
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void start(String id, String rootPwd) {
        NctigbaEnvDO env = getAgentInfo(id);
        startAgent(env, rootPwd);
        checkHealthStatus(env);
    }

    private void startAgent(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO check = checkPidStatus(env, rootPwd);
        if (check.isStatus()) {
            throw new CustomException(check.getExceptionInfo());
        } else {
            if (check.getExceptionInfo().contains("root password error")) {
                throw new CustomException(check.getExceptionInfo());
            }
        }
        check = checkRunningEnvironment(env, rootPwd);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        env.setEnvStatus(AgentStatusEnum.STARTING.getStatus());
        envMapper.updateById(env);
        check = execStartCmd(env, rootPwd);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
    }

    @Override
    public void stop(String id, String rootPwd) {
        NctigbaEnvDO env = getAgentInfo(id);
        stopAgent(env, rootPwd);
    }

    private void stopAgent(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO check = checkPidStatus(env, rootPwd);
        if (!check.isStatus()) {
            if (check.getExceptionInfo().contains("No such file or directory")) {
                killPid(env, rootPwd);
                return;
            } else if (check.getExceptionInfo().contains("root password error")) {
                throw new CustomException(check.getExceptionInfo());
            } else {
                env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
                envMapper.updateById(env);
                throw new CustomException(check.getExceptionInfo());
            }
        }
        env.setEnvStatus(AgentStatusEnum.STOPPING.getStatus());
        envMapper.updateById(env);
        check = execStopCmd(env, rootPwd);
        if (!check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        check = checkPidStatus(env, rootPwd);
        if (check.isStatus()) {
            env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
            envMapper.updateById(env);
            throw new CustomException(check.getExceptionInfo());
        }
        env.setEnvStatus(AgentStatusEnum.MANUAL_STOP.getStatus());
        envMapper.updateById(env);
    }

    private void killPid(NctigbaEnvDO env, String rootPwd) {
        try (SshSessionUtils session = connect(env, rootPwd)) {
            var pid = session.execute(Command.PS.parse("elasticsearch"));
            if (StrUtil.isNotBlank(pid) && NumberUtil.isLong(pid)) {
                session.execute(Command.KILL.parse(pid));
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException("exec failed:" + e.getMessage());
        }
    }

    @Override
    public List<AgentStatusVO> getAgentStatus() {
        List<AgentStatusVO> list = envMapper.selectStatusByType(NctigbaEnvDO.envType.AGENT.name());
        list.forEach(f -> {
            String status = f.getStatus();
            boolean isStop = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status);
            boolean isRunning =
                    AgentStatusEnum.STARTING.getStatus().equals(status) || AgentStatusEnum.STOPPING.getStatus().equals(
                            status);
            boolean isRunTimeout = isRunning
                    && new Date().getTime() - f.getUpdateTime().getTime() > 3 * MONITOR_CYCLE * 1000L;
            boolean isTimeout = new Date().getTime() - f.getUpdateTime().getTime() > MONITOR_CYCLE * 1000L;
            if (!isStop && !isRunTimeout && isTimeout) {
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
                .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.AGENT));
        envList.forEach(e -> e.setHost(hostFacade.getById(e.getHostid())));
        for (NctigbaEnvDO env : envList) {
            String status = env.getStatus();
            boolean isTimeout = new Date().getTime() - env.getUpdateTime().getTime() > 3 * MONITOR_CYCLE * 1000L;
            boolean isDuring =
                    (AgentStatusEnum.STARTING.getStatus().equals(status) || AgentStatusEnum.STOPPING.getStatus().equals(
                            status)) && !isTimeout;
            boolean isSkip = AgentStatusEnum.MANUAL_STOP.getStatus().equals(status) || isDuring;
            if (isSkip) {
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

    private void uploadShell(NctigbaEnvDO env, String rootPwd, String jdkVersion) {
        try (SshSessionUtils session = connect(env, rootPwd);
             InputStream originalIn = loader.getResource("run_agent.sh").getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(originalIn, StandardCharsets.UTF_8));
            StringBuilder fileContent = new StringBuilder();
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append(System.getProperty("line.separator"));
                if (lineNumber == 5) {
                    fileContent.append("JAVA_VERSION=\"").append(jdkVersion).append(
                                    "\"").append(System.getProperty("line.separator"))
                            .append("JAR_NAME=\"").append(NAME).append("\"").append(
                                    System.getProperty("line.separator"))
                            .append("CALLBACK_URL=\"").append(env.getParam()).append("\"").append(
                                    System.getProperty("line.separator"))
                            .append("PORT=").append(env.getPort()).append(System.getProperty("line.separator"));
                }
                lineNumber++;
            }
            ByteArrayInputStream in = new ByteArrayInputStream(fileContent.toString().getBytes(StandardCharsets.UTF_8));
            session.upload(in, env.getPath() + "run_agent.sh");
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private AgentExceptionVO checkPidStatus(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String cd = "cd " + env.getPath() + " && ";
            String message = session.execute(cd + " sh run_agent.sh status");
            agentExceptionVO.setAgentStatus(message.contains("Agent is running with PID"), message);
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    private boolean getHealthStatus(NctigbaEnvDO env) {
        for (int i = 0; i < 10; i++) {
            ThreadUtil.sleep(1000L);
            try {
                if (utils.getStatus(env)) {
                    break;
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (i == 9) {
                return false;
            }
        }
        return true;
    }

    private void checkHealthStatus(NctigbaEnvDO env) {
        for (int i = 0; i < 10; i++) {
            ThreadUtil.sleep(3000L);
            String exceptionInfo = "";
            try {
                if (utils.getStatus(env)) {
                    break;
                }
            } catch (Exception e) {
                exceptionInfo = e.getMessage();
            }
            if (i == 9) {
                env.setEnvStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus());
                envMapper.updateById(env);
                throw new CustomException("agent is not healthy:" + exceptionInfo);
            }
        }
        env.setEnvStatus(AgentStatusEnum.NORMAL.getStatus());
        envMapper.updateById(env);
    }

    private AgentExceptionVO checkRunningEnvironment(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String message = session.execute(
                    "netstat -tuln | grep -q " + env.getPort() + " && echo \"true\" || echo \"false\"");
            if (message.contains("true")) {
                return agentExceptionVO.setAgentStatus(false, "port is exists!");
            }
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

    private AgentExceptionVO execStartCmd(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String cd = "cd " + env.getPath() + " && ";
            session.executeNoWait(cd + " sh run_agent.sh start");
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
            return agentExceptionVO;
        }
        agentExceptionVO.setStatus(true);
        return agentExceptionVO;
    }

    private AgentExceptionVO execStopCmd(NctigbaEnvDO env, String rootPwd) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(env, rootPwd)) {
            String cd = "cd " + env.getPath() + " && ";
            session.execute(cd + "sh run_agent.sh stop");
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
            throw new CustomException("agent not found");
        }
        env.setHost(hostFacade.getById(env.getHostid()));
        return env;
    }
}