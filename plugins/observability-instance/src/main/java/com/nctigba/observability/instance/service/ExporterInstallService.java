/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.entity.NctigbaEnv.envType;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig;
import com.nctigba.observability.instance.service.PrometheusService.prometheusConfig.job;
import com.nctigba.observability.instance.util.Download;
import com.nctigba.observability.instance.util.SshSession;
import com.nctigba.observability.instance.util.SshSession.command;
import com.nctigba.observability.instance.util.YamlUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExporterInstallService extends AbstractInstaller {
    private static final String EXPORTER_NAME = "instance-exporter.jar";
    private static final String JDK = "https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/";
    private static final String JDKPKG = "bisheng-jdk-11.0.17-linux-{0}.tar.gz";

    private final ClusterManager clusterManager;
    private final ResourceLoader loader;

    public void install(WsSession wsSession, String nodeId, String path, Integer exporterPort, Integer httpPort) {
        // @formatter:off
		var steps = Arrays.asList(
				new Step("exporterinstall.step1"),
				new Step("exporterinstall.step2"),
				new Step("exporterinstall.step3"),
				new Step("exporterinstall.step4"),
				new Step("exporterinstall.step5"),
				new Step("exporterinstall.step6"),
				new Step("exporterinstall.step7"));
		// @formatter:on
        int curr = 0;

        curr = nextStep(wsSession, steps, curr);
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        NctigbaEnv expEnv = null;
        try {
            var promEnv = envMapper
                    .selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, envType.PROMETHEUS));
            if (promEnv == null)
                throw new RuntimeException("prometheus not exists");
            if (StrUtil.isBlank(promEnv.getPath()))
                throw new RuntimeException("prometheus installing");

            curr = nextStep(wsSession, steps, curr);
            var node = clusterManager.getOpsNodeById(nodeId);
            if (node == null)
                throw new RuntimeException("node not found");
            var hostId = node.getHostId();
            OpsHostEntity hostEntity = hostFacade.getById(hostId);
            if (hostEntity == null)
                throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
            var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
                return node.getInstallUserName().equals(e.getUsername());
            }).findFirst().orElse(null);
            try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
                    encryptionUtils.decrypt(user.getPassword()));) {
                // check port
                session.testPortCanUse(httpPort);
                session.testPortCanUse(exporterPort);

                expEnv = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getHostid, hostId)
                        .eq(NctigbaEnv::getType, envType.EXPORTER).eq(NctigbaEnv::getNodeid, nodeId));
                curr = nextStep(wsSession, steps, curr);
                if (expEnv == null) {
                    session.execute("mkdir -p " + path);
                    expEnv = new NctigbaEnv().setHostid(hostId).setNodeid(nodeId).setPort(httpPort)
                            .setUsername(node.getInstallUserName()).setType(envType.EXPORTER);
                    // install new exporter
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
                        var pkg = envMapper
                                .selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, tar));
                        if (pkg == null) {
                            var f = Download.download(JDK + tar, "pkg/" + tar);
                            pkg = new NctigbaEnv().setPath(f.getCanonicalPath()).setType("JDK");
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
                    try (var in = loader.getResource(EXPORTER_NAME).getInputStream();
                            var out = new FileOutputStream(f);) {
                        IoUtil.copy(in, out);
                    }
                    session.upload(f.getCanonicalPath(), path + EXPORTER_NAME);
                    Files.delete(f.toPath());
                    curr = nextStep(wsSession, steps, curr);
                    // exec
                    session.executeNoWait("cd " + path + " && " + java + " -jar " + EXPORTER_NAME + " --server.port="
                            + httpPort + " --exporter.port=" + exporterPort + " &");
                    expEnv.setPath(path);
                    for (int i = 0; i < 10; i++) {
                        ThreadUtil.sleep(5000L);
                        try {
                            HttpUtil.get("http://" + hostEntity.getPublicIp() + ":" + expEnv.getPort() + "/config/set",
                                    Map.of("hostId", hostId, "nodeId", nodeId, "dbport", node.getDbPort(), "username",
                                            node.getDbUser(), "password", node.getDbUserPassword()));
                        } catch (IORuntimeException e) {
                            if (i == 9)
                                throw e;
                        }
                    }
                    curr = nextStep(wsSession, steps, curr);
                    envMapper.insert(expEnv);
                    // reload prometheus
                    var promeHost = hostFacade.getById(promEnv.getHostid());
                    var promUser = hostUserFacade.listHostUserByHostId(promEnv.getHostid()).stream()
                            .filter(p -> p.getUsername().equals(promEnv.getUsername())).findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                    "The node information corresponding to the host is not found"));
                    try (var promSession = SshSession.connect(promeHost.getPublicIp(), promeHost.getPort(),
                            promEnv.getUsername(), encryptionUtils.decrypt(promUser.getPassword()));) {
                        var promYmlStr = promSession
                                .execute("cat " + promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
                        var conf = YamlUtil.loadAs(promYmlStr, prometheusConfig.class);

                        var conexporter = new job.conf();
                        conexporter.setLabels(Map.of("instance", nodeId, "type", "exporter"));
                        conexporter.setTargets(Arrays.asList(node.getPublicIp() + ":" + exporterPort));
                        var job = new job();
                        job.setStatic_configs(Arrays.asList(conexporter));
                        job.setJob_name(nodeId);
                        conf.getScrape_configs().add(job);
                        conf.setScrape_configs(new ArrayList<>(new LinkedHashSet<>(conf.getScrape_configs())));

                        var prometheusConfigFile = File.createTempFile("prom", ".tmp");
                        FileUtil.appendUtf8String(YamlUtil.dump(conf), prometheusConfigFile);
                        promSession.execute("rm " + promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
                        promSession.upload(prometheusConfigFile.getAbsolutePath(),
                                promEnv.getPath() + CommonConstants.PROMETHEUS_YML);
                        Files.delete(prometheusConfigFile.toPath());
                    }

                    // curl -X POST http://IP/-/reload
                    var res = HttpUtil.post("http://" + promeHost.getPublicIp() + ":" + promEnv.getPort() + "/-/reload",
                            "");
                    if ("Lifecycle API is not enabled.".equals(res)) {
                        // TODO reload fail
                    }
                    curr = nextStep(wsSession, steps, curr);
                    sendMsg(wsSession, steps, curr, status.DONE);
                } else {
                    // use old exporter
                    addMsg(wsSession, steps, curr, "exporter exists");
                }
            }
        } catch (Exception e) {
            if (expEnv != null)
                envMapper.deleteById(expEnv);
            steps.get(curr).setState(status.ERROR).add(e.getMessage());
            wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
            var sw = new StringWriter();
            try (var pw = new PrintWriter(sw);) {
                e.printStackTrace(pw);
            }
            wsUtil.sendText(wsSession, sw.toString());
        }
    }

    public void uninstall(WsSession wsSession, String nodeId) {
        var steps = new ArrayList<Step>();
        steps.add(new Step("exporteruninstall.step1"));
        steps.add(new Step("exporteruninstall.step10"));
        var curr = 0;

        try {
            var node = clusterManager.getOpsNodeById(nodeId);
            uninstallExporter(wsSession, steps, curr, node);
            curr = steps.size() - 1;
            uninstallNodeAndGaussExporter(wsSession, steps, curr, node);
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

    private void uninstallExporter(WsSession wsSession, ArrayList<Step> steps, int curr, OpsClusterNodeVOSub node) {
        var exporterList = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery()
                .eq(NctigbaEnv::getType, envType.EXPORTER).eq(NctigbaEnv::getHostid, node.getHostId()));
        var exporterenv = exporterList.isEmpty() ? null : exporterList.get(0);
        if (exporterenv == null) {
            return;
        }
        OpsHostEntity expHostEntity = hostFacade.getById(node.getHostId());
        if (expHostEntity == null)
            throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);
        steps.add(steps.size() - 1, new Step("exporteruninstall.step2"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step3"));
        steps.add(steps.size() - 1, new Step("exporteruninstall.step4"));
        curr = nextStep(wsSession, steps, curr);
        var expuser = hostUserFacade.listHostUserByHostId(expHostEntity.getHostId()).stream().filter(e -> {
            return exporterenv == null ? false : exporterenv.getUsername().equals(e.getUsername());
        }).findFirst().orElse(null);
        // exporter user
        if (expuser == null) {
            curr = skipStep(wsSession, steps, curr);
            return;
        }
        try (var sshsession = SshSession.connect(expHostEntity.getPublicIp(), expHostEntity.getPort(),
                expuser.getUsername(), encryptionUtils.decrypt(expuser.getPassword()));) {
            curr = nextStep(wsSession, steps, curr);
            // ps
            var nodePid = sshsession.execute(command.PS.parse(EXPORTER_NAME, exporterenv.getPort()));
            if (StrUtil.isNotBlank(nodePid)) {
                curr = nextStep(wsSession, steps, curr);
                // kill
                sshsession.execute(command.KILL.parse(nodePid));
            } else {
                curr = skipStep(wsSession, steps, curr);
            }
            curr = nextStep(wsSession, steps, curr);
            envMapper.deleteById(exporterenv);
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

    private void uninstallNodeAndGaussExporter(WsSession wsSession, ArrayList<Step> steps, int curr,
            OpsClusterNodeVOSub node) throws IOException {
        var nodeenvList = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery()
                .eq(NctigbaEnv::getType, envType.NODE_EXPORTER).eq(NctigbaEnv::getHostid, node.getHostId()));
        var nodeenv = nodeenvList.isEmpty() ? null : nodeenvList.get(0);
        var gaussExporterEnvList = envMapper.selectList(Wrappers.<NctigbaEnv>lambdaQuery()
                .eq(NctigbaEnv::getType, envType.OPENGAUSS_EXPORTER).eq(NctigbaEnv::getHostid, node.getHostId()));
        var gaussExporterEnv = gaussExporterEnvList.isEmpty() ? null : gaussExporterEnvList.get(0);

        OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
        if (hostEntity == null)
            throw new RuntimeException(CommonConstants.HOST_NOT_FOUND);

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
        try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
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
            } else
                curr = skipStep(wsSession, steps, curr);

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
}