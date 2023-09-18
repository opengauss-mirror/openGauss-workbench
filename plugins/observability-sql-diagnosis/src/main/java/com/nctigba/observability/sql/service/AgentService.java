package com.nctigba.observability.sql.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.service.AbstractInstaller.Step.status;
import com.nctigba.observability.sql.util.Download;
import com.nctigba.observability.sql.util.SshSession;
import com.nctigba.observability.sql.util.SshSession.command;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
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
import java.util.Arrays;

@Service
public class AgentService extends AbstractInstaller {
    private static final String AGENT_USER = "root";
    private static final String NAME = "opengauss-ebpf.jar";
    private static final String JDK = "https://mirrors.huaweicloud.com/kunpeng/archive/compiler/bisheng_jdk/";
    private static final String JDKPKG = "bisheng-jdk-11.0.17-linux-{0}.tar.gz";
    @Autowired
    private ResourceLoader loader;
    @Autowired
    private ClusterManager clusterManager;

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
                new Step("agent.install.step7"),
                new Step("agent.install.step8"),
                new Step("agent.install.step9"),
                new Step("agent.install.step10"));
        // @formatter:on
        var curr = 0;
        try {
            if (StrUtil.isBlank(callbackPath))
                throw new RuntimeException("callback host null");

            curr = nextStep(wsSession, steps, curr);
            var node = clusterManager.getOpsNodeById(nodeId);
            var hostId = node.getHostId();

            curr = nextStep(wsSession, steps, curr);
            var env = envMapper.selectOne(
                    Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.envType.AGENT)
                            .eq(NctigbaEnv::getNodeid, nodeId));
            if (env != null)
                throw new RuntimeException("agent exists");
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            env = new NctigbaEnv().setHostid(hostId).setNodeid(nodeId).setPort(port).setUsername(AGENT_USER)
                    .setType(NctigbaEnv.envType.AGENT);
            try (var session = connect(env, rootPassword);) {
                curr = nextStep(wsSession, steps, curr);
                session.execute("mkdir -p " + path);
                if (!session.test("unzip -v"))
                    session.execute("yum install -y unzip zip");
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
                    var pkg = envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().like(NctigbaEnv::getPath, tar));
                    if (pkg == null) {
                        var f = Download.download(JDK + tar, "pkg/" + tar);
                        pkg = new NctigbaEnv().setPath(f.getCanonicalPath()).setType(NctigbaEnv.envType.AGENT_PKG);
                        addMsg(wsSession, steps, curr, "agent.install.downloadsuccess");
                        save(pkg);
                    }
                    session.upload(pkg.getPath(), tar);
                    session.execute("tar zxvf " + tar + " -C /etc/");
                    session.execute("mv /etc/bisheng-jdk-11.0.17 /etc/jdk11");
                    session.execute("rm -rf " + tar);
                }
                curr = nextStep(wsSession, steps, curr);
                var python_v = session.execute("python --version");
                if (!python_v.toLowerCase().startsWith("python "))
                    throw new RuntimeException("python version err, curr:" + python_v);
                addMsg(wsSession, steps, curr, python_v);
                curr = nextStep(wsSession, steps, curr);
                // bcc-tool
                if (!session.test(command.STAT.parse("/usr/share/bcc/tools")))
                    for (int i = 0; i < 3; i++)
                        try {
                            session.execute("yum -y install bcc-tools");
                            break;
                        } catch (Exception e) {
                            if (i == 2)
                                throw new RuntimeException("bcc install fail");
                            addMsg(wsSession, steps, curr, "bcc install fail " + i + ", retrying");
                        }
                else
                    addMsg(wsSession, steps, curr, "bcc exists");
                curr = nextStep(wsSession, steps, curr);
                File graph = File.createTempFile("FlameGraph", ".zip");
                try (var in = loader.getResource("FlameGraph.zip").getInputStream();
                     var out = new FileOutputStream(graph);) {
                    IoUtil.copy(in, out);
                }
                session.execute("mkdir -p /opt/software");
                session.execute("rm -rf /opt/software/FlameGraph*");
                session.upload(graph.getCanonicalPath(), "/opt/software/FlameGraph.zip");
                session.execute("cd /opt/software && unzip FlameGraph.zip && cd FlameGraph && chmod +x flamegraph.pl");

                curr = nextStep(wsSession, steps, curr);
                // upload
                File f = File.createTempFile("opengauss-ebpf-1.0.0-SNAPSHOT", ".jar");
                try (var in = loader.getResource(NAME).getInputStream(); var out = new FileOutputStream(f);) {
                    IoUtil.copy(in, out);
                }
                session.upload(f.getCanonicalPath(), path + NAME);
                Files.delete(f.toPath());
                curr = nextStep(wsSession, steps, curr);
                // exec
                session.executeNoWait(
                        "cd " + path + " && " + java + " --add-opens java.base/java.lang=ALL-UNNAMED -jar " + NAME
                                + " --diagnosis_host=" + callbackPath + " --agent_port=" + env.getPort() + " &");
                env.setPath(path);
                curr = nextStep(wsSession, steps, curr);
                envMapper.insert(env);
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

    private SshSession connect(NctigbaEnv env, String rootPassword) throws IOException {
        OpsHostEntity hostEntity = hostFacade.getById(env.getHostid());
        if (hostEntity == null)
            throw new RuntimeException("host not found");
        env.setHost(hostEntity);
        var user = getUser(hostEntity, AGENT_USER, rootPassword);
        return SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), AGENT_USER,
                encryptionUtils.decrypt(user.getPassword()));
    }

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
                    Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, NctigbaEnv.envType.AGENT)
                            .eq(NctigbaEnv::getNodeid, nodeId));
            if (env == null)
                throw new RuntimeException("agent not found");
            OpsHostEntity hostEntity = hostFacade.getById(node.getHostId());
            if (hostEntity == null)
                throw new RuntimeException("host not found");
            env.setHost(hostEntity);
            try (var sshsession = SshSession.connect(hostEntity.getPublicIp(), hostEntity.getPort(), "root",
                    encryptionUtils.decrypt(rootPassword));) {
                curr = nextStep(wsSession, steps, curr);
                var nodePid = sshsession.execute(command.PS.parse(NAME, env.getPort()));
                if (StrUtil.isNotBlank(nodePid)) {
                    curr = nextStep(wsSession, steps, curr);
                    sshsession.execute(command.KILL.parse(nodePid));
                } else {
                    curr = skipStep(wsSession, steps, curr);
                }
                curr = nextStep(wsSession, steps, curr);
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