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
 *  ExporterInstallService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/ExporterInstallService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.enums.AgentStatusEnum;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO.envType;
import com.nctigba.observability.instance.exception.TipsException;
import com.nctigba.observability.instance.model.vo.AgentExceptionVO;
import com.nctigba.observability.instance.model.vo.AgentStatusVO;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.Status;
import com.nctigba.observability.instance.util.CommonUtils;
import com.nctigba.observability.instance.util.DownloadUtils;
import com.nctigba.observability.instance.util.SshSessionUtils;
import com.nctigba.observability.instance.util.SshSessionUtils.command;
import com.nctigba.observability.instance.util.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExporterInstallService extends AbstractInstaller {
    private static final String EXPORTER_NAME = "instance-exporter.jar";
    private static final String JDK = "https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/";
    private static final String JDKPKG = "bisheng-jdk-8u392-linux-{0}.tar.gz";

    @Autowired
    ClusterManager clusterManager;
    @Autowired
    ResourceLoader loader;
    @Autowired
    AgentNodeRelationService agentNodeRelationService;
    @Autowired
    AgentService agentService;
    @Autowired
    PrometheusService prometheusService;

    /**
     * install
     *
     * @param wsSession WsSession
     * @param exporterInstallDTO ExporterInstallDTO
     */
    public void install(WsSession wsSession, ExporterInstallDTO exporterInstallDTO) {
        List<Step> steps = Arrays.asList(new Step("exporterinstall.step1"), new Step("exporterinstall.step2"),
                new Step("exporterinstall.step3"), new Step("exporterinstall.step4"),
                new Step("exporterinstall.step5"), new Step("exporterinstall.step6"),
                new Step("exporterinstall.step7"));
        initWsSessionStepTl(wsSession, steps);
        install(exporterInstallDTO);
        if (wsSessionStepTl.get() !=  null) {
            wsSessionStepTl.remove();
        }
    }

    /**
     * install
     *
     * @param exporterInstallDTO ExporterInstallDTO
     */
    public AjaxResult install(ExporterInstallDTO exporterInstallDTO) {
        nextStep();
        String path = exporterInstallDTO.getPath();
        if (StrUtil.isNotBlank(path) && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
            exporterInstallDTO.setPath(path);
        }
        NctigbaEnvDO expEnv = null;
        try {
            // check nodeId,check node db install user
            // check prometheus
            List<NctigbaEnvDO> promEnvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS));
            if (CollectionUtil.isEmpty(promEnvList)) {
                throw new CustomException("prometheus not exists");
            }

            nextStep();
            // get host data
            OpsHostEntity hostEntity = hostFacade.getById(exporterInstallDTO.getHostId());
            if (hostEntity == null) {
                throw new CustomException(CommonConstants.HOST_NOT_FOUND);
            }
            // get user data
            OpsHostUserEntity user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream()
                .filter(e -> exporterInstallDTO.getUsername().equals(e.getUsername())).findFirst()
                .orElseThrow(() -> new CustomException("user not found"));

            try (SshSessionUtils session = SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                user.getUsername(), encryptionUtils.decrypt(user.getPassword()))) {
                // check agent port
                Integer httpPort = exporterInstallDTO.getHttpPort();
                Boolean isNewInstall = StrUtil.isBlank(exporterInstallDTO.getEnvId());
                if (isNewInstall) {
                    session.testPortCanUse(httpPort);
                }
                nextStep();
                if (isNewInstall) {
                    // check existed,get installed data (same host id and same port)
                    List<NctigbaEnvDO> expEnvs = envMapper.selectList(
                        Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getHostid, exporterInstallDTO.getHostId())
                            .eq(NctigbaEnvDO::getType, envType.EXPORTER).eq(NctigbaEnvDO::getPort, httpPort));
                    if (expEnvs.size() > 0) {
                        throw new CustomException("exporter exists which has same host and port!");
                    } else {
                        expEnv = new NctigbaEnvDO().setHostid(exporterInstallDTO.getHostId()).setPort(httpPort)
                            .setUsername(exporterInstallDTO.getUsername()).setType(envType.EXPORTER)
                            .setPath(path);
                    }
                } else {
                    expEnv = envMapper.selectById(exporterInstallDTO.getEnvId());
                    if (expEnv == null) {
                        throw new TipsException("envIdError");
                    }
                }
                expEnv.setHost(hostEntity);

                // install exporter java programe
                if (isNewInstall) {
                    sendMsg(null, "upload exporter package");
                    uploadExporter(session, path);
                    String java = getJavaVersion(expEnv);
                    // upload run_agent.sh
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("port", httpPort);
                    paramMap.put("java", java);
                    uploadScript(session, path, paramMap);

                    // 保存env和agentNodeRelation
                    // set success,insert data
                    nextStep();
                    envMapper.insert(expEnv);
                    updateAgentNodeRel(expEnv.getId(), exporterInstallDTO.getNodeIds());
                    // exec
                    sendMsg(null, "startup exporter");
                    session.executeNoWait("cd " + path + " && source /etc/profile && sh run_agent.sh start");
                    session.executeNoWait("cd " + path + " && " + "rm -rf application.yml");
                } else {
                    skipStep();
                    updateAgentNodeRel(expEnv.getId(), exporterInstallDTO.getNodeIds());
                }
                boolean isStop = AgentStatusEnum.MANUAL_STOP.getStatus().equalsIgnoreCase(expEnv.getStatus());
                // set agent collect config: build param
                sendMsg(null, "init exporter");
                List<Map<String, Object>> param = getExporterParams(exporterInstallDTO.getNodeIds());
                if (isStop) {
                    initExporterParamsOffline(session, expEnv.getPath(), param);
                    startExporter(expEnv);
                } else {
                    initExporterParamsOnline(param, hostEntity.getPublicIp(), expEnv.getPort());
                }
                // check status
                checkHealthStatus(expEnv);
                // 重新分配agent
                nextStep();
                prometheusService.incAgentAlloc(expEnv);
                nextStep();
                sendMsg(Status.DONE, "");
            }
            return AjaxResult.success();
        } catch (Exception e) {
            if (StrUtil.isBlank(exporterInstallDTO.getEnvId()) && expEnv != null) {
                envMapper.deleteById(expEnv);
            }
            sendMsg(Status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
            return AjaxResult.error(e.getMessage() + ":" + sw);
        }
    }

    public void uninstall(WsSession wsSession, String envId) {
        List<Step> steps = new ArrayList<Step>();
        steps.add(new Step("exporteruninstall.step1"));
        steps.add(new Step("exporteruninstall.step2"));
        steps.add(new Step("exporteruninstall.step3"));
        steps.add(new Step("exporteruninstall.step4"));
        steps.add(new Step("exporteruninstall.step10"));
        initWsSessionStepTl(wsSession, steps);

        try {
            uninstallExporter(envId);
            nextStep();
            sendMsg(Status.DONE, "");
        } catch (Exception e) {
            sendMsg(Status.ERROR, e.getMessage());
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            sendMsg(null, sw.toString());
        }
        if (wsSessionStepTl.get() !=  null) {
            wsSessionStepTl.remove();
        }
    }

    private void uploadExporter(SshSessionUtils session, String path) throws IOException {
        session.execute("mkdir -p " + path);
        if (session.checkFileExist(path + "/" + EXPORTER_NAME)) {
            throw new CustomException("agent package is exist");
        }
        // upload
        File f = File.createTempFile("instance-exporter", ".jar");
        try (InputStream in = loader.getResource(EXPORTER_NAME).getInputStream();
             OutputStream out = new FileOutputStream(f)) {
            IoUtil.copy(in, out);
        }
        String path0 = path.endsWith("/") ? path : (path + "/");
        session.upload(f.getCanonicalPath(), path0 + EXPORTER_NAME);
        Files.delete(f.toPath());
    }

    private void uploadScript(SshSessionUtils sshSession, String path, Map<String, Object> paramMap)
        throws IOException {
        Resource startSh = loader.getResource("classpath:sh/run_agent.sh");
        try (InputStream inputStream = startSh.getInputStream();
             BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            StringBuilder strBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(CommonUtils.parseText(line, paramMap));
                strBuilder.append(System.lineSeparator());
            }
            ByteArrayInputStream inStream =
                new ByteArrayInputStream(strBuilder.toString().getBytes(Charset.defaultCharset()));
            String target = path + (path.endsWith("/") ? "" : "/") + startSh.getFilename();
            sshSession.upload(inStream, target);
            inStream.close();
        }
    }

    private void updateAgentNodeRel(String envId, List<String> nodeIds) {
        // 1、clear all relation data
        agentNodeRelationService.remove(
            new LambdaQueryWrapper<AgentNodeRelationDO>()
                .eq(AgentNodeRelationDO::getEnvId, envId));
        // refresh prometheus by setting template
        nodeIds.forEach(nodeId -> {
            AgentNodeRelationDO agentNodeRelationDO = new AgentNodeRelationDO();
            agentNodeRelationDO.setEnvId(envId);
            agentNodeRelationDO.setNodeId(nodeId);
            agentNodeRelationService.getBaseMapper().insert(agentNodeRelationDO);
        });
    }

    private List<Map<String, Object>> getExporterParams(List<String> nodeIds) {
        List<Map<String, Object>> param = new ArrayList<>();
        nodeIds.forEach(nodeId -> {
            ClusterManager.OpsClusterNodeVOSub nodeTemp = clusterManager.getOpsNodeById(nodeId);
            if (nodeTemp == null) {
                throw new CustomException("node not found:" + nodeId);
            }

            OpsHostEntity targetHostEntity = hostFacade.getById(nodeTemp.getHostId());
            String targetSSHUser = nodeTemp.getInstallUserName();
            OpsHostUserEntity targetUser =
                hostUserFacade.listHostUserByHostId(targetHostEntity.getHostId()).stream()
                    .filter(e -> targetSSHUser.equals(e.getUsername())).findFirst()
                    .orElseThrow(() -> new CustomException("user not found"));

            Map<String, Object> paramItem = new HashMap<>();
            paramItem.put("nodeId", nodeId);
            paramItem.put("hostId", nodeTemp.getHostId());
            paramItem.put("dbport", nodeTemp.getDbPort());
            paramItem.put("dbUsername", nodeTemp.getDbUser());
            paramItem.put("dbPassword", nodeTemp.getDbUserPassword());
            paramItem.put("pass", encryptionUtils.decrypt(targetUser.getPassword()));
            paramItem.put("user", nodeTemp.getInstallUserName());
            paramItem.put("machineIP", targetHostEntity.getPublicIp());
            paramItem.put("dbIp", targetHostEntity.getPublicIp());
            paramItem.put("machinePort", targetHostEntity.getPort());
            param.add(paramItem);
        });
        return param;
    }

    private void initExporterParamsOnline(List<Map<String, Object>> param, String ip, int port) {
        for (int i = 0; i < 11; i++) {
            try {
                String url = "http://" + ip + ":" + port + "/config/set";
                log.info("agent set config URl:{}", url);
                HttpUtil.post(url, JSONUtil.toJsonStr(param));
                break;
            } catch (IORuntimeException e) {
                if (i == 10) {
                    throw new CustomException(e.getMessage());
                }
            }
            ThreadUtil.sleep(5000L);
        }
    }

    private void initExporterParamsOffline(SshSessionUtils session, String path, List<Map<String, Object>> param)
        throws IOException {
        String filePath = path + (path.endsWith("/") ? "" : "/") + "application.yml";
        String ymlStr = session.execute("cat " + filePath);
        Map map = YamlUtils.loadAs(ymlStr, Map.class);
        map.put("targets", param);
        File agentConfigFile = File.createTempFile("application", ".tmp");
        FileUtil.appendUtf8String(YamlUtils.dump(map), agentConfigFile);
        // upload
        session.upload(agentConfigFile.getCanonicalPath(), filePath);
        Files.delete(agentConfigFile.toPath());
    }

    private void uninstallNodeAndGaussExporter(
            WsSession wsSession, ArrayList<Step> steps, int curr, OpsClusterNodeVO node) throws IOException {
        var nodeenvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.NODE_EXPORTER)
                        .eq(NctigbaEnvDO::getHostid, node.getHostId()));
        var nodeenv = nodeenvList.isEmpty() ? null : nodeenvList.get(0);
        var gaussExporterEnvList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.OPENGAUSS_EXPORTER)
                        .eq(NctigbaEnvDO::getHostid, node.getHostId()));
        var gaussExporterEnv = gaussExporterEnvList.isEmpty() ? null : gaussExporterEnvList.get(0);

        OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
        if (hostEntity == null) {
            throw new CustomException(CommonConstants.HOST_NOT_FOUND);
        }

        // installed user
        var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
            if (nodeenv != null) {
                return nodeenv.getUsername().equals(e.getUsername());
            } else {
                return gaussExporterEnv == null ? false : gaussExporterEnv.getUsername().equals(e.getUsername());
            }
        }).findFirst().orElse(null);
        if (user == null) {
            return;
        }
        steps.add(steps.size() - 1, new Step("exporteruninstall.step5"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step6"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step7"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step8"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step9"));
        curr = nextStep(wsSession, steps, curr);
        try (var sshsession = SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
                encryptionUtils.decrypt(user.getPassword()));) {
            curr = nextStep(wsSession, steps, curr);
            if (nodeenv != null) {
                var nodePid = sshsession.execute(command.PS.parse("node_exporter", nodeenv.getPort()));
                if (StrUtil.isNotBlank(nodePid)) {
                    curr = nextStep(wsSession, steps, curr);
                    sshsession.execute(command.KILL.parse(nodePid));
                } else {
                    curr = skipStep(wsSession, steps, curr);
                }
                envMapper.deleteById(nodeenv);
            } else curr = skipStep(wsSession, steps, curr);

            curr = nextStep(wsSession, steps, curr);
            var gaussPid = sshsession.execute(command.PS.parse("opengauss_exporter", gaussExporterEnv.getPort()));
            if (StrUtil.isNotBlank(gaussPid)) {
                curr = nextStep(wsSession, steps, curr);
                sshsession.execute(command.KILL.parse(gaussPid));
            } else {
                curr = skipStep(wsSession, steps, curr);
            }
            envMapper.deleteById(gaussExporterEnv);
        }
        return;
    }

    /**
     * uninstallExporter
     *
     * @param envId String
     */
    public void uninstallExporter(String envId) throws IOException {
        // get exporter info
        List<NctigbaEnvDO> exporterList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.EXPORTER)
                        .eq(NctigbaEnvDO::getId, envId));
        if (CollectionUtil.isEmpty(exporterList)) {
            throw new CustomException("exporter is not exist!");
        }
        NctigbaEnvDO exporterEnv = exporterList.get(0);
        // check host
        OpsHostEntity expHostEntity = hostFacade.getById(exporterEnv.getHostid());
        if (expHostEntity == null) {
            throw new CustomException(CommonConstants.HOST_NOT_FOUND);
        }
        nextStep();
        exporterEnv.setHost(expHostEntity);
        if (StrUtil.isBlank(exporterEnv.getStatus())) {
            oldVersionAdapter(exporterEnv);
        }
        String oldStatus = exporterEnv.getStatus();
        AgentExceptionVO agentException = checkPidStatus(exporterEnv);
        if (!agentException.isUpStatus()) {
            if (agentException.getExceptionInfo().contains("No such file or directory")) {
                killPid(exporterEnv);
                exporterEnv.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(exporterEnv);
            }
        } else {
            stoppingExporter(exporterEnv);
        }
        if (AgentStatusEnum.NORMAL.equals(oldStatus)) {
            prometheusService.decAgentAlloc(exporterEnv);
        }
        nextStep();
        clearInstallFolder(exporterEnv);
        nextStep();
        envMapper.deleteById(envId);
        sendMsg(Status.DONE, "");
    }

    private String getJavaVersion(NctigbaEnvDO env) {
        String java = "java";
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String javaVersion =
                session.execute("source /etc/profile && java -version 2>&1 | awk -F '\"' '/version/ {print $2}'");
            if (!StrUtil.isBlank(javaVersion)) {
                String[] parts = javaVersion.split("\\.");
                int majorVersion = Integer.parseInt(parts[0]);
                int minorVersion = Integer.parseInt(parts[1]);
                if (majorVersion > 1 || (majorVersion == 1 && minorVersion >= 8)) {
                    return java;
                }
            }
            String jdkIsExists = session.execute(
                "[ -f /etc/jdk8/bin/java ] && echo 'true' || echo 'false'");
            if (jdkIsExists.contains("true")) {
                return "/etc/jdk8/bin/java";
            }
            sendMsg(null, "java not found, downloading");
            String arch = session.execute(command.ARCH);
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
                sendMsg(null, "agent.install.downloadsuccess");
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

    /**
     * get exporter status
     *
     * @return List<AgentStatusVO>
     */
    public List<AgentStatusVO> getStatus() {
        List<NctigbaEnvDO> envList = envMapper.selectList(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.envType.EXPORTER.name()));
        if (CollectionUtil.isEmpty(envList)) {
            return new ArrayList<>();
        }
        return envList.stream().map(env -> AgentStatusVO.of(env)).collect(Collectors.toList());
    }

    /**
     * monitorStatus
     */
    @Scheduled(fixedDelay = CommonConstants.MONITOR_CYCLE, timeUnit = TimeUnit.SECONDS)
    public void monitorStatus() {
        List<NctigbaEnvDO> envList = envMapper
            .selectList(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType,
                NctigbaEnvDO.envType.EXPORTER.name()));
        envList.forEach(e -> {
            e.setHost(hostFacade.getById(e.getHostid()));
        });
        for (NctigbaEnvDO env : envList) {
            try {
                String oldStatus = env.getStatus();
                if (StrUtil.isBlank(oldStatus)) {
                    oldVersionAdapter(env);
                }
                if (AgentStatusEnum.MANUAL_STOP.getStatus().equals(oldStatus)) {
                    continue;
                }
                long updateTime = env.getUpdateTime() != null ? env.getUpdateTime().getTime()
                    : new Date().getTime() - 3 * CommonConstants.MONITOR_CYCLE * 1000L - 1000L;
                boolean isTimeout = new Date().getTime() - updateTime > 3 * CommonConstants.MONITOR_CYCLE * 1000L;
                if ((AgentStatusEnum.STARTING.getStatus().equals(oldStatus)
                    || AgentStatusEnum.STOPPING.getStatus().equals(oldStatus)) && !isTimeout) {
                    continue;
                }
                AgentExceptionVO check = checkPidStatus(env);
                if (!check.isUpStatus()) {
                    startExporter(env);
                    prometheusService.incAgentAlloc(env);
                }
                checkHealthStatus(env);
                String status = env.getStatus();
                if (AgentStatusEnum.NORMAL.equals(oldStatus) && !AgentStatusEnum.NORMAL.equals(status)) {
                    prometheusService.decAgentAlloc(env);
                }
                if (!AgentStatusEnum.NORMAL.equals(oldStatus) && AgentStatusEnum.NORMAL.equals(status)) {
                    prometheusService.incAgentAlloc(env);
                }
            } catch (CustomException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * startup exporter
     *
     * @param id String
     */
    public void start(String id) {
        NctigbaEnvDO env = getEnvInfo(id);
        if (StrUtil.isBlank(env.getStatus())) {
            oldVersionAdapter(env);
        }
        startExporter(env);
        checkHealthStatus(env);
        prometheusService.incAgentAlloc(env);
    }

    private void checkHealthStatus(NctigbaEnvDO env) {
        for (int i = 0; i < 11; i++) {
            try {
                Boolean isAlive = agentService.isAgentAlive(env.getHost().getPublicIp(), env.getPort().toString());
                if (!isAlive) {
                    throw new CustomException("Agent is not alive");
                }
                env.setStatus(AgentStatusEnum.NORMAL.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                break;
            } catch (Exception e) {
                ThreadUtil.sleep(3000L);
                if (i == 10) {
                    env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                    envMapper.updateById(env);
                    throw new CustomException("Agent is unhealthy");
                }
            }
        }
    }

    private void startExporter(NctigbaEnvDO env) {
        AgentExceptionVO check = checkPidStatus(env);
        if (check.isUpStatus()) {
            throw new CustomException(check.getExceptionInfo());
        }
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String message = session.execute(String.format(CommonConstants.PORT_IS_EXIST, env.getPort()));
            if (message.contains("true")) {
                env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException("port is exists!");
            }
            if (!session.checkDirExist(env.getPath())) {
                env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                throw new CustomException("package is not exists!");
            }
            env.setStatus(AgentStatusEnum.STARTING.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            String command = "cd " + env.getPath() + " && source /etc/profile && sh run_agent.sh start";
            session.executeNoWait(command);
        } catch (IOException e) {
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException("exec failed:" + e.getMessage());
        }
    }

    /**
     * stop exporter
     *
     * @param id String
     */
    public void stop(String id) {
        NctigbaEnvDO env = getEnvInfo(id);
        if (StrUtil.isBlank(env.getStatus())) {
            oldVersionAdapter(env);
        }
        stopExporter(env);
        prometheusService.decAgentAlloc(env);
    }

    private void stopExporter(NctigbaEnvDO env) {
        AgentExceptionVO agentException = checkPidStatus(env);
        if (!agentException.isUpStatus()) {
            if (agentException.getExceptionInfo().contains("No such file or directory")) {
                killPid(env);
                env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
                envMapper.updateById(env);
                return;
            }
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        stoppingExporter(env);
    }

    private void stoppingExporter(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String command = "cd " + env.getPath() + " && sh run_agent.sh stop";
            session.execute(command);
        } catch (IOException e) {
            env.setStatus(AgentStatusEnum.ERROR_THREAD_NOT_EXISTS.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(e.getMessage());
        }
        AgentExceptionVO agentException = checkPidStatus(env);
        if (agentException.isUpStatus()) {
            env.setStatus(AgentStatusEnum.ERROR_PROGRAM_UNHEALTHY.getStatus()).setUpdateTime(new Date());
            envMapper.updateById(env);
            throw new CustomException(agentException.getExceptionInfo());
        }
        env.setStatus(AgentStatusEnum.MANUAL_STOP.getStatus()).setUpdateTime(new Date());
        envMapper.updateById(env);
    }

    private void killPid(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String command = String.format(CommonConstants.PORT_PID, env.getPort());
            String result = session.execute(command);
            if (StrUtil.isNotBlank(result)) {
                String[] arr = result.split(System.lineSeparator());
                String pid = arr[arr.length - 1];
                if (!pid.matches("\\d+")) {
                    return;
                }
                session.execute(SshSessionUtils.command.KILL.parse(pid));
            }
        } catch (IOException | RuntimeException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private AgentExceptionVO checkPidStatus(NctigbaEnvDO env) {
        AgentExceptionVO agentExceptionVO = new AgentExceptionVO();
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String cd = "cd " + env.getPath() + " && ";
            String message = session.execute(cd + " sh run_agent.sh status");
            agentExceptionVO.setAgentStatus(message.contains("Agent is running with PID"), message);
        } catch (IOException | RuntimeException e) {
            agentExceptionVO.setAgentStatus(false, "exec failed:" + e.getMessage());
        }
        return agentExceptionVO;
    }

    private NctigbaEnvDO getEnvInfo(String id) {
        NctigbaEnvDO env = envMapper.selectById(id);
        if (env == null) {
            throw new CustomException("Exporter not found");
        }
        env.setHost(hostFacade.getById(env.getHostid()));
        return env;
    }

    private SshSessionUtils connect(String hostId, String userName) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (hostEntity == null) {
            throw new CustomException(CommonConstants.HOST_NOT_FOUND);
        }
        OpsHostUserEntity user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(
            e -> userName.equals(e.getUsername())).findFirst().orElse(null);
        return SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(), userName,
            encryptionUtils.decrypt(user.getPassword()));
    }

    private void oldVersionAdapter(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            String target = env.getPath() + (env.getPath().endsWith("/") ? "" : "/") + "run_agent.sh";
            if (session.checkFileExist(target)) {
                // upload run_agent.sh
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("port", env.getPort());
                paramMap.put("java", getJavaVersion(env));
                uploadScript(session, env.getPath(), paramMap);
            }
            String result = session.execute(String.format(CommonConstants.PORT_PID, env.getPort()));
            if (StrUtil.isNotBlank(result)) {
                String[] arr = result.split(System.lineSeparator());
                String pid = arr[arr.length - 1];
                if (!pid.matches("\\d+")) {
                    return;
                }
                File pidFile = File.createTempFile("agent", ".pid");
                FileUtil.appendUtf8String(pid, pidFile);
                session.upload(pidFile.getCanonicalPath(), env.getPath() + "/instance-exporter.pid");
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private void clearInstallFolder(NctigbaEnvDO env) {
        try (SshSessionUtils session = connect(env.getHostid(), env.getUsername())) {
            if (StrUtil.isNotBlank(env.getPath()) && session.checkDirExist(env.getPath())) {
                String cd = "cd " + env.getPath() + " && ";
                session.execute(cd + "rm -rf " + env.getPath());
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }
}