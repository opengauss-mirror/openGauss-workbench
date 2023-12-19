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

package com.nctigba.observability.log.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO;
import com.nctigba.observability.log.model.entity.NctigbaEnvDO.type;
import com.nctigba.observability.log.service.AbstractInstaller.Step.status;
import com.nctigba.observability.log.util.Download;
import com.nctigba.observability.log.util.SshSession;
import com.nctigba.observability.log.util.SshSession.command;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Arrays;

@Service
@Slf4j
public class FilebeatService extends AbstractInstaller {

    public static final String PATH = "https://artifacts.elastic.co/downloads/beats/filebeat/";
    public static final String NAME = "filebeat-8.3.3-linux-";
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ClusterManager clusterManager;

    public void install(WsSession wsSession, String path, String nodeId, JSONObject obj) {
        // @formatter:off
        var steps = Arrays.asList(
                new Step("filebeat.install.step1"),
                new Step("filebeat.install.step2"),
                new Step("filebeat.install.step3"),
                new Step("filebeat.install.step4"),
                new Step("filebeat.install.step5"),
                new Step("filebeat.install.step6"),
                new Step("filebeat.install.step7"),
                new Step("filebeat.install.step8"),
                new Step("filebeat.install.step9"),
                new Step("filebeat.install.step10"),
                new Step("filebeat.install.step11"));
        // @formatter:on
        int curr = 0;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        curr = nextStep(wsSession, steps, curr);
        try {
            var node = clusterManager.getOpsNodeById(nodeId);
            var cluster = clusterManager.getOpsClusterByNodeId(nodeId);
            if (node == null) {
                throw new RuntimeException("node not found");
            }
            curr = nextStep(wsSession, steps, curr);
            var hostId = node.getHostId();
            OpsHostEntity hostEntity = hostFacade.getById(hostId);
            var env = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getHostid, hostId)
                    .eq(NctigbaEnvDO::getType, type.FILEBEAT).eq(NctigbaEnvDO::getNodeid, nodeId));
            if (env != null) {
                throw new RuntimeException("filebeat exists");
            }
            env = new NctigbaEnvDO().setHostid(hostId).setType(type.FILEBEAT).setNodeid(nodeId).setPath(path);
            envMapper.insert(env);
            try {
                var esEnv = envMapper
                        .selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, type.ELASTICSEARCH));
                if (esEnv == null) {
                    throw new RuntimeException("elasticsearch not exist");
                }
                if (StrUtil.isBlank(esEnv.getUsername())) {
                    throw new RuntimeException("elasticsearch installing");
                }

                curr = nextStep(wsSession, steps, curr);
                var opsClusterNodeEntity = opsClusterNodeService.getById(nodeId);
                if (opsClusterNodeEntity == null) {
                    throw new RuntimeException("No cluster node information found");
                }

                curr = nextStep(wsSession, steps, curr);
                String installUserId = opsClusterNodeEntity.getInstallUserId();
                OpsHostUserEntity user = hostUserFacade.getById(installUserId);
                env.setUsername(user.getUsername());
                if (hostEntity == null) {
                    throw new RuntimeException("host not found");
                }
                curr = nextStep(wsSession, steps, curr);
                try (var session = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(),
                        user.getUsername(), encryptionUtils.decrypt(user.getPassword()));) {
                    curr = nextStep(wsSession, steps, curr);
                    session.execute("mkdir -p " + path);
                    String logPath = obj.getStr("logPath");
                    if (!session.test(command.STAT.parse(logPath))) {
                        throw new RuntimeException("log path err:" + logPath);
                    }

                    curr = nextStep(wsSession, steps, curr);
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
                    if (!session.test(command.STAT.parse(path + name))) {
                        if (!session.test(command.STAT.parse(path + tar))) {
                            var pkg = envMapper
                                    .selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().like(NctigbaEnvDO::getPath, tar));
                            if (pkg == null) {
                                var f = Download.download(PATH + tar, "pkg/" + tar);
                                pkg = new NctigbaEnvDO().setPath(f.getCanonicalPath()).setType(type.FILEBEAT_PKG);
                                addMsg(wsSession, steps, curr, "filebeat.install.download.success");
                                save(pkg);
                            }
                            session.upload(pkg.getPath(), path + tar);
                        }
                        session.execute("cd " + path + " && " + command.TAR.parse(tar));
                    }
                    curr = nextStep(wsSession, steps, curr);
                    String cd = "cd " + path + name + " && ";
                    // filebeat_conf.zip
                    File f = File.createTempFile("filebeat_conf", ".tar.gz");
                    try (var in = loader.getResource("filebeat_conf.tar.gz").getInputStream();
                         var out = new FileOutputStream(f);) {
                        System.out.println(f.getCanonicalPath());
                        IoUtil.copy(in, out);
                    }
                    session.upload(f.getCanonicalPath(), path + "filebeat_conf.tar.gz");
                    Files.delete(f.toPath());
                    session.execute("cd " + path + " && tar -xvf filebeat_conf.tar.gz");
                    var esHost = hostFacade.getById(esEnv.getHostid());

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

                    curr = nextStep(wsSession, steps, curr);
                    session.executeNoWait(
                            cd + " ./filebeat modules enable system opengauss && ./filebeat -e -c filebeat.yml &");

                    curr = nextStep(wsSession, steps, curr);
                    envMapper.updateById(env);
                    sendMsg(wsSession, steps, curr, status.DONE);
                }
            } catch (Exception e) {
                envMapper.deleteById(env);
                throw e;
            }
        } catch (Exception e) {
            log.error("", e);
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
            var env = envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, type.FILEBEAT)
                    .eq(NctigbaEnvDO::getNodeid, node.getNodeId()));
            if (env == null) {
                throw new RuntimeException("filebeat not found");
            }
            curr = nextStep(wsSession, steps, curr);
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (hostEntity == null) {
                throw new RuntimeException("host not found");
            }
            env.setHost(hostEntity);
            var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
                return env.getUsername().equals(e.getUsername());
            }).findFirst().orElse(null);
            try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), user.getUsername(),
                    encryptionUtils.decrypt(user.getPassword()));) {
                curr = nextStep(wsSession, steps, curr);
                var nodePid = sshsession.execute(command.PS.parse("filebeat"));
                if (StrUtil.isNotBlank(nodePid)) {
                    var pids = StrUtil.split(nodePid, '\n');
                    String currpid = null;
                    if (pids.size() > 1) {
                        for (var pid : pids) {
                            var path = sshsession.execute("ls -l /proc/" + pid + "|grep cwd|awk '{print $11}'");
                            if (path.startsWith(env.getPath())) {
                                currpid = pid;
                                break;
                            }
                        }
                    } else {
                        currpid = nodePid;
                    }
                    curr = nextStep(wsSession, steps, curr);
                    if (currpid != null) {
                        sshsession.execute(command.KILL.parse(currpid));
                    }
                } else {
                    curr = skipStep(wsSession, steps, curr);
                }
                envMapper.deleteById(env);
                sendMsg(wsSession, steps, curr, status.DONE);
            }
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
}