/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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

package org.opengauss.agent.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcraft.jsch.Session;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.AgentConstants;
import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.core.domain.model.agent.AgentStartResult;
import org.opengauss.admin.common.core.domain.model.agent.HeartbeatReport;
import org.opengauss.admin.common.enums.agent.AgentStatus;
import org.opengauss.admin.common.exception.ops.AgentConnectionException;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.CommonUtils;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.common.utils.YamlUtils;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.system.mapper.agent.AgentInstallMapper;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.ISysSettingExtService;
import org.opengauss.admin.system.service.JschExecutorService;
import org.opengauss.agent.service.AgentHttpProxy;
import org.opengauss.agent.service.IAgentHeartbeatService;
import org.opengauss.agent.service.IAgentInstallService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * IAgentInstallService
 *
 * @author: wangchao
 * @Date: 2025/4/10 09:30
 * @Description: AgentInstallServiceImpl
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentInstallServiceImpl extends ServiceImpl<AgentInstallMapper, AgentInstallEntity>
    implements IAgentInstallService {
    private static final String AGENT_CONFIG_FILE_NAME = "application.yml";
    private static final int AGENT_STATUS_CHECK_MAX_RETRIES = 6;

    private final Map<String, String> serverIpAndMask = new HashMap<>();
    @Value("${server.port}")
    private String serverPort;
    @Value("${agent.localPath}")
    private String agentLocalPath;
    @Value("${agent.agentName}")
    private String agentName;
    @Resource
    private JschExecutorService jschExecutorService;
    @Resource
    private AgentSshLoginService agentSshLoginService;
    @Resource
    private IAgentHeartbeatService agentHeartbeatService;
    @Resource
    private ISysSettingExtService sysSettingExtService;
    @Resource
    private AgentHttpProxy agentHttpProxy;

    /**
     * init ServerIp and Mask
     */
    @PostConstruct
    public void initServerIpList() {
        serverIpAndMask.putAll(IpUtils.getLocalIpAddressMap());
    }

    @Override
    public void installAgent(AgentInstallEntity agentInstall) {
        String agentId = agentInstall.getAgentId();
        log.info("start install Agent[{}]", agentId);
        CommonUtils.pathValidate(agentInstall.getInstallPath());
        SshLogin sshLogin = agentSshLoginService.getSshLogin(agentInstall);
        jschExecutorService.executeWithSession(sshLogin, (session -> {
            agentInstall.setAgentIp(sshLogin.getHost());
            checkJavaVersion(session);
            log.info("check  agent {} java version success", agentId);
            checkAndPrepareAgentDir(session, agentInstall.getInstallPath());
            log.info("check agent {} install path success", agentId);
            checkAgentPort(session, agentInstall.getAgentPort());
            log.info("check agent {} port success", agentId);
            installAndConfigHostAgent(session, agentInstall);
            log.info("config install agent {} success", agentId);
            agentInstall.setCreateTime(Instant.now());
        }));
        save(agentInstall);
        log.info("save agent install info {}", agentId);
        startAgent(agentId);
        log.info("start agent {} and agent install finished", agentId);
    }

    private void installAndConfigHostAgent(Session session, AgentInstallEntity install) {
        try {
            scpAgent(session, getAgentLocalFilePath(), install.getInstallPath());
            Map<String, Object> properties = loadAndConfigAgentProperties(install);
            refreshRemoteHostAgentConfig(session, properties, install.getInstallPath());
        } catch (OpsException e) {
            cleanAgentInstall(session, install);
            throw new OpsException("Failed to install agent: " + e.getMessage());
        }
    }

    private void cleanAgentInstall(Session session, AgentInstallEntity install) {
        String deleteCmd = AgentCmdBuilder.buildDeleteAgentDirCommand(install.getInstallPath());
        jschExecutorService.execCommand(session, deleteCmd);
    }

    private String getAgentLocalFilePath() {
        return new File(agentLocalPath, agentName).getPath();
    }

    private void refreshRemoteHostAgentConfig(Session session, Map<String, Object> properties, String installPath) {
        String content = YamlUtils.dumpYaml(properties);
        jschExecutorService.remoteWrite(session, content, installPath + "/" + AGENT_CONFIG_FILE_NAME);
    }

    private Map<String, Object> loadAndConfigAgentProperties(AgentInstallEntity install) {
        Map<String, Object> properties = loadAgentDefaultYml();
        configAgentProperties(properties, install);
        return properties;
    }

    private void configAgentProperties(Map<String, Object> properties, AgentInstallEntity install) {
        String serverHost = sysSettingExtService.queryAdminServerHost();
        ConfigAgentProperties configAgentProperties = new ConfigAgentProperties(serverHost, serverIpAndMask,
            serverPort);
        configAgentProperties.configAgentProperties(properties, install);
    }

    private Map<String, Object> loadAgentDefaultYml() {
        File propertiesFile = new File(agentLocalPath, AGENT_CONFIG_FILE_NAME);
        if (!propertiesFile.exists()) {
            throw new OpsException("agent.properties file not found");
        }
        return YamlUtils.readYaml(propertiesFile.getPath());
    }

    private void scpAgent(Session session, String localJarPath, String installPath) {
        if (!Files.exists(Path.of(localJarPath))) {
            throw new OpsException("agent jar file not found");
        }
        jschExecutorService.scp(session, localJarPath, installPath);
    }

    private void checkAgentPort(Session session, Integer port) {
        OpsAssert.isTrue(jschExecutorService.checkOsPortConflict(session, port), "agent port " + port + " is in used");
    }

    private void checkAndPrepareAgentDir(Session session, String targetDir) {
        String checkExistCmd = AgentCmdBuilder.buildCheckDirExistCommand(targetDir);
        String existFlag = jschExecutorService.execCommand(session, checkExistCmd).trim();
        if ("not_exist".equals(existFlag)) {
            String createCmd = AgentCmdBuilder.buildCreateDirCommand(targetDir);
            String exitCode = jschExecutorService.execCommand(session, createCmd);
            log.info("{}:{} create dir result: [{}] ", session.getHost(), session.getUserName(), exitCode);
        }
        String checkEmptyCmd = AgentCmdBuilder.buildCheckDirEmptyCommand(targetDir);
        String emptyFlag = jschExecutorService.execCommand(session, checkEmptyCmd).trim();
        if ("not_empty".equals(emptyFlag)) {
            throw new OpsException("dir is not empty: " + targetDir);
        }
        String permissionCmd = AgentCmdBuilder.buildCheckDirWritableCommand(targetDir);
        String permissionFlag = jschExecutorService.execCommand(session, permissionCmd).trim();
        if (!"writable".equals(permissionFlag)) {
            throw new OpsException("dir does not permission writable: " + targetDir);
        }
    }

    private void checkJavaVersion(Session session) {
        String javaVersionCmd = AgentCmdBuilder.buildCheckJavaVersionCommand();
        String versionOutput = jschExecutorService.execCommand(session, javaVersionCmd);
        if (versionOutput == null || versionOutput.isEmpty()) {
            throw new OpsException("Java environment not found");
        }
        int majorVersion = CommonUtils.getJavaVersionMajor(versionOutput);
        if (majorVersion < 17) {
            throw new OpsException("Java version required 17+ , current version is " + versionOutput);
        }
    }

    @Override
    public void uninstallAgent(String agentId) {
        AgentInstallEntity agentInstall = getByAgentId(agentId);
        OpsAssert.nonNull(agentInstall, "uninstall agent failed, agentId:" + agentId + " must be available");
        String stopCmd = AgentCmdBuilder.buildStopAgentCommand(agentInstall.getInstallPath(), agentName);
        SshLogin sshLogin = agentSshLoginService.getSshLogin(agentInstall);
        String execResult = jschExecutorService.execCommand(sshLogin, stopCmd);
        log.info("stop agent result: {}", execResult);
        String deleteCmd = AgentCmdBuilder.buildDeleteAgentDirCommand(agentInstall.getInstallPath());
        execResult = jschExecutorService.execCommand(sshLogin, deleteCmd);
        log.info("delete agent install dir result: {}", execResult);
        removeByAgentId(agentId);
        agentHeartbeatService.deregister(
            new HeartbeatReport(agentId, Instant.now(), AgentConstants.Status.HEARTBEAT_STATUS_DOWN));
        log.info("uninstall agent success, agentId: {}", agentId);
    }

    @Override
    public void removeByAgentId(String agentId) {
        remove(Wrappers.lambdaQuery(AgentInstallEntity.class).eq(AgentInstallEntity::getAgentId, agentId));
    }

    @Override
    public AgentStartResult startAgent(String agentId) {
        AgentInstallEntity agentInstall = getByAgentId(agentId);
        OpsAssert.nonNull(agentInstall, "start agent failed, agentId:" + agentId + " must be available");
        return startAgent(agentInstall);
    }

    @Override
    public void updateAgentPort(String agentId, Integer port) {
        AgentInstallEntity agent = getByAgentId(agentId);
        OpsAssert.nonNull(agent, "update agent port failed, agentId:" + agentId + " must be available");
        agent.setAgentPort(port);
        jschExecutorService.executeWithSession(agentSshLoginService.getSshLogin(agent), (session) -> {
            Map<String, Object> properties = loadAndConfigAgentProperties(agent);
            refreshRemoteHostAgentConfig(session, properties, agent.getInstallPath());
            log.info("agent is updated, agentId: {}", agentId);
        });
        update(agent, Wrappers.lambdaUpdate(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getAgentId, agentId)
            .set(AgentInstallEntity::getAgentPort, port)
            .set(AgentInstallEntity::getUpdateTime, Instant.now()));
        restartAgent(agent);
    }

    /**
     * Start the Agent process via remote SSH command . After executing the start command, it is necessary to check
     * whether the Agent process has been successfully started. Verifies successful Agent process startup through
     * cyclic checks.
     *
     * <p>Loop detection (maximum detection attempts: {@link #AGENT_STATUS_CHECK_MAX_RETRIES}).</p>
     * <p>Each detection cycle process:</p>
     * <ol>
     *   <li>Check if the Agent process exists.</li>
     *   <li>If the process exists, check if the Agent health interface is started normally</li>
     *   <li>If the health interface is accessible normally, the Agent has been successfully started. if the agent has
     *    started,exit the loop detection. </li>
     *   <li>Otherwise, the startup failed, and continue to the next cycle</li>
     * </ol>
     *  If all cycles fail to execute, confirm that the Agent startup is abnormal
     *
     * @param agentInstall agent install info
     * @return AgentStartResult agent start result
     * @see AgentCmdBuilder#buildStartAgentCommand
     */
    public AgentStartResult startAgent(AgentInstallEntity agentInstall) {
        String startAgentCmd = AgentCmdBuilder.buildStartAgentCommand(agentInstall.getInstallPath(), agentName);
        jschExecutorService.execCommand(agentSshLoginService.getSshLogin(agentInstall), startAgentCmd);
        log.info("start agent [{}:{}] [{}]", agentInstall.getAgentIp(), agentInstall.getInstallUser(), startAgentCmd);
        refreshAgentStatus(agentInstall.getAgentId(), AgentStatus.STARTING);
        if (isAgentProcessAlive(agentInstall)) {
            return new AgentStartResult(agentInstall.getAgentId(), true, "start success");
        } else {
            refreshAgentStatus(agentInstall.getAgentId(), AgentStatus.STOP);
            return new AgentStartResult(agentInstall.getAgentId(), false,
                "agent start failed,please check the agent start log.");
        }
    }

    private boolean isAgentProcessAlive(AgentInstallEntity agentInstall) {
        String checkStatusCmd = AgentCmdBuilder.buildCheckAgentRunningCommand(agentInstall.getInstallPath(), agentName);
        String checkResult;
        boolean isAgentProcessAlive = false;
        for (int i = 0; i < AGENT_STATUS_CHECK_MAX_RETRIES; i++) {
            checkResult = jschExecutorService.execCommand(agentSshLoginService.getSshLogin(agentInstall),
                checkStatusCmd);
            log.info("agent [{}:{}] process pid=[{}]", agentInstall.getAgentIp(), agentInstall.getInstallUser(),
                checkResult);
            if (StrUtil.isNotEmpty(checkResult)) {
                try {
                    String healthStatus = agentHttpProxy.checkAgentHealth(agentInstall);
                    isAgentProcessAlive = StrUtil.equalsIgnoreCase(healthStatus, "success");
                    break;
                } catch (AgentConnectionException ex) {
                    log.debug("agent [{}:{}] health error=[{}]", agentInstall.getAgentIp(),
                        agentInstall.getInstallUser(), ex.getMessage());
                }
            }
            ThreadUtil.sleep(1000);
        }
        log.warn("Agent process and health alive=[{}] agent: [{}:{}] cmd=[{}]", isAgentProcessAlive,
            agentInstall.getAgentIp(), agentInstall.getInstallUser(), checkStatusCmd);
        return isAgentProcessAlive;
    }

    @Override
    public List<AgentInstallEntity> listByUserId(String userId) {
        return list(Wrappers.lambdaQuery(AgentInstallEntity.class).eq(AgentInstallEntity::getInstallUserId, userId));
    }

    @Override
    public AgentInstallEntity getByAgentId(String agentId) {
        return getOne(Wrappers.lambdaQuery(AgentInstallEntity.class).eq(AgentInstallEntity::getAgentId, agentId));
    }

    @Override
    public void stopAgent(String agentId) {
        AgentInstallEntity agent = getByAgentId(agentId);
        OpsAssert.nonNull(agent, "stop agent failed, agentId:" + agentId + " must be available");
        stopAgent(agent);
    }

    private void stopAgent(AgentInstallEntity agent) {
        String stopCmd = AgentCmdBuilder.buildStopAgentCommand(agent.getInstallPath(), agentName);
        String execResult = jschExecutorService.execCommand(agentSshLoginService.getSshLogin(agent), stopCmd);
        log.info("stop agent {} {} result: {} {}", agent.getAgentIp(), agent.getInstallUser(), stopCmd, execResult);
        update(Wrappers.lambdaUpdate(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getAgentId, agent.getAgentId())
            .set(AgentInstallEntity::getStatus, AgentStatus.STOP));
        log.info("stop agent success, agentId: {}", agent.getAgentId());
    }

    private boolean restartAgent(AgentInstallEntity agent) {
        stopAgent(agent);
        AgentStartResult agentStartStatus = startAgent(agent);
        return agentStartStatus.isSuccess();
    }

    @Override
    public void refreshAgentStatus(String agentId, AgentStatus status) {
        update(Wrappers.lambdaUpdate(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getAgentId, agentId)
            .set(AgentInstallEntity::getStatus, status)
            .set(AgentInstallEntity::getUpdateTime, Instant.now()));
    }

    @Override
    public void updateAgent(String agentId) {
        AgentInstallEntity agent = getByAgentId(agentId);
        OpsAssert.nonNull(agent, "update agent failed, agentId:" + agentId + " must be available");
        String localHash = getSha256SumOfLocalAgentFile(getAgentLocalFilePath());
        String command = AgentCmdBuilder.buildSha256CheckCommand(agent.getInstallPath(), agentName);
        SshLogin sshLogin = agentSshLoginService.getSshLogin(agent);
        String remoteHash = jschExecutorService.execCommand(sshLogin, command);
        if (localHash.equalsIgnoreCase(remoteHash)) {
            log.info("agent is lasted, agentId: {}", agentId);
        } else {
            jschExecutorService.executeWithSession(sshLogin, (session) -> {
                scpAgent(session, getAgentLocalFilePath(), agent.getInstallPath());
                Map<String, Object> properties = loadAndConfigAgentProperties(agent);
                refreshRemoteHostAgentConfig(session, properties, agent.getInstallPath());
                log.info("agent is updated, agentId: {}", agentId);
            });
        }
        update(Wrappers.lambdaUpdate(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getAgentId, agentId)
            .set(AgentInstallEntity::getUpdateTime, Instant.now()));
    }

    @Override
    public List<AgentInstallEntity> startAllOfAgent() {
        List<AgentInstallEntity> allAgents = list();
        List<AgentInstallEntity> startAgentList = new LinkedList<>();
        log.info("start all agent, {} agent are starting", allAgents.size());
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(
            allAgents.stream().map((agent -> CompletableFuture.supplyAsync(() -> {
                if (restartAgent(agent)) {
                    startAgentList.add(agent);
                    return true;
                } else {
                    return false;
                }
            }))).toArray(CompletableFuture[]::new));
        try {
            allFuture.get();
            log.info("all agent restart success");
        } catch (InterruptedException | ExecutionException e) {
            log.error("error occurred while starting agents :", e);
        }
        return startAgentList;
    }

    @Override
    public AgentStatus getAgentStatus(String hostId) {
        return Optional.ofNullable(getById(hostId)).map(AgentInstallEntity::getStatus).orElse(AgentStatus.UNINSTALL);
    }

    @Override
    public Map<String, AgentStatus> queryAgentStatus() {
        return list().stream().collect(Collectors.toMap(AgentInstallEntity::getAgentId, AgentInstallEntity::getStatus));
    }

    /**
     * stop all of agent ,when application is stop
     */
    @PreDestroy
    public void destroyAgent() {
        list().parallelStream().forEach(this::stopAgent);
    }

    @Override
    public void updateAgentStatus(String agentId, AgentStatus status) {
        update(Wrappers.lambdaUpdate(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getAgentId, agentId)
            .set(AgentInstallEntity::getStatus, status)
            .set(AgentInstallEntity::getUpdateTime, Instant.now()));
    }

    @Override
    public Map<String, AgentInstallEntity> queryAgentInstallInfo() {
        return list().stream().collect(Collectors.toMap(AgentInstallEntity::getAgentId, Function.identity()));
    }

    @Override
    public boolean hasInstall(String hostId) {
        return !Objects.equals(AgentStatus.UNINSTALL, getAgentStatus(hostId));
    }

    @Override
    public long countByHostUserId(String hostUserId) {
        LambdaQueryWrapper<AgentInstallEntity> userInstallCounter = Wrappers.lambdaQuery(AgentInstallEntity.class)
            .eq(AgentInstallEntity::getInstallUserId, hostUserId);
        return count(userInstallCounter);
    }

    private String getSha256SumOfLocalAgentFile(String agentLocalFilePath) {
        Path sha256Path = Path.of(agentLocalFilePath + ".sha256");
        String sha256Hash = readSha256FromFile(sha256Path);
        if (sha256Hash.isEmpty()) {
            sha256Hash = calculateLocalSHA256(agentLocalFilePath);
            log.debug("Calculated SHA256 for {}: {}", agentLocalFilePath, sha256Hash);
        } else {
            log.info("Using cached SHA256 for {}: {}", agentLocalFilePath, sha256Hash);
        }
        return sha256Hash;
    }

    /**
     * Calculates the SHA256 hash of the file at the given path.
     *
     * @param sha256Path Path to the SHA256 file.
     * @return SHA256 hash of the file.
     */
    private String readSha256FromFile(Path sha256Path) {
        if (!Files.exists(sha256Path)) {
            return "";
        }
        try {
            String content = Files.readString(sha256Path, StandardCharsets.UTF_8);
            String[] parts = content.split("\\s+"); // 兼容多个空白字符分隔
            if (parts.length < 1) {
                log.warn("Invalid SHA256 file format: {}", sha256Path);
                return "";
            }
            return parts[0].trim();
        } catch (IOException e) {
            log.error("Failed to read SHA256 file {}: {}", sha256Path, e.getMessage(), e);
            return "";
        }
    }

    /**
     * calculates the SHA256 hash of the file at the given path.
     *
     * @param filePath Path to the SHA256 file.
     * @return SHA256 hash of the file.
     */
    private String calculateLocalSHA256(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new OpsException("calculateLocalSHA256 error " + e.getMessage());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static class ConfigAgentProperties {
        private final String agentId = "agent.id";
        private final String agentName = "agent.name";
        private final String agentServer = "agent.server";
        private final String agentPort = "quarkus.http.port";
        private final String agentLogPath = "quarkus.log.file.path";
        private Map<String, String> serverIpAndMaskMap;
        private String defaultServerHost;
        private String serverPort;

        /**
         * constructor
         *
         * @param serverHost serverHost
         * @param serverIpAndMask server ip and mask map
         * @param serverPort server port
         */
        public ConfigAgentProperties(String serverHost, Map<String, String> serverIpAndMask, String serverPort) {
            this.serverIpAndMaskMap = serverIpAndMask;
            this.defaultServerHost = serverHost;
            this.serverPort = serverPort;
        }

        /**
         * config agent properties
         *
         * @param properties agent properties
         * @param install agent install entity
         */
        public void configAgentProperties(Map<String, Object> properties, AgentInstallEntity install) {
            try {
                if (properties == null || install == null) {
                    throw new OpsException("properties and install cannot be null");
                }
                update(properties, agentId, install.getAgentId());
                update(properties, agentName, install.getAgentName());
                update(properties, agentServer, loadServerUrl(install.getAgentIp()));
                update(properties, agentPort, install.getAgentPort());
                Object defaultPath = YamlUtils.getProperties(properties, agentLogPath);
                update(properties, agentLogPath, buildDefaultLogPath(defaultPath.toString(), install.getInstallPath()));
            } catch (OpsException e) {
                throw new OpsException("Failed to load dataKit server url" + e.getMessage());
            }
        }

        private void update(Map<String, Object> properties, String key, Object value) throws OpsException {
            YamlUtils.updateProperties(properties, key, value);
        }

        private String loadServerUrl(String agentIp) {
            if (StrUtil.isNotEmpty(defaultServerHost)) {
                return "http://" + defaultServerHost + ":" + this.serverPort;
            }
            return this.serverIpAndMaskMap.entrySet().stream().filter(entry -> {
                try {
                    return IpUtils.isIpInSubnet(agentIp, entry.getKey(), entry.getValue());
                } catch (UnknownHostException e) {
                    log.error("Failed to load server url", e);
                    return false;
                }
            }).findFirst().map(entry -> "http://" + entry.getKey() + ":" + this.serverPort).orElse("");
        }

        private Object buildDefaultLogPath(String defaultPath, String installPath) {
            if (StrUtil.isBlank(defaultPath)) {
                return Paths.get(installPath, "datakit_agent.log").toString();
            }
            return defaultPath.replace("/path", installPath);
        }
    }
}