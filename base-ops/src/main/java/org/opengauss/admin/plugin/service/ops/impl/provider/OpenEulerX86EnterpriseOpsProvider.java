package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/12 09:23
 **/
@Slf4j
@Service
public class OpenEulerX86EnterpriseOpsProvider extends AbstractOpsProvider {

    @Autowired
    private IOpsClusterService opsClusterService;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private WsUtil wsUtil;

    @Override
    public OpenGaussVersionEnum version() {
        return OpenGaussVersionEnum.ENTERPRISE;
    }

    @Override
    public OpenGaussSupportOSEnum os() {
        return OpenGaussSupportOSEnum.OPENEULER_X86_64;
    }

    @Override
    public String dependencyCommand() {
        return SshCommandConstants.INSTALL_DEPENDENCY_OPENEULER_X86;
    }

    @Override
    public void install(InstallContext installContext) {
        log.info("Start installing Enterprise Edition");

        doInstall(installContext);

        wsUtil.sendText(installContext.getRetSession(),"SAVE_INSTALL_CONTEXT");
        OpsClusterContext opsClusterContext = saveContext(installContext);

        wsUtil.sendText(installContext.getRetSession(),"CREATE_REMOTE_USER");
        createRemoteUser(installContext, opsClusterContext);
        wsUtil.sendText(installContext.getRetSession(),"FINISH");
        log.info("The installation is complete");
    }

    private void doInstall(InstallContext installContext) {
        wsUtil.sendText(installContext.getRetSession(),"START_GEN_XML_CONFIG");
        String xml = generateClusterConfigXml(installContext);
        log.info("Generated xml information：{}", xml);
        wsUtil.sendText(installContext.getRetSession(),"END_GEN_XML_CONFIG");

        WsSession retSession = installContext.getRetSession();

        // Master node configuration information
        EnterpriseInstallNodeConfig masterNodeConfig = installContext.getEnterpriseInstallConfig().getNodeConfigList().stream().filter(nodeConfig -> nodeConfig.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Master node information not found"));
        String masterHostId = masterNodeConfig.getHostId();

        // root
        Session rootSession = loginWithUser(jschUtil,encryptionUtils, installContext.getHostInfoHolders(), true, masterHostId, null);

        String pkgPath = preparePath("/opt/software/openGauss");
        ensureDirExist(jschUtil,rootSession, pkgPath, retSession);
        chmodFullPath(jschUtil,rootSession, pkgPath, retSession);

        List<HostInfoHolder> hostInfoHolders = installContext.getHostInfoHolders();
        for (HostInfoHolder hostInfoHolder : hostInfoHolders) {
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            OpsHostUserEntity rootUser = hostInfoHolder.getHostUserEntities().stream().filter(hostUser -> "root".equals(hostUser.getUsername())).findFirst().orElseThrow(() -> new OpsException("root user information not found"));
            wsUtil.sendText(retSession,"host " + hostEntity.getPublicIp());
            Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootUser.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection"));

            try {
                ensureDirExist(jschUtil,session,"/opt/openGauss",retSession);
                chown(session,masterNodeConfig.getInstallUsername(),"/opt/openGauss",retSession);
                installDependency(jschUtil,session,retSession);
            }finally {
                if (session!=null && session.isConnected()){
                    session.disconnect();
                }
            }
        }

        // scp
        wsUtil.sendText(installContext.getRetSession(),"START_SCP_INSTALL_PACKAGE");
        String installPackageFullPath = scpInstallPackageToMasterNode(jschUtil,rootSession, installContext.getInstallPackagePath(), pkgPath, retSession);
        wsUtil.sendText(installContext.getRetSession(),"END_SCP_INSTALL_PACKAGE");

        // unzip install pack
        wsUtil.sendText(installContext.getRetSession(),"START_UNZIP_INSTALL_PACKAGE");
        decompress(jschUtil,rootSession, pkgPath, installPackageFullPath, retSession, "-xvf");
        // unzip CM
        decompress(jschUtil,rootSession, pkgPath, pkgPath + "/openGauss-3.0.0-openEuler-64bit-om.tar.gz", retSession, "-zxvf");
        wsUtil.sendText(installContext.getRetSession(),"END_UNZIP_INSTALL_PACKAGE");

        // write xml
        String xmlConfigFullPath = pkgPath + "/cluster_config.xml";
        String createClusterConfigXmlCommand = MessageFormat.format(SshCommandConstants.FILE_CREATE, xml, xmlConfigFullPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(createClusterConfigXmlCommand, rootSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to create xml configuration file, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create xml configuration file");
            }
        } catch (Exception e) {
            log.error("Failed to create xml configuration file：", e);
            throw new OpsException("Failed to create xml configuration file");
        }

        String group = null;
        String userGroupCommand = "groups " + masterNodeConfig.getInstallUsername() + " | awk -F ':' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(userGroupCommand, rootSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Querying user group failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query user group");
            }

            group = jschResult.getResult();
        } catch (Exception e) {
            log.error("Failed to query user group：", e);
            throw new OpsException("Failed to query user group");
        }

        HostInfoHolder masterHostHolder = hostInfoHolders.stream().filter(holder -> holder.getHostEntity().getHostId().equals(masterHostId)).findFirst().orElseThrow(() -> new OpsException("Master node host information not found"));
        OpsHostUserEntity masterRootUserInfo = masterHostHolder.getHostUserEntities().stream().filter(hostUser -> hostUser.getUsername().equals("root")).findFirst().orElseThrow(() -> new OpsException("The root user information of the primary node cannot be found"));
        OpsHostUserEntity installUserInfo = masterHostHolder.getHostUserEntities().stream().filter(hostUser -> hostUser.getHostUserId().equals(masterNodeConfig.getInstallUserId())).findFirst().orElseThrow(() -> new OpsException("No installation user information found"));

        wsUtil.sendText(installContext.getRetSession(),"START_EXE_PREINSTALL_COMMAND");
        preInstall(group, pkgPath, masterNodeConfig, xmlConfigFullPath, rootSession, retSession,encryptionUtils.decrypt(masterRootUserInfo.getPassword()),encryptionUtils.decrypt(installUserInfo.getPassword()));
        wsUtil.sendText(installContext.getRetSession(),"END_EXE_PREINSTALL_COMMAND");

        Session ommSession = loginWithUser(jschUtil,encryptionUtils,installContext.getHostInfoHolders(), false, masterHostId, masterNodeConfig.getInstallUserId());

        // install
        wsUtil.sendText(installContext.getRetSession(),"START_EXE_INSTALL_COMMAND");
        String installCommand = MessageFormat.format(SshCommandConstants.ENTERPRISE_INSTALL, xmlConfigFullPath);
        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            autoResponse.put("Please enter password for database:", installContext.getEnterpriseInstallConfig().getDatabasePassword());
            autoResponse.put("Please repeat for database:", installContext.getEnterpriseInstallConfig().getDatabasePassword());

            JschResult jschResult = jschUtil.executeCommand(installCommand, ommSession, retSession, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("install failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("install failed");
            }
        } catch (Exception e) {
            log.error("install failed：", e);
            throw new OpsException("install failed");
        }
        wsUtil.sendText(installContext.getRetSession(),"END_EXE_INSTALL_COMMAND");
    }

    private void chown(Session rootSession, String installUserName,String targetPath, WsSession wsSession) {
        String chown = MessageFormat.format(SshCommandConstants.CHOWN, installUserName, targetPath);

        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(chown, rootSession, wsSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to grant permission, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to grant permission");
            }
        } catch (IOException e) {
            log.error("Failed to grant permission", e);
            throw new OpsException("Failed to grant permission");
        }
    }

    public void upgrade(UpgradeContext upgradeContext){
        Session rootSession = null;
        Session ommSession = null;
        try {
            rootSession = jschUtil.getSession(upgradeContext.getHostPublicIp(), upgradeContext.getHostPort(), "root", encryptionUtils.decrypt(upgradeContext.getRootPassword())).orElseThrow(() -> new OpsException("The root user failed to establish a connection"));
            ommSession = jschUtil.getSession(upgradeContext.getHostPublicIp(), upgradeContext.getHostPort(), upgradeContext.getInstallUsername(), encryptionUtils.decrypt(upgradeContext.getInstallUserPassword())).orElseThrow(() -> new OpsException("Install user connection connection failed"));
            enableStreamReplication(rootSession,upgradeContext);
            checkClusterStatus(ommSession,upgradeContext);
            upgradePreinstall(rootSession,upgradeContext);
            doUpgrade(ommSession,upgradeContext);
            upgradeCheck(ommSession,upgradeContext);
            upgradeCommit(ommSession,upgradeContext);
            updateClusterVersion(upgradeContext);
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }

            if (Objects.nonNull(ommSession) && ommSession.isConnected()){
                ommSession.disconnect();
            }
        }
    }

    private void updateClusterVersion(UpgradeContext upgradeContext) {
        LambdaUpdateWrapper<OpsClusterEntity> updateWrapper = Wrappers.lambdaUpdate(OpsClusterEntity.class)
                .set(OpsClusterEntity::getVersionNum, "3.0.0")
                .eq(OpsClusterEntity::getClusterId, upgradeContext.getClusterEntity().getClusterId());
        opsClusterService.update(updateWrapper);
    }

    private void upgradeCommit(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "COMMIT_UPGRADE");
        try {
            String command = "gs_upgradectl -t commit-upgrade  -X "+upgradeContext.getClusterConfigXmlPath();
            JschResult jschResult = jschUtil.executeCommand(command, ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("upgradeCommit failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("upgradeCommit failed");
            }
        }catch (Exception e){
            log.error("upgradeCommit failed", e);
            throw new OpsException("upgradeCommit failed");
        }
    }

    private void upgradeCheck(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "UPGRADE_CHECK_CLUSTER_STATUS");
        try {
            String command = "gs_om -t status";
            JschResult jschResult = jschUtil.executeCommand(command, ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("upgradeCheck failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("upgradeCheck failed");
            }

            String result = jschResult.getResult();
            if (!result.contains("cluster_state") || !result.substring(result.indexOf("cluster_state"),result.indexOf("\n")).contains("Normal")){
                log.error("cluster status:{}",result);
                throw new OpsException("cluster status check fail");
            }
        }catch (Exception e){
            log.error("upgradeCheck failed", e);
            throw new OpsException("upgradeCheck failed");
        }
    }

    private void doUpgrade(Session ommSession, UpgradeContext upgradeContext) {
        try {
            String command = "gs_upgradectl -t auto-upgrade -X "+upgradeContext.getClusterConfigXmlPath();
            if (upgradeContext.getUpgradeType() == UpgradeTypeEnum.GRAY_UPGRADE){
                command = command + " --grey";
            }

            JschResult jschResult = jschUtil.executeCommand(command, ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("gs_upgradectl failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_upgradectl failed");
            }
        }catch (Exception e){
            log.error("gs_upgradectl failed", e);
            throw new OpsException("gs_upgradectl failed");
        }
    }

    private void upgradePreinstall(Session rootSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(),"PREINSTALL");

        String targetPath = "/opt/software/gaussdb_upgrade";

        String group = null;
        String userGroupCommand = "groups " + upgradeContext.getInstallUsername() + " | awk -F ':' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(userGroupCommand, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("Querying user group failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query user group");
            }

            group = jschResult.getResult();
        } catch (Exception e) {
            log.error("Failed to query user group：", e);
            throw new OpsException("Failed to query user group");
        }

        try {
            String command = "mkdir -p "+targetPath;
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("mkdir failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("mkdir failed");
            }
        }catch (Exception e){
            log.error("mkdir failed", e);
            throw new OpsException("mkdir failed");
        }

        try {
            jschUtil.upload(rootSession,upgradeContext.getRetSession(),upgradeContext.getUpgradePackagePath(),targetPath+"/"+ FileUtil.getName(upgradeContext.getUpgradePackagePath()));
        }catch (Exception e){
            log.error("upload failed", e);
            throw new OpsException("upload failed");
        }

        try {
            String command = "chown -R " + upgradeContext.getInstallUsername() + " " +targetPath;
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("chown failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("chown failed");
            }
        }catch (Exception e){
            log.error("chown failed", e);
            throw new OpsException("chown failed");
        }

        decompress(jschUtil, rootSession, targetPath, targetPath+"/"+FileUtil.getName(upgradeContext.getUpgradePackagePath()), upgradeContext.getRetSession(), "-xvf");
        decompress(jschUtil,rootSession, targetPath, targetPath + "/openGauss-3.0.0-openEuler-64bit-om.tar.gz", upgradeContext.getRetSession(), "-zxvf");

        try {
            String command = "cd "+ targetPath + "/script && ./gs_preinstall -U "+upgradeContext.getInstallUsername()+" -G "+group+"  -X "+ upgradeContext.getClusterConfigXmlPath() +" --non-interactive";
            if (StrUtil.isNotEmpty(upgradeContext.getSepEnvFile())){
                command = command + " --sep-env-file="+upgradeContext.getSepEnvFile();
            }

            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("gs_preinstall failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_preinstall failed");
            }
        }catch (Exception e){
            log.error("gs_preinstall failed", e);
            throw new OpsException("gs_preinstall failed");
        }
    }

    private void checkClusterStatus(Session ommSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(), "CHECK_CLUSTER_STATUS");
        try {
            String command = "gs_om -t status";
            JschResult jschResult = jschUtil.executeCommand(command, ommSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("checkClusterStatus failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("checkClusterStatus failed");
            }

            String result = jschResult.getResult();
            if (!result.contains("cluster_state") || !result.substring(result.indexOf("cluster_state"),result.indexOf("\n")).contains("Normal")){
                log.error("cluster status:{}",result);
                throw new OpsException("cluster status check fail");
            }
        }catch (Exception e){
            log.error("checkClusterStatus failed", e);
            throw new OpsException("checkClusterStatus failed");
        }
    }

    private void enableStreamReplication(Session rootSession, UpgradeContext upgradeContext) {
        wsUtil.sendText(upgradeContext.getRetSession(),"ENABLE_STREAM_REPLICATION");
        OpsClusterNodeEntity opsClusterNodeEntity = upgradeContext.getOpsClusterNodeEntity();
        try {
            String command = "gs_guc reload -D "+opsClusterNodeEntity.getDataPath()+" -c \"enable_stream_replication=on\"";
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        }catch (Exception e){
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }

        try {
            String command = "gs_guc check -D "+opsClusterNodeEntity.getDataPath()+" -c \"enable_stream_replication\"";
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        }catch (Exception e){
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }

        try {
            String command = "gs_guc reload -I all -c \"enable_stream_replication=on\"";
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        }catch (Exception e){
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }

        try {
            String command = "gs_guc check -I all -c \"enable_stream_replication\"";
            JschResult jschResult = jschUtil.executeCommand(command, rootSession, upgradeContext.getRetSession());
            if (0 != jschResult.getExitCode()) {
                log.error("enableStreamReplication failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("enableStreamReplication failed");
            }
        }catch (Exception e){
            log.error("enableStreamReplication failed", e);
            throw new OpsException("enableStreamReplication failed");
        }
    }

    private void preInstall1(String group, String pkgPath, EnterpriseInstallNodeConfig masterNodeConfig, String xmlConfigFullPath, Session rootSession, WsSession retSession, String rootPassword, String installUserPassword) {
        // gs_preinstall
        String gsPreInstall = MessageFormat.format(SshCommandConstants.GS_PREINSTALL, pkgPath + "/script/", masterNodeConfig.getInstallUsername(), group, xmlConfigFullPath);
        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?", "yes");
            autoResponse.put("Please enter password for root\r\nPassword:", rootPassword);
            autoResponse.put("Please enter password for current user[omm].\r\nPassword:", installUserPassword);
            JschResult jschResult = jschUtil.executeCommand(gsPreInstall, rootSession, retSession, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("gs_preinstall failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_preinstall failed");
            }
        } catch (Exception e) {
            log.error("gs_preinstall failed：", e);
            throw new OpsException("gs_preinstall failed");
        }
    }

    private void preInstall(String group, String pkgPath, EnterpriseInstallNodeConfig masterNodeConfig, String xmlConfigFullPath, Session rootSession, WsSession retSession, String rootPassword, String installUserPassword) {
        String gsPreInstall = MessageFormat.format(SshCommandConstants.GS_PREINSTALL_INTERACTIVE, pkgPath + "/script/", masterNodeConfig.getInstallUsername(), group, xmlConfigFullPath);
        try {
            Map<String,String> autoResponse = new HashMap<>();
            autoResponse.put("(yes/no)?","yes");
            autoResponse.put("Please enter password for root\r\nPassword:",rootPassword);
            autoResponse.put("Please enter password for current user["+masterNodeConfig.getInstallUsername()+"].\r\nPassword:",installUserPassword);
            JschResult jschResult = jschUtil.executeCommand(gsPreInstall, rootSession, retSession,autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("gs_preinstall failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("gs_preinstall failed");
            }
        } catch (Exception e) {
            log.error("gs_preinstall failed：", e);
            throw new OpsException("gs_preinstall failed");
        }
    }

    private void createRemoteUser(InstallContext installContext, OpsClusterContext opsClusterContext) {
        WsSession retSession = installContext.getRetSession();
        opsClusterContext.setRetSession(retSession);
        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());

        EnterpriseInstallConfig enterpriseInstallConfig = installContext.getEnterpriseInstallConfig();
        List<EnterpriseInstallNodeConfig> nodeConfigList = enterpriseInstallConfig.getNodeConfigList();
        String databasePassword = enterpriseInstallConfig.getDatabasePassword();
        Integer port = enterpriseInstallConfig.getPort();

        EnterpriseInstallNodeConfig enterpriseInstallNodeConfig = nodeConfigList.stream().filter(nodeConfig -> nodeConfig.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("master node not found"));

        String hostId = enterpriseInstallNodeConfig.getHostId();
        String installUserId = enterpriseInstallNodeConfig.getInstallUserId();
        String dataPath = enterpriseInstallNodeConfig.getDataPath();


        Session session = loginWithUser(jschUtil,encryptionUtils,installContext.getHostInfoHolders(), false, hostId, installUserId);
        String listenerAddress = MessageFormat.format(SshCommandConstants.LISTENER, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(listenerAddress, session, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to modify listening address, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to modify listening address");
            }

        } catch (Exception e) {
            log.error("Failed to modify listening address", e);
            throw new OpsException("Failed to modify listening address");
        }

        String hba = MessageFormat.format(SshCommandConstants.HBA, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(hba, session, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to modify host, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to modify host");
            }

        } catch (Exception e) {
            log.error("Failed to modify host", e);
            throw new OpsException("Failed to modify host");
        }

        String clientLoginOpenGauss = MessageFormat.format(SshCommandConstants.LOGIN, String.valueOf(port));
        try {
            Map<String, String> response = new HashMap<>();
            String createUser = MessageFormat.format("CREATE USER gaussdb WITH MONADMIN PASSWORD \"{0}\";\\q", databasePassword);
            response.put("openGauss=#", createUser);
            JschResult jschResult = jschUtil.executeCommand(clientLoginOpenGauss, session, retSession, response);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to create user, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create user");
            }

        } catch (Exception e) {
            log.error("Failed to create user", e);
            throw new OpsException("Failed to create user");
        }

        opsClusterContext.setHostInfoHolders(installContext.getHostInfoHolders());
        restart(opsClusterContext);
    }

    private OpsClusterContext saveContext(InstallContext installContext) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        OpsClusterEntity opsClusterEntity = installContext.toOpsClusterEntity();
        List<OpsClusterNodeEntity> opsClusterNodeEntities = installContext.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();

        opsClusterService.save(opsClusterEntity);
        for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
            opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
        }
        opsClusterNodeService.saveBatch(opsClusterNodeEntities);

        opsClusterContext.setOpsClusterEntity(opsClusterEntity);
        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntities);
        return opsClusterContext;
    }

    private String generateClusterConfigXml(InstallContext installContext) {
        String xmlString = "";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // Disallow the DTDs (doctypes) entirely.
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // Or do the following:
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            Element rootElement = document.createElement("ROOT");

            Element cluster = document.createElement("CLUSTER");
            appendClusterParam(document, cluster, installContext);

            Element deviceList = document.createElement("DEVICELIST");
            appendDeviceList(document, deviceList, installContext);

            rootElement.appendChild(cluster);
            rootElement.appendChild(deviceList);

            document.appendChild(rootElement);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            transformer.transform(domSource, new StreamResult(byteArrayOutputStream));
            xmlString = byteArrayOutputStream.toString();
        } catch (Exception e) {
            log.error("generate xml exception：", e);
            throw new OpsException("generate xml exception");
        }
        return xmlString;
    }

    private void appendDeviceList(Document document, Element deviceList, InstallContext installContext) {
        List<EnterpriseInstallNodeConfig> nodeConfigList = installContext.getEnterpriseInstallConfig().getNodeConfigList();
        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            Element device = document.createElement("DEVICE");
            device.setAttribute("sn", enterpriseInstallNodeConfig.getHostname());
            deviceList.appendChild(device);

            appendDeviceParam(document, device, enterpriseInstallNodeConfig, nodeConfigList, installContext.getEnterpriseInstallConfig());
        }
    }

    private void appendDeviceParam(Document document, Element device, EnterpriseInstallNodeConfig enterpriseInstallNodeConfig, List<EnterpriseInstallNodeConfig> nodeConfigList, EnterpriseInstallConfig enterpriseInstallConfig) {
        Element name = document.createElement("PARAM");
        name.setAttribute("name", "name");
        name.setAttribute("value", enterpriseInstallNodeConfig.getHostname());
        device.appendChild(name);

        Element azName = document.createElement("PARAM");
        azName.setAttribute("name", "azName");
        azName.setAttribute("value", enterpriseInstallConfig.getAzName());
        device.appendChild(azName);

        Element azPriority = document.createElement("PARAM");
        azPriority.setAttribute("name", "azPriority");
        azPriority.setAttribute("value", "1");
        device.appendChild(azPriority);

        Element backIp1 = document.createElement("PARAM");
        backIp1.setAttribute("name", "backIp1");
        backIp1.setAttribute("value", enterpriseInstallNodeConfig.getPrivateIp());
        device.appendChild(backIp1);

        if (enterpriseInstallNodeConfig.getIsInstallCM()) {
            if (enterpriseInstallNodeConfig.getIsCMMaster()) {
                Element cmsNum = document.createElement("PARAM");
                cmsNum.setAttribute("name", "cmsNum");
                cmsNum.setAttribute("value", "1");
                device.appendChild(cmsNum);

                Element cmServerPortBase = document.createElement("PARAM");
                cmServerPortBase.setAttribute("name", "cmServerPortBase");
                cmServerPortBase.setAttribute("value", String.valueOf(enterpriseInstallNodeConfig.getCmPort()));
                device.appendChild(cmServerPortBase);

                Element cmServerListenIp1 = document.createElement("PARAM");
                cmServerListenIp1.setAttribute("name", "cmServerListenIp1");
                Object[] ips = nodeConfigList.stream().filter(EnterpriseInstallNodeConfig::getIsInstallCM).map(EnterpriseInstallNodeConfig::getPrivateIp).toArray();
                cmServerListenIp1.setAttribute("value", StringUtils.arrayToCommaDelimitedString(ips));
                device.appendChild(cmServerListenIp1);

                Element cmServerHaIp1 = document.createElement("PARAM");
                cmServerHaIp1.setAttribute("name", "cmServerHaIp1");
                cmServerHaIp1.setAttribute("value", StringUtils.arrayToCommaDelimitedString(ips));
                device.appendChild(cmServerHaIp1);

                Element cmServerlevel = document.createElement("PARAM");
                cmServerlevel.setAttribute("name", "cmServerlevel");
                cmServerlevel.setAttribute("value", "1");
                device.appendChild(cmServerlevel);

                Element cmServerRelation = document.createElement("PARAM");
                cmServerRelation.setAttribute("name", "cmServerRelation");
                Object[] hostnames = nodeConfigList.stream().filter(EnterpriseInstallNodeConfig::getIsInstallCM).map(EnterpriseInstallNodeConfig::getHostname).toArray();
                cmServerRelation.setAttribute("value", StringUtils.arrayToCommaDelimitedString(hostnames));
                device.appendChild(cmServerRelation);

                Element cmDir = document.createElement("PARAM");
                cmDir.setAttribute("name", "cmDir");
                cmDir.setAttribute("value", enterpriseInstallNodeConfig.getCmDataPath());
                device.appendChild(cmDir);
            } else {
                Element cmDir = document.createElement("PARAM");
                cmDir.setAttribute("name", "cmDir");
                cmDir.setAttribute("value", enterpriseInstallNodeConfig.getCmDataPath());
                device.appendChild(cmDir);

                Element cmServerPortStandby = document.createElement("PARAM");
                cmServerPortStandby.setAttribute("name", "cmServerPortStandby");
                cmServerPortStandby.setAttribute("value", String.valueOf(enterpriseInstallNodeConfig.getCmPort()));
                device.appendChild(cmServerPortStandby);
            }


        }

        if (enterpriseInstallNodeConfig.getClusterRole() == ClusterRoleEnum.MASTER) {
            Element dataNum = document.createElement("PARAM");
            dataNum.setAttribute("name", "dataNum");
            dataNum.setAttribute("value", "1");
            device.appendChild(dataNum);

            Element dataPortBase = document.createElement("PARAM");
            dataPortBase.setAttribute("name", "dataPortBase");
            dataPortBase.setAttribute("value", String.valueOf(enterpriseInstallConfig.getPort()));
            device.appendChild(dataPortBase);

            Element dataNode1 = document.createElement("PARAM");
            dataNode1.setAttribute("name", "dataNode1");
            List<EnterpriseInstallNodeConfig> nodeConfigs = nodeConfigList.stream().filter(EnterpriseInstallNodeConfig::getIsInstallCM).collect(Collectors.toList());
            EnterpriseInstallNodeConfig master = nodeConfigs.stream().filter(EnterpriseInstallNodeConfig::getIsCMMaster).findFirst().orElseThrow(() -> new OpsException("CM master node information not found"));
            StringBuilder dataNodeValue = new StringBuilder(master.getDataPath());
            for (EnterpriseInstallNodeConfig nodeConfig : nodeConfigs) {
                if (!master.equals(nodeConfig)) {
                    dataNodeValue.append(",")
                            .append(nodeConfig.getHostname())
                            .append(",")
                            .append(nodeConfig.getDataPath());
                }

            }
            dataNode1.setAttribute("value", dataNodeValue.toString());
            device.appendChild(dataNode1);

            Element dataNode1_syncNum = document.createElement("PARAM");
            dataNode1_syncNum.setAttribute("name", "dataNode1_syncNum");
            dataNode1_syncNum.setAttribute("value", "0");
            device.appendChild(dataNode1_syncNum);
        }
    }

    private void appendClusterParam(Document document, Element cluster, InstallContext installContext) {
        Element clusterName = document.createElement("PARAM");
        clusterName.setAttribute("name", "clusterName");
        clusterName.setAttribute("value", installContext.getClusterName());
        cluster.appendChild(clusterName);

        Element nodeNames = document.createElement("PARAM");
        nodeNames.setAttribute("name", "nodeNames");
        Object[] hostNames = installContext.getEnterpriseInstallConfig().getNodeConfigList().stream().map(EnterpriseInstallNodeConfig::getHostname).toArray();
        nodeNames.setAttribute("value", StringUtils.arrayToCommaDelimitedString(hostNames));
        cluster.appendChild(nodeNames);

        Element gaussdbAppPath = document.createElement("PARAM");
        gaussdbAppPath.setAttribute("name", "gaussdbAppPath");
        gaussdbAppPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getInstallPath());
        cluster.appendChild(gaussdbAppPath);

        Element gaussdbLogPath = document.createElement("PARAM");
        gaussdbLogPath.setAttribute("name", "gaussdbLogPath");
        gaussdbLogPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getLogPath());
        cluster.appendChild(gaussdbLogPath);

        Element tmpMppdbPath = document.createElement("PARAM");
        tmpMppdbPath.setAttribute("name", "tmpMppdbPath");
        tmpMppdbPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getTmpPath());
        cluster.appendChild(tmpMppdbPath);

        Element gaussdbToolPath = document.createElement("PARAM");
        gaussdbToolPath.setAttribute("name", "gaussdbToolPath");
        gaussdbToolPath.setAttribute("value", installContext.getEnterpriseInstallConfig().getOmToolsPath());
        cluster.appendChild(gaussdbToolPath);

        Element corePath = document.createElement("PARAM");
        corePath.setAttribute("name", "corePath");
        corePath.setAttribute("value", installContext.getEnterpriseInstallConfig().getCorePath());
        cluster.appendChild(corePath);

        Element backIp1s = document.createElement("PARAM");
        backIp1s.setAttribute("name", "backIp1s");
        Object[] privateIps = installContext.getEnterpriseInstallConfig().getNodeConfigList().stream().map(EnterpriseInstallNodeConfig::getPrivateIp).toArray();
        backIp1s.setAttribute("value", StringUtils.arrayToCommaDelimitedString(privateIps));
        cluster.appendChild(backIp1s);

        if (installContext.getEnterpriseInstallConfig().getEnableDCF()) {
            Element enableDcf = document.createElement("PARAM");
            enableDcf.setAttribute("name", "enable_dcf");
            enableDcf.setAttribute("value", "on");
            cluster.appendChild(enableDcf);

            Element dcfConfig = document.createElement("PARAM");
            dcfConfig.setAttribute("name", "dcf_config");

            StringBuilder res = new StringBuilder("[");
            String template = "'{'\"stream_id\":1,\"node_id\":{0},\"ip\":\"{1}\",\"port\":{2},\"role\":\"{3}\"'}'";
            List<EnterpriseInstallNodeConfig> nodeConfigList = installContext.getEnterpriseInstallConfig().getNodeConfigList();
            for (int i = 0; i < nodeConfigList.size(); i++) {
                EnterpriseInstallNodeConfig enterpriseInstallNodeConfig = nodeConfigList.get(i);
                String format = MessageFormat.format(template, i + 1, enterpriseInstallNodeConfig.getPrivateIp(), String.valueOf(Objects.isNull(enterpriseInstallNodeConfig.getDcfPort())?16683:enterpriseInstallNodeConfig.getDcfPort()), enterpriseInstallNodeConfig.getIsCMMaster() ? "LEADER" : "FOLLOWER");
                res.append(format);
                res.append(",");
            }

            res.deleteCharAt(res.length() - 1);
            res.append("]");

            dcfConfig.setAttribute("value", res.toString());
            cluster.appendChild(dcfConfig);
        }
    }

    @Override
    public void uninstall(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        if (Objects.isNull(opsClusterEntity)) {
            throw new OpsException("Uninstall cluster does not exist");
        }

        WsSession retSession = unInstallContext.getRetSession();

        OpsClusterNodeEntity opsClusterNodeEntity = unInstallContext.getOpsClusterNodeEntityList().get(0);

        HostInfoHolder hostInfoHolder = unInstallContext.getHostInfoHolders().get(0);
        OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
        OpsHostUserEntity hostUserEntity = hostInfoHolder.getHostUserEntities().stream().filter(userInfo -> opsClusterNodeEntity.getInstallUserId().equals(userInfo.getHostUserId())).findFirst().orElseThrow(() -> new OpsException("No install user info user found"));
        Session session = sshLogin(jschUtil,encryptionUtils,hostEntity, hostUserEntity);

        String uninstallCommand = "gs_uninstall --delete-data";
        try {
            JschResult jschResult = jschUtil.executeCommand(uninstallCommand, session, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("Uninstall error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("Uninstall error", e);
            throw new OpsException("Uninstall error");
        }

        removeContext(unInstallContext);
    }

    private void removeContext(UnInstallContext unInstallContext) {
        OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
        opsClusterService.removeById(opsClusterEntity.getClusterId());

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();
        opsClusterNodeService.removeBatchByIds(opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList()));
    }

    @Override
    public void restart(OpsClusterContext opsClusterContext) {
        log.info("Start rebooting Enterprise Edition");
        List<String> restartNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(restartNodeIds)) {
            restartNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(restartNodeIds)) {
            log.error("No nodes to restart");
        }

        for (String restartNodeId : restartNodeIds) {
            doRestartNode(restartNodeId, opsClusterContext);
        }
    }

    private void doRestartNode(String restartNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity restartNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(restartNodeId)).findFirst().orElse(null);
        if (Objects.isNull(restartNodeEntity)) {
            log.error("No restart node information found:{}", restartNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = restartNodeEntity.getDataPath();
        log.info("login restart user");
        Session restartUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, restartNodeEntity.getHostId(), restartNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_RESTART, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                log.error("restart error，exit code{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("restart error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during the restart process", e);
            throw new OpsException("restart exception");
        }
    }

    @Override
    public void start(OpsClusterContext opsClusterContext) {
        log.info("Get started with Enterprise Edition");
        List<String> startNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(startNodeIds)) {
            startNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(startNodeIds)) {
            log.error("No node to start");
        }

        for (String startNodeId : startNodeIds) {
            doStartNode(startNodeId, opsClusterContext);
        }
    }

    private void doStartNode(String startNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity startNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(startNodeId)).findFirst().orElse(null);
        if (Objects.isNull(startNodeEntity)) {
            log.error("Boot node information not found:{}", startNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = startNodeEntity.getDataPath();
        log.info("Login to start user");
        Session restartUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, startNodeEntity.getHostId(), startNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_START, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("startup error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during startup", e);
            throw new OpsException("An exception occurred during startup");
        }
    }

    @Override
    public void stop(OpsClusterContext opsClusterContext) {
        log.info("Start Stop Enterprise Edition");
        List<String> stopNodeIds = opsClusterContext.getOpNodeIds();
        if (CollUtil.isEmpty(stopNodeIds)) {
            stopNodeIds = opsClusterContext.getOpsClusterNodeEntityList().stream().map(OpsClusterNodeEntity::getClusterNodeId).collect(Collectors.toList());
        }

        if (CollUtil.isEmpty(stopNodeIds)) {
            log.error("no node to stop");
        }

        for (String stopNodeId : stopNodeIds) {
            doStopNode(stopNodeId, opsClusterContext);
        }
    }

    @Override
    public void enableWdrSnapshot(Session session, OpsClusterEntity clusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntities, WdrScopeEnum scope, String dataPath) {
        String command;
        if (StrUtil.isEmpty(dataPath)) {
            command = "gs_guc reload -I all -c \"enable_wdr_snapshot=on\"";
        } else {
            command = "gs_guc reload -D " + dataPath + " -c \"enable_wdr_snapshot=on\"";
        }

        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("set enable_wdr_snapshot parameter failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
            }

        } catch (Exception e) {
            log.error("Failed to set the enable_wdr_snapshot parameter", e);
            throw new OpsException("Failed to set the enable_wdr_snapshot parameter");
        }
    }

    private void doStopNode(String stopNodeId, OpsClusterContext opsClusterContext) {
        OpsClusterNodeEntity stopNodeEntity = opsClusterContext.getOpsClusterNodeEntityList().stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterNodeId().equals(stopNodeId)).findFirst().orElse(null);
        if (Objects.isNull(stopNodeEntity)) {
            log.error("No stop node information found:{}", stopNodeId);
        }

        WsSession retSession = opsClusterContext.getRetSession();
        String dataPath = stopNodeEntity.getDataPath();
        log.info("login stop user");
        Session restartUserSession = loginWithUser(jschUtil,encryptionUtils,opsClusterContext.getHostInfoHolders(), false, stopNodeEntity.getHostId(), stopNodeEntity.getInstallUserId());

        String restartCommand = MessageFormat.format(SshCommandConstants.LITE_STOP, dataPath);
        try {
            JschResult jschResult = jschUtil.executeCommand(restartCommand, restartUserSession, retSession);
            if (0 != jschResult.getExitCode()) {
                throw new OpsException("stop error，exit code " + jschResult.getExitCode());
            }
        } catch (Exception e) {
            log.error("An exception occurred during the stop process", e);
            throw new OpsException("stop exception");
        }
    }
}
