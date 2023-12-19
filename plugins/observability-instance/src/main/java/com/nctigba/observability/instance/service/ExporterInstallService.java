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

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import com.nctigba.observability.instance.model.dto.PrometheusConfigNodeDTO;
import com.nctigba.observability.instance.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.instance.model.entity.CollectTemplateNodeDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO.envType;
import com.nctigba.observability.instance.exception.TipsException;
import com.nctigba.observability.instance.mapper.CollectTemplateNodeMapper;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.util.DownloadUtils;
import com.nctigba.observability.instance.util.SshSessionUtils;
import com.nctigba.observability.instance.util.SshSessionUtils.command;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExporterInstallService extends AbstractInstaller {
    private static final String EXPORTER_NAME = "instance-exporter.jar";
    private static final String JDK = "https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/";
    private static final String JDKPKG = "bisheng-jdk-11.0.17-linux-{0}.tar.gz";

    @Autowired
    ClusterManager clusterManager;
    @Autowired
    ResourceLoader loader;
    @Autowired
    CollectTemplateNodeMapper collectTemplateNodeMapper;
    @Autowired
    CollectTemplateNodeService collectTemplateNodeService;
    @Autowired
    AgentNodeRelationService agentNodeRelationService;
    @Autowired
    AgentService agentService;

    public void install(WsSession wsSession, ExporterInstallDTO exporterInstallDTO) {
        var steps = Arrays.asList(new Step("exporterinstall.step1"), new Step("exporterinstall.step2"),
                new Step("exporterinstall.step3"), new Step("exporterinstall.step4"),
                new Step("exporterinstall.step5"), new Step("exporterinstall.step6"),
                new Step("exporterinstall.step7"));
        int curr = 0;

        curr = nextStep(wsSession, steps, curr);
        String path = exporterInstallDTO.getPath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        NctigbaEnvDO expEnv = null;
        try {
            // tod: check nodeId,check node db install user
            // check prometheus
            var promEnv =
                    envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.PROMETHEUS));
            if (promEnv == null) throw new RuntimeException("prometheus not exists");
            if (StrUtil.isBlank(promEnv.getPath())) throw new RuntimeException("prometheus installing");

            curr = nextStep(wsSession, steps, curr);

            // get host data
            OpsHostEntity hostEntity = hostFacade.getById(exporterInstallDTO.getHostId());
            if (hostEntity == null) throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);

            // get user data
            OpsHostUserEntity user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream()
                    .filter(e -> exporterInstallDTO.getUsername().equals(e.getUsername())).findFirst()
                    .orElseThrow(() -> new CustomException("user not found"));

            try (SshSessionUtils session = SshSessionUtils.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                    user.getUsername(),
                    encryptionUtils.decrypt(user.getPassword()))) {
                // check agent port
                Integer httpPort = exporterInstallDTO.getHttpPort();
                Boolean isNewInstall = StrUtil.isBlank(exporterInstallDTO.getEnvId());
                if (isNewInstall) {
                    session.testPortCanUse(httpPort);
                } else {
                    Boolean isAgentAlive =
                            agentService.isAgentAlive(hostEntity.getPublicIp(),
                                    String.valueOf(exporterInstallDTO.getHttpPort()));
                    if (!isAgentAlive) {
                        throw new TipsException("agentNotAlive");
                    }
                }

                curr = nextStep(wsSession, steps, curr);

                if (isNewInstall) {
                    // check existed,get installed data (same host id and same port)
                    List<NctigbaEnvDO> expEnvs = envMapper.selectList(
                            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getHostid, exporterInstallDTO.getHostId())
                                    .eq(NctigbaEnvDO::getType, envType.EXPORTER).eq(NctigbaEnvDO::getPort, httpPort));
                    if (expEnvs.size() > 0) {
                        addMsg(wsSession, steps, curr, "exporter exists");
                        return;
                    }
                    expEnv =
                            new NctigbaEnvDO().setHostid(exporterInstallDTO.getHostId()).setNodeid(null).setPort(httpPort)
                                    .setUsername(exporterInstallDTO.getUsername()).setType(envType.EXPORTER)
                                    .setPath(path);
                } else {
                    expEnv = envMapper.selectById(exporterInstallDTO.getEnvId());
                    if (expEnv == null) {
                        throw new TipsException("envIdError");
                    }
                }

                // install exporter java programe
                if (isNewInstall) {
                    session.execute("mkdir -p " + path);
                    var java = "java";
                    try {
                        if (!session.test(java + " --version")) {
                            java = "/etc/jdk11/bin/java";
                            var java_v = session.execute(java + " --version");
                            addMsg(wsSession, steps, curr, java_v);
                        }
                    } catch (Exception e) {
                        addMsg(wsSession, steps, curr, "java not found, downloading");
                        var arch = session.execute(command.ARCH);
                        var v = "aarch64".equals(arch) ? "aarch64" : "x64";
                        var tar = MessageFormat.format(JDKPKG, v);
                        var pkg =
                                envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().like(NctigbaEnvDO::getPath, tar));
                        if (pkg == null) {
                            var f = DownloadUtils.download(JDK + tar, "pkg/" + tar);
                            pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType("JDK");
                            addMsg(wsSession, steps, curr, "agent.install.downloadsuccess");
                            save(pkg);
                        }
                        session.upload(pkg.getPath(), tar);
                        session.execute("tar zxvf " + tar + " -C /etc/");
                        session.execute("mv /etc/bisheng-jdk-11.0.17 /etc/jdk11");
                        session.execute("rm -rf " + tar);
                    }
                    // upload
                    File f = File.createTempFile("instance-exporter", ".jar");
                    try (var in = loader.getResource(EXPORTER_NAME).getInputStream(); var out = new FileOutputStream(
                            f);) {
                        IoUtil.copy(in, out);
                    }
                    session.upload(f.getCanonicalPath(), path + EXPORTER_NAME);
                    Files.delete(f.toPath());
                    curr = nextStep(wsSession, steps, curr);
                    // exec
                    session.executeNoWait(
                            "cd " + path + " && " + java + " -jar " + EXPORTER_NAME
                                    + " --server.port=" + httpPort + " &");
                    session.execute("cd " + path + " && " + "rm -rf application.yml");
                }

                // set agent collect config
                // build param
                List<Map<String, Object>> param = new ArrayList<>();
                exporterInstallDTO.getNodeIds().forEach(nodeId -> {
                    ClusterManager.OpsClusterNodeVOSub nodeTemp = clusterManager.getOpsNodeById(nodeId);
                    if (nodeTemp == null) throw new RuntimeException("node not found:" + nodeId);

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
                log.info("agent set config param:{}", JSONUtil.toJsonStr(param));
                for (int i = 0; i < 11; i++) {
                    try {
                        String url = "http://" + hostEntity.getPublicIp() + ":" + expEnv.getPort() + "/config/set";
                        log.info("agent set config URl:{}", url);
                        String result = HttpUtil.post(url, JSONUtil.toJsonStr(param));
                        log.info("agent set config result:{}", result);
                        break;
                    } catch (IORuntimeException e) {
                        if (i == 10) throw e;
                    }
                    ThreadUtil.sleep(5000L);
                }

                // set success,insert data
                curr = nextStep(wsSession, steps, curr);
                if (isNewInstall) {
                    envMapper.insert(expEnv);
                }

                String envId = expEnv.getId();
                // todo just fresh once for multi nodes
                // 1、clear all relation data
                agentNodeRelationService.remove(
                        new LambdaQueryWrapper<AgentNodeRelationDO>()
                                .eq(AgentNodeRelationDO::getEnvId, envId));
                // refresh prometheus by setting template
                exporterInstallDTO.getNodeIds().forEach(nodeId -> {
                    AgentNodeRelationDO agentNodeRelationDO = new AgentNodeRelationDO();
                    agentNodeRelationDO.setEnvId(envId);
                    agentNodeRelationDO.setNodeId(nodeId);
                    agentNodeRelationService.saveOrUpdate(
                        agentNodeRelationDO,
                            new LambdaQueryWrapper<AgentNodeRelationDO>()
                                    .eq(AgentNodeRelationDO::getNodeId, nodeId));

                    // find node template
                    CollectTemplateNodeDO collectTemplateNodeDO =
                            collectTemplateNodeMapper.selectOne(
                                    new LambdaQueryWrapper<CollectTemplateNodeDO>()
                                            .eq(CollectTemplateNodeDO::getNodeId, nodeId)
                                            .last("limit 1"));

                    Integer templateId =
                            collectTemplateNodeDO == null ? null : collectTemplateNodeDO.getTemplateId();

                    List<PrometheusConfigNodeDTO> configNodes =
                            collectTemplateNodeService.getNodePrometheusConfigParam(templateId, Arrays.asList(nodeId));
                    collectTemplateNodeService.setPrometheusConfig(configNodes);
                });

                curr = nextStep(wsSession, steps, curr);
                sendMsg(wsSession, steps, curr, status.DONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (expEnv != null) envMapper.deleteById(expEnv);
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
    }

    public void uninstall(WsSession wsSession, String envId) {
        var steps = new ArrayList<Step>();
        steps.add(new Step("exporteruninstall.step1"));
        steps.add(new Step("exporteruninstall.step10"));
        var curr = 0;

        try {
            uninstallExporter(wsSession, steps, curr, envId);
            curr = steps.size() - 1;
            sendMsg(wsSession, steps, curr, status.DONE);
        } catch (Exception e) {
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
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
        if (hostEntity == null) throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);

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

    private void uninstallExporter(WsSession wsSession, ArrayList<Step> steps, int curr, String envId) {
        // get exporter info
        var exporterList = envMapper.selectList(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, envType.EXPORTER)
                        .eq(NctigbaEnvDO::getId, envId));
        var exporterEnv = exporterList.isEmpty() ? null : exporterList.get(0);
        if (exporterEnv == null) {
            return;
        }

        // check host
        OpsHostEntity expHostEntity = hostFacade.getById(exporterEnv.getHostid());
        if (expHostEntity == null) throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);

        steps.add(steps.size() - 1, new Step("exporteruninstall.step2"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step3"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step4"));
        curr = nextStep(wsSession, steps, curr);

        // check exporter install user
        var expuser = hostUserFacade.listHostUserByHostId(expHostEntity.getHostId()).stream().filter(e -> {
            return exporterEnv == null ? false : exporterEnv.getUsername().equals(e.getUsername());
        }).findFirst().orElse(null);
        // exporter user
        if (expuser == null) {
            curr = skipStep(wsSession, steps, curr);
            return;
        }

        // 1.kill exporter process
        // 2.delete evn info
        // 3.delete relation info
        try (var sshsession = SshSessionUtils.connect(expHostEntity.getPublicIp(), expHostEntity.getPort(),
                expuser.getUsername(),
                encryptionUtils.decrypt(expuser.getPassword()));) {
            curr = nextStep(wsSession, steps, curr);
            // ps
            var nodePid = sshsession.execute(command.PS.parse(EXPORTER_NAME, exporterEnv.getPort()));
            if (StrUtil.isNotBlank(nodePid)) {
                curr = nextStep(wsSession, steps, curr);
                // kill
                sshsession.execute(command.KILL.parse(nodePid));
            } else {
                curr = skipStep(wsSession, steps, curr);
            }
            curr = nextStep(wsSession, steps, curr);
            envMapper.deleteById(exporterEnv);
            agentNodeRelationService.remove(
                    new LambdaQueryWrapper<AgentNodeRelationDO>()
                            .eq(AgentNodeRelationDO::getEnvId, exporterEnv.getId())
            );
        } catch (Exception e) {
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
        return;
    }
}