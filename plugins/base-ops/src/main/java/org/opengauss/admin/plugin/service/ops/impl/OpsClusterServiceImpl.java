/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * -------------------------------------------------------------------------
 *
 * OpsClusterServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.entity.SysSettingEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.PermissionUtils;
import org.opengauss.admin.plugin.domain.entity.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.cache.SSHChannelManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.env.EnvProperty;
import org.opengauss.admin.plugin.domain.model.ops.env.HardwareEnv;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.domain.model.ops.env.SoftwareEnv;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.*;
import org.opengauss.admin.plugin.listener.OpsImportEntityListener;
import org.opengauss.admin.plugin.mapper.ops.OpsClusterMapper;
import org.opengauss.admin.plugin.mapper.ops.OpsDisasterClusterMapper;
import org.opengauss.admin.plugin.mapper.ops.OpsImportSshMapper;
import org.opengauss.admin.plugin.service.ops.IOpsCheckService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerService;
import org.opengauss.admin.plugin.service.ops.impl.provider.EnterpriseOpsProvider;
import org.opengauss.admin.plugin.utils.DBUtil;
import org.opengauss.admin.plugin.utils.DownloadUtil;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.plugin.vo.ops.*;
import org.opengauss.admin.plugin.vo.ops.SessionVO;
import org.opengauss.admin.plugin.vo.ops.SlowSqlVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.opengauss.admin.system.plugin.facade.SysSettingFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants.*;
import static org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum.MASTER;
import static org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum.SLAVE;
import static org.opengauss.admin.plugin.enums.ops.DeployTypeEnum.CLUSTER;
import static org.opengauss.admin.plugin.enums.ops.DeployTypeEnum.SINGLE_NODE;
import static org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum.*;

/**
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@Slf4j
@Service
public class OpsClusterServiceImpl extends ServiceImpl<OpsClusterMapper, OpsClusterEntity> implements IOpsClusterService {
    private static String[] dependencyPackageNames = {"libaio-devel", "flex", "bison", "ncurses-devel", "glibc-devel",
            "patch", "readline-devel"};
    private static final Logger log = LoggerFactory.getLogger(OpsClusterServiceImpl.class);

    private int importSuccessCount;

    private boolean infoAndConn;

    private boolean masterIsNormal;

    private OpsClusterEntity opsClusterEntity = new OpsClusterEntity();

    private List<OpsClusterEntity> opsClusterEntityList = new ArrayList<>();

    private List<OpsClusterNodeEntity> opsClusterNodeEntityList = new ArrayList<>();

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private OpsFacade opsFacade;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;
    @Autowired
    private IOpsCheckService opsCheckService;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private DownloadUtil downloadUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private DBUtil DBUtil;
    @Autowired
    private SSHChannelManager sshChannelManager;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private ClusterOpsProviderManager clusterOpsProviderManager;
    @Autowired
    private EnterpriseOpsProvider enterpriseOpsProvider;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private SysSettingFacade sysSettingFacade;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private IOpsPackageManagerService pkgManagerService;

    @Autowired
    private OpsDisasterClusterMapper disasterClusterMapper;

    @Autowired
    private OpsImportSshMapper opsImportSshMapper;

    @Override
    public void download(DownloadBody downloadBody) {
        WsSession wsSession = wsConnectorManager.getSession(downloadBody.getBusinessId()).orElseThrow(() -> new OpsException("No output websocket session found"));

        Future<?> future = threadPoolTaskExecutor.submit(() -> downloadUtil.download(downloadBody.getResourceUrl(), downloadBody.getTargetPath(), downloadBody.getFileName(), wsSession));
        TaskManager.registry(downloadBody.getBusinessId(), future);
    }

    @Override
    public void install(InstallBody installBody) {
        log.info("install:{}", JSON.toJSONString(installBody));

        InstallContext installContext = installBody.getInstallContext();
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        installContext.setRetSession(wsSession);
        populateHostInfo(installContext);
        installContext.setOs(checkOS(installBody.getInstallContext().getHostInfoHolders(), true));

        if (Boolean.TRUE.equals(installBody.getQuickInstall())) {
            downloadInstallPackage(installBody);
        }

        checkInstallConfig(installContext);
        InstallContext clone = ObjectUtil.clone(installContext);
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doInstall(clone);
        });
        TaskManager.registry(installBody.getBusinessId(), future);
    }

    @Override
    public UpgradeOsCheckVO upgradeOsCheck(String clusterId, String rootPassword) {
        OpsClusterEntity clusterEntity = getById(clusterId);

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information cannot be empty");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Masternode information not found"));
        String hostId = nodeEntity.getHostId();
        String installUserId = nodeEntity.getInstallUserId();

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(rootPassword)){
            throw new OpsException("root password cannot be empty");
        }

        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            rootPassword = rootUserEntity.getPassword();
        }

        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootPassword)).orElseThrow(() -> new OpsException("with the host[" + hostEntity.getPublicIp() + "]Failed to establish session"));
        String checkOsRes = upgradeCheckOs(rootSession,hostUserEntity.getUsername());
        String diskInfo = getDiskInfo(rootSession);

        UpgradeOsCheckVO upgradeOsCheckVO = new UpgradeOsCheckVO();
        upgradeOsCheckVO.setOsInfo(checkOsRes);
        upgradeOsCheckVO.setDiskUsed(calcDiskUsed(diskInfo, clusterEntity.getInstallPath()));
        return upgradeOsCheckVO;
    }

    private String getDiskInfo(Session session) {
        String command = "df -h";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("Query disk fail, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Query disk size failed");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query disk size", e);
            throw new OpsException("Failed to query disk size");
        }
    }

    private String upgradeCheckOs(Session session, String installUsername) {
        String command = "cd /root/gauss_om/"+installUsername+"/script/ && ./gs_checkos -i A";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, session);
            if (0 != jschResult.getExitCode()) {
                log.error("checkos fail, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("checkos failed");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to checkos", e);
            return StrUtil.EMPTY;
        }
    }

    private OpenGaussSupportOSEnum checkOS(List<HostInfoHolder> hostInfoHolderList, boolean root) {
        Set<String> os = new HashSet<>();
        Set<String> osVersion = new HashSet<>();
        Set<String> cpuArch = new HashSet<>();
        for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
            OpsHostEntity hostEntity = hostInfoHolder.getHostEntity();
            OpsHostUserEntity installUserEntity;
            if (root) {
                installUserEntity = hostInfoHolder.getHostUserEntities().stream().filter(user -> user.getUsername().equalsIgnoreCase("root")).findFirst().orElseThrow(() -> new OpsException("Host [" + hostEntity.getPublicIp() + "] root user information not found"));
            } else {
                installUserEntity = hostInfoHolder.getHostUserEntities().stream().filter(user -> !user.getUsername().equalsIgnoreCase("root")).findFirst().orElseThrow(() -> new OpsException("Host [" + hostEntity.getPublicIp() + "] root user information not found"));
            }
            Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish a connection with the target host [ " + hostEntity.getPublicIp() + " ]"));

            os.add(getOS(rootSession));
            osVersion.add(getOSVersion(rootSession));
            cpuArch.add(getCpuArch(rootSession));

            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }
        }

        if (os.size() > 1) {
            throw new OpsException("The system information of multiple hosts is inconsistent");
        }

        if (osVersion.size() > 1) {
            throw new OpsException("System version information is inconsistent");
        }

        if (cpuArch.size() > 1) {
            throw new OpsException("CPU architecture information is inconsistent");
        }

        if (os.size() < 1) {
            throw new OpsException("System information not detected");
        }

        if (osVersion.size() < 1) {
            throw new OpsException("System version information not detected");
        }

        if (cpuArch.size() < 1) {
            throw new OpsException("No cpu architecture information detected");
        }

        String osInfo = null;
        for (String osInfo0 : os) {
            osInfo = osInfo0;
            break;
        }

        String osVersionInfo = null;
        for (String osVersionInfo0 : osVersion) {
            osVersionInfo = osVersionInfo0;
            break;
        }

        String cpuArchInfo = null;
        for (String cpuArchInfo0 : cpuArch) {
            cpuArchInfo = cpuArchInfo0;
            break;
        }

        OpenGaussSupportOSEnum osInfoEnum = OpenGaussSupportOSEnum.of(osInfo, osVersionInfo, cpuArchInfo);
        if (osInfoEnum == null) {
            throw new OpsException("unsupported operating system，system id " + osInfo + ",system version id" + osVersionInfo + ", cpu Architecture " + cpuArchInfo);
        }

        return osInfoEnum;
    }

    private String getCpuArch(Session rootSession) {
        String command = "lscpu | grep Architecture: | head -n 1 | awk -F ':' '{print $2}'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to get cpu architecture information,exitCode:{},res:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get cpu architecture information");
            }

            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to get cpu architecture information", e);
            throw new OpsException("Failed to get cpu architecture information");
        }
    }

    private String getOSVersion(Session rootSession) {
        String command = "cat /etc/os-release | grep VERSION_ID= | head -n 1 | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to get system version information,exitCode:{},res:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get system version information");
            }

            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to get system version information", e);
            throw new OpsException("Failed to get system version information");
        }
    }

    private String getOS(Session rootSession) {
        String command = "cat /etc/os-release | grep -oP '^ID=.*' | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Failed to get system information,exitCode:{},res:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get system information");
            }

            return jschResult.getResult().trim();
        } catch (Exception e) {
            log.error("Failed to get system information", e);
            throw new OpsException("Failed to get system information");
        }
    }

    @Override
    public void upgrade(UpgradeBody upgradeBody) {
        log.info("upgrade:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        if (clusterEntity.getVersion() != OpenGaussVersionEnum.ENTERPRISE) {
            throw new OpsException("Only supports Enterprise Edition upgrades");
        }

        if (Objects.isNull(upgradeBody.getUpgradeType())) {
            throw new OpsException("Please select an upgrade type");
        }

        if (StrUtil.isEmpty(upgradeBody.getUpgradePackagePath()) || !FileUtil.exist(upgradeBody.getUpgradePackagePath())) {
            throw new OpsException("Upgrade package not found");
        }


        WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }

        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }

        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(upgradeBody.getHostRootPassword())){
            throw new OpsException("root password cannot be empty");
        }

        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            upgradeBody.setHostRootPassword(rootUserEntity.getPassword());
        }

        UpgradeContext upgradeContext = new UpgradeContext();
        upgradeContext.setVersionNum(upgradeBody.getVersionNum());
        upgradeContext.setHostPublicIp(hostEntity.getPublicIp());
        upgradeContext.setRootPassword(upgradeBody.getHostRootPassword());
        upgradeContext.setRetSession(wsSession);
        upgradeContext.setClusterConfigXmlPath(clusterEntity.getXmlConfigPath());
        upgradeContext.setUpgradeType(upgradeBody.getUpgradeType());
        upgradeContext.setUpgradePackagePath(upgradeBody.getUpgradePackagePath());
        upgradeContext.setSepEnvFile(clusterEntity.getEnvPath());
        upgradeContext.setInstallUsername(installUserEntity.getUsername());
        upgradeContext.setInstallUserPassword(installUserEntity.getPassword());
        upgradeContext.setHostPort(hostEntity.getPort());
        upgradeContext.setClusterEntity(clusterEntity);
        upgradeContext.setOpsClusterNodeEntity(opsClusterNodeEntity);

        HostInfoHolder hostInfoHolder = new HostInfoHolder();
        hostInfoHolder.setHostEntity(hostEntity);
        hostInfoHolder.setHostUserEntities(Arrays.asList(rootUserEntity));
        upgradeContext.setOs(checkOS(Arrays.asList(hostInfoHolder), true));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doUpgrade(upgradeContext);
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    @Override
    public void upgradeCommit(UpgradeBody upgradeBody) {
        log.info("upgradeCommit:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }

        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }

        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(upgradeBody.getHostRootPassword())){
            throw new OpsException("root password cannot be empty");
        }

        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            upgradeBody.setHostRootPassword(rootUserEntity.getPassword());
        }

        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Install user connection failed"));

        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doUpgradeCommit(ommSession, wsSession, clusterEntity.getEnvPath(), clusterEntity.getXmlConfigPath(),clusterEntity.getClusterId(),upgradeBody.getVersionNum());

            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    private void doUpgradeCommit(Session ommSession, WsSession wsSession, String envPath, String clusterConfigXmlPath, String clusterId, String versionNum) {
        wsUtil.sendText(wsSession, "COMMIT_UPGRADE");
        try {
            String command = "gs_upgradectl -t commit-upgrade  -X " + clusterConfigXmlPath;
            JschResult jschResult = jschUtil.executeCommand(envPath, command, ommSession, wsSession, false);
            if (5 != jschResult.getExitCode()) {
                log.error("upgradeCommit failed, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("upgradeCommit failed");
            }

            LambdaUpdateWrapper<OpsClusterEntity> updateWrapper = Wrappers.lambdaUpdate(OpsClusterEntity.class)
                    .set(OpsClusterEntity::getVersionNum, versionNum)
                    .eq(OpsClusterEntity::getClusterId, clusterId);
            update(updateWrapper);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("upgradeCommit failed", e);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            throw new OpsException("upgradeCommit failed");
        }
    }

    @Override
    public void upgradeRollback(UpgradeBody upgradeBody) {
        log.info("upgradeRollback:{}", JSON.toJSONString(upgradeBody));
        OpsClusterEntity clusterEntity = getById(upgradeBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        WsSession wsSession = wsConnectorManager.getSession(upgradeBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(upgradeBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("No cluster node information found");
        }

        OpsClusterNodeEntity opsClusterNodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("The node information corresponding to the host is not found"));
        String installUserId = opsClusterNodeEntity.getInstallUserId();
        OpsHostUserEntity installUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user information not found");
        }

        OpsHostEntity hostEntity = hostFacade.getById(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information not found");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(opsClusterNodeEntity.getHostId());
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("User root was not found");
        }

        if (StrUtil.isEmpty(rootUserEntity.getPassword()) && StrUtil.isEmpty(upgradeBody.getHostRootPassword())){
            throw new OpsException("root password cannot be empty");
        }

        if (StrUtil.isNotEmpty(rootUserEntity.getPassword())) {
            upgradeBody.setHostRootPassword(rootUserEntity.getPassword());
        }

        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Install user connection failed"));

        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);
            doUpgradeRollback(ommSession, wsSession,clusterEntity.getEnvPath(), clusterEntity.getXmlConfigPath());

            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        });
        TaskManager.registry(upgradeBody.getBusinessId(), future);
    }

    private void doUpgradeRollback(Session ommSession, WsSession wsSession, String envPath, String clusterConfigXmlPath) {
        try {
            String command = "gs_upgradectl -t auto-rollback  -X " + clusterConfigXmlPath + " --force";
            JschResult jschResult = jschUtil.executeCommand(envPath, command, ommSession, wsSession, false);
            if (2 != jschResult.getExitCode()) {
                log.error("Failed to create xml configuration file, exit code: {}, error message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create xml configuration file");
            }
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Failed to create xml configuration file：", e);
            wsUtil.sendText(wsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        } finally {
            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }
        }
    }

    private void doUpgrade(UpgradeContext upgradeContext) {
        try {
            clusterOpsProviderManager
                    .provider(OpenGaussVersionEnum.ENTERPRISE)
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .upgrade(upgradeContext);
            wsUtil.sendText(upgradeContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Upgrade fail：", e);
            wsUtil.sendText(upgradeContext.getRetSession(), e.getMessage());
            wsUtil.sendText(upgradeContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void quickInstall(InstallBody installBody) {
        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId()).orElseThrow(() -> new OpsException("No output websocket session found"));
        wsUtil.sendText(wsSession, "START");
        installBody.setQuickInstall(Boolean.TRUE);
        if (StrUtil.isEmpty(installBody.getQuickInstallResourceUrl())) {
            throw new OpsException("installation package path cannot be empty");
        }
        List<MinimalistInstallNodeConfig> nodeConfigList = installBody.getInstallContext().getMinimalistInstallConfig().getNodeConfigList();
        if (CollUtil.isNotEmpty(nodeConfigList)) {
            MinimalistInstallNodeConfig minimalistInstallNodeConfig = nodeConfigList.get(0);
            String installUserId = minimalistInstallNodeConfig.getInstallUserId();
            if (StrUtil.isEmpty(installUserId)) {
                List<OpsHostUserEntity> opsHostUserEntities = hostUserFacade.listHostUserByHostId(minimalistInstallNodeConfig.getHostId());
                if (CollUtil.isNotEmpty(opsHostUserEntities)) {
                    OpsHostUserEntity opsHostUserEntity = opsHostUserEntities.stream().filter(user -> !"root".equals(user.getUsername())).findFirst().orElse(null);
                    if (Objects.nonNull(opsHostUserEntity)) {
                        minimalistInstallNodeConfig.setInstallUserId(opsHostUserEntity.getHostUserId());
                    }
                }
            }
        }
        install(installBody);
    }

    private void downloadInstallPackage(InstallBody installBody) {
        String installPackagePath = installBody.getInstallContext().getInstallPackagePath();

        String resourceUrl = installBody.getQuickInstallResourceUrl();
        String fileName = resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1);

        WsSession wsSession = wsConnectorManager.getSession(installBody.getBusinessId()).orElseThrow(() -> new OpsException("No output websocket session found"));
        downloadUtil.download(resourceUrl, installPackagePath, fileName, wsSession);

        installBody.getInstallContext().setInstallPackagePath(installPackagePath + "/" + fileName);
    }

    private void populateHostInfo(InstallContext installContext) {
        int hostLen;
        List<String> hostIdList;
        Map<String, String> hostPasswdMap = new HashMap<>();
        OpenGaussVersionEnum openGaussVersion = installContext.getOpenGaussVersion();
        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            List<EnterpriseInstallNodeConfig> nodeConfigList = installContext.getEnterpriseInstallConfig().getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream().map(EnterpriseInstallNodeConfig::getHostId).collect(Collectors.toList());
            for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(enterpriseInstallNodeConfig.getHostId(), enterpriseInstallNodeConfig.getRootPassword());
            }
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            List<MinimalistInstallNodeConfig> nodeConfigList = installContext.getMinimalistInstallConfig().getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream().map(MinimalistInstallNodeConfig::getHostId).collect(Collectors.toList());
            for (MinimalistInstallNodeConfig minimalistInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(minimalistInstallNodeConfig.getHostId(), minimalistInstallNodeConfig.getRootPassword());
            }
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            List<LiteInstallNodeConfig> nodeConfigList = installContext.getLiteInstallConfig().getNodeConfigList();
            hostLen = nodeConfigList.size();
            hostIdList = nodeConfigList.stream().map(LiteInstallNodeConfig::getHostId).collect(Collectors.toList());
            for (LiteInstallNodeConfig liteInstallNodeConfig : nodeConfigList) {
                hostPasswdMap.put(liteInstallNodeConfig.getHostId(), liteInstallNodeConfig.getRootPassword());
            }
        } else {
            throw new OpsException("unsupported version：[" + installContext.getOpenGaussVersion() + "]");
        }

        if (CollUtil.isNotEmpty(hostIdList)) {
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);
            if (CollUtil.isEmpty(opsHostEntities)) {
                log.info("Host information not found,hostIds{}", hostIdList);
                throw new OpsException("Host information not found");
            }
            if (opsHostEntities.size() != hostLen) {
                String errMsg = "Cannot select the same host as the installation target";
                log.info(errMsg);
                throw new OpsException(errMsg);
            }

            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);

            List<HostInfoHolder> hostInfoHolderList = opsHostEntities
                    .stream()
                    .map(host -> new HostInfoHolder(host, hostUserEntities
                            .stream()
                            .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                OpsHostUserEntity rootUserEntity = userEntities.stream().filter(userEntity -> "root".equals(userEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("Host not found[" + hostInfoHolder.getHostEntity().getPublicIp() + "]root user information"));
                if (StrUtil.isEmpty(rootUserEntity.getPassword())) {
                    if (StrUtil.isNotEmpty(hostPasswdMap.get(rootUserEntity.getHostId()))) {
                        rootUserEntity.setPassword(hostPasswdMap.get(rootUserEntity.getHostId()));
                    } else {
                        throw new OpsException("root password does not exist");
                    }
                }
            }

            installContext.setHostInfoHolders(hostInfoHolderList);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }
    }

    private void checkInstallConfig(InstallContext installContext) {
        String clusterId = installContext.getClusterId();

        clusterIdUniqueCheck(clusterId);

        installContext.checkConfig();
    }

    private void clusterIdUniqueCheck(String clusterId) {
        if (StrUtil.isEmpty(clusterId)) {
            throw new OpsException("Cluster ID error");
        }

        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.nonNull(clusterEntity)) {
            throw new OpsException("Cluster ID already exists");
        }
    }

    private void doInstall(InstallContext installContext) {
        try {
            clusterOpsProviderManager
                    .provider(installContext.getOpenGaussVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .install(installContext);
            wsUtil.sendText(installContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("Installation exception：", e);
            wsUtil.sendText(installContext.getRetSession(), e.getMessage());
            wsUtil.sendText(installContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void ssh(SSHBody sshBody) {
        WsSession wsSession = wsConnectorManager.getSession(sshBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));

        OpsHostEntity opsHostEntity = hostFacade.getById(sshBody.getHostId());
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());
        OpsHostUserEntity rootUserEntity = hostUserList.stream().filter(opsHostUserEntity -> "root".equalsIgnoreCase(opsHostUserEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + opsHostEntity.getHostname() + "]root user not found"));
        if (StrUtil.isEmpty(rootUserEntity.getPassword())) {
            if (StrUtil.isNotEmpty(sshBody.getRootPassword())) {
                rootUserEntity.setPassword(sshBody.getRootPassword());
            } else {
                throw new OpsException("root password not found");
            }
        }


        ChannelShell channelShell = sshChannelManager.initChannelShell(sshBody, opsHostEntity, rootUserEntity).orElseThrow(() -> new OpsException("Connection establishment failed"));

        Future<?> future = threadPoolTaskExecutor.submit(() -> jschUtil.channelToWsSession(channelShell, wsSession));

        TaskManager.registry(sshBody.getBusinessId(), future);
    }

    @Override
    public List<HostFile> ls(String hostId, String path) {
        OpsHostEntity opsHostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());
        OpsHostUserEntity installUserEntity = hostUserList.stream().filter(opsHostUserEntity -> !"root".equalsIgnoreCase(opsHostUserEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + opsHostEntity.getHostname() + "]root user not found"));

        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("root user failed to establish connection"));

        List<HostFile> ls = jschUtil.ls(session, path);

        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }

        return ls;
    }

    @Override
    public OpsNodeLogVO logPath(String clusterId, String hostId) {
        OpsNodeLogVO opsNodeLogVO = new OpsNodeLogVO();
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }

        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        if (OpenGaussVersionEnum.ENTERPRISE == clusterEntity.getVersion()) {
            opsNodeLogVO.setSystemLogPath(clusterEntity.getLogPath());
            opsNodeLogVO.setDumpLogPath(clusterEntity.getCorePath());

            Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElse(null);
            try {
                String command = "echo $GAUSSLOG";
                JschResult jschResult = jschUtil.executeCommand(command, session);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get log path, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get log path");
                }

                String result = jschResult.getResult();
                if (StrUtil.isNotEmpty(result)) {
                    opsNodeLogVO.setOpLogPath(result);
                    opsNodeLogVO.setPerformanceLogPath(result + "/gs_profile");
                }
            } catch (Exception e) {
                log.error("Failed to get operation log path", e);
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }
        } else {
            String dataPath = clusterNode.getDataPath();
            if (OpenGaussVersionEnum.MINIMAL_LIST == clusterEntity.getVersion()) {
                if (DeployTypeEnum.CLUSTER == clusterEntity.getDeployType()) {
                    ClusterRoleEnum clusterRole = clusterNode.getClusterRole();
                    if (clusterRole == ClusterRoleEnum.MASTER) {
                        dataPath = dataPath + "/master";
                    } else {
                        dataPath = dataPath + "/slave";
                    }
                } else {
                    dataPath = dataPath + "/single_node";
                }
            }
            opsNodeLogVO.setSystemLogPath(dataPath + "/pg_log");

            Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElse(null);
            try {
                String command = "echo $GAUSSLOG";
                JschResult jschResult = jschUtil.executeCommand(command, session);
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get log path, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get log path");
                }

                String result = jschResult.getResult();
                if (StrUtil.isNotEmpty(result)) {
                    opsNodeLogVO.setOpLogPath(result);
                    opsNodeLogVO.setPerformanceLogPath(result);
                    opsNodeLogVO.setDumpLogPath(result);
                }
            } catch (Exception e) {
                log.error("Failed to get operation log path", e);
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }
        }

        return opsNodeLogVO;
    }

    @Override
    public List<AuditLogVO> auditLog(Page page, String clusterId, String hostId, String start, String end) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }

        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), clusterEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySession(connection, start, end);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<SessionVO> listSession(String clusterId, String hostId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }

        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }

        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), clusterEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySession(connection);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }
        return Collections.emptyList();
    }

    private List<SessionVO> querySession(Connection connection) {
        List<SessionVO> res = new ArrayList<>();
        String sql = "SELECT * FROM pg_stat_activity";

        try (Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                SessionVO sessionVO = new SessionVO();
                sessionVO.setDatid(resultSet.getString("datid"));
                sessionVO.setDatname(resultSet.getString("datname"));
                sessionVO.setPid(resultSet.getString("pid"));
                sessionVO.setSessionid(resultSet.getString("sessionid"));
                sessionVO.setUsesysid(resultSet.getString("usesysid"));
                sessionVO.setUsename(resultSet.getString("usename"));
                sessionVO.setApplication_name(resultSet.getString("application_name"));
                sessionVO.setClient_addr(resultSet.getString("client_addr"));
                sessionVO.setClient_hostname(resultSet.getString("client_hostname"));
                sessionVO.setClient_port(resultSet.getString("client_port"));
                sessionVO.setBackend_start(resultSet.getString("backend_start"));
                sessionVO.setXact_start(resultSet.getString("xact_start"));
                sessionVO.setQuery_start(resultSet.getString("query_start"));
                sessionVO.setState_change(resultSet.getString("state_change"));
                sessionVO.setWaiting(resultSet.getString("waiting"));
                sessionVO.setEnqueue(resultSet.getString("enqueue"));
                sessionVO.setState(resultSet.getString("state"));
                sessionVO.setResource_pool(resultSet.getString("resource_pool"));
                sessionVO.setQuery_id(resultSet.getString("query_id"));
                sessionVO.setQuery(resultSet.getString("query"));
                sessionVO.setConnection_info(resultSet.getString("connection_info"));
                sessionVO.setUnique_sql_id(resultSet.getString("unique_sql_id"));
                sessionVO.setTrace_id(resultSet.getString("trace_id"));
                res.add(sessionVO);
            }
        } catch (Exception e) {
            log.error("Querying the Audit Log Exception", e);
        }

        return res;
    }

    @Override
    public List<SlowSqlVO> slowSql(Page page, String clusterId, String hostId, String start, String end) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("The current cluster node was not found on the host");
        }

        OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("The current cluster node was not found on the host"));
        String installUserId = clusterNode.getInstallUserId();

        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), clusterEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("Failed to establish connection to database"));
            return querySlowSql(connection, start, end);
        } catch (Exception e) {
            log.error("get connection fail");
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }

        return Collections.emptyList();
    }

    private List<SlowSqlVO> querySlowSql(Connection connection, String start, String end) {
        List<SlowSqlVO> res = new ArrayList<>();
        String sql = "select * from DBE_PERF.get_global_full_sql_by_timestamp('"+start+"', '"+end+"')";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SlowSqlVO slowSqlVO = new SlowSqlVO();
                slowSqlVO.setNode_name(resultSet.getString("node_name"));
                slowSqlVO.setDb_name(resultSet.getString("db_name"));
                slowSqlVO.setStart_time(resultSet.getString("start_time"));
                slowSqlVO.setFinish_time(resultSet.getString("finish_time"));
                slowSqlVO.setSlow_sql_threshold(resultSet.getString("slow_sql_threshold"));
                slowSqlVO.setQuery(resultSet.getString("query"));
                slowSqlVO.setQuery_plan(resultSet.getString("query_plan"));
                res.add(slowSqlVO);
            }
        } catch (Exception e) {
            log.error("Querying the Audit Log Exception", e);
        }
        return res;
    }

    private List<AuditLogVO> querySession(Connection connection, String start, String end) {
        List<AuditLogVO> res = new ArrayList<>();
        String sql = "select * from pg_query_audit('" + start + "','" + end + "')";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                AuditLogVO auditLogVO = new AuditLogVO();
                auditLogVO.setTime(resultSet.getString("time"));
                auditLogVO.setType(resultSet.getString("type"));
                auditLogVO.setResult(resultSet.getString("result"));
                auditLogVO.setUserid(resultSet.getString("userid"));
                auditLogVO.setUsername(resultSet.getString("username"));
                auditLogVO.setDatabase(resultSet.getString("database"));
                auditLogVO.setClient_conninfo(resultSet.getString("client_conninfo"));
                auditLogVO.setObject_name(resultSet.getString("object_name"));
                auditLogVO.setDetail_info(resultSet.getString("detail_info"));
                auditLogVO.setNode_name(resultSet.getString("node_name"));
                auditLogVO.setThread_id(resultSet.getString("thread_id"));
                auditLogVO.setLocal_port(resultSet.getString("local_port"));
                auditLogVO.setRemote_port(resultSet.getString("remote_port"));
                res.add(auditLogVO);
            }
        } catch (Exception e) {
            log.error("Querying the Audit Log Exception", e);
        }

        return res;
    }

    @Override
    public void download(String hostId, String path, String filename, HttpServletResponse response) {
        OpsHostEntity opsHostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(opsHostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(opsHostEntity.getHostId());

        OpsHostUserEntity installUser = hostUserList.stream().filter(opsHostUserEntity -> !"root".equalsIgnoreCase(opsHostUserEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + opsHostEntity.getHostname() + "] user not found"));
        Session session = jschUtil.getSession(opsHostEntity.getPublicIp(), opsHostEntity.getPort(), installUser.getUsername(), encryptionUtils.decrypt(installUser.getPassword())).orElseThrow(() -> new OpsException("root user failed to establish connection"));

        jschUtil.download(session, path, filename, response);

        if (Objects.nonNull(session) && session.isConnected()) {
            session.disconnect();
        }
    }

    @Override
    public void uninstall(UnInstallBody unInstallBody) {
        if (disasterClusterMapper.queryDisasterClusterCountByClusterName(unInstallBody.getClusterId()) != 0) {
            throw new OpsException("the cluster is used by disaster cluster,can't uninstall");
        }
        UnInstallContext unInstallContext = new UnInstallContext();
        OpsClusterEntity clusterEntity = getById(unInstallBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        unInstallContext.setOpsClusterEntity(clusterEntity);
        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(unInstallBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }
        unInstallContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        WsSession wsSession = wsConnectorManager.getSession(unInstallBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        unInstallContext.setRetSession(wsSession);
        constructUninstallContext(opsClusterNodeEntityList, clusterEntity, unInstallBody, unInstallContext);
    }

    private void constructUninstallContext(List<OpsClusterNodeEntity> opsClusterNodeEntityList,
        OpsClusterEntity clusterEntity, UnInstallBody unInstallBody, UnInstallContext unInstallContext) {
        try {
            List<String> hostIdList = opsClusterNodeEntityList.stream()
                .map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
            if (CollUtil.isEmpty(hostIdList)) {
                throw new OpsException("Node host configuration information cannot be empty");
            }
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);
            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);
            List<HostInfoHolder> hostInfoHolderList = opsHostEntities.stream()
                .map(host -> new HostInfoHolder(host, hostUserEntities.stream()
                    .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                    .collect(Collectors.toList()))).collect(Collectors.toList());
            for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                OpsHostUserEntity rootUserEntity = userEntities.stream()
                    .filter(userEntity -> "root".equals(userEntity.getUsername())).findFirst()
                    .orElseThrow(() -> new OpsException(
                        "[" + hostInfoHolder.getHostEntity().getPublicIp() + "]root user information not found"));
                if (clusterEntity.getVersion() == OpenGaussVersionEnum.ENTERPRISE) {
                    if (StrUtil.isEmpty(rootUserEntity.getPassword())) {
                        if (StrUtil.isNotEmpty(unInstallBody.getRootPasswords().get(rootUserEntity.getHostId()))) {
                            rootUserEntity.setPassword(
                                unInstallBody.getRootPasswords().get(rootUserEntity.getHostId()));
                        } else {
                            throw new OpsException("root password not found");
                        }
                    }
                }
            }
            unInstallContext.setHostInfoHolders(hostInfoHolderList);
            unInstallContext.setOs(checkOS(unInstallContext.getHostInfoHolders(), false));
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doUnInstall(unInstallContext, unInstallBody.getForce());
            });
            TaskManager.registry(unInstallBody.getBusinessId(), future);
        } catch (OpsException e) {
            if (Objects.nonNull(unInstallBody.getForce()) && unInstallBody.getForce()) {
                OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
                removeById(opsClusterEntity);
                opsClusterNodeService.removeByIds(opsClusterNodeEntityList);
            }
            log.error("Uninstall exception：", e);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public void restart(OpsClusterBody restartBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(restartBody.getRole());
        opsClusterContext.setOpNodeIds(restartBody.getNodeIds());

        OpsClusterEntity clusterEntity = getById(restartBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(restartBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }

        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);
        if (StrUtil.isNotEmpty(restartBody.getBusinessId())) {
            WsSession wsSession = wsConnectorManager.getSession(restartBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
            opsClusterContext.setRetSession(wsSession);
        }

        List<String> hostIdList = opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIdList)) {
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);

            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);

            List<HostInfoHolder> hostInfoHolderList = opsHostEntities
                    .stream()
                    .map(host -> new HostInfoHolder(host, hostUserEntities
                            .stream()
                            .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                OpsHostUserEntity rootUserEntity = userEntities.stream().filter(userEntity -> "root".equals(userEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + hostInfoHolder.getHostEntity().getPublicIp() + "]root user information not found"));
                userEntities.remove(rootUserEntity);
            }

            opsClusterContext.setHostInfoHolders(hostInfoHolderList);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }

        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));

        if (Objects.nonNull(restartBody.getSync()) && restartBody.getSync()) {
            doRestart(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doRestart(opsClusterContext, false);
            });
            TaskManager.registry(restartBody.getBusinessId(), future);
        }
    }

    private void doRestart(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = ObjectUtil.clone(opsClusterContext);
            clusterOpsProviderManager
                    .provider(clone.getOpsClusterEntity().getVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .restart(clone);

            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("Cluster restart failed：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException("Cluster restart failed");
            }
        }
    }

    @Override
    public void start(OpsClusterBody startBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(startBody.getRole());
        opsClusterContext.setOpNodeIds(startBody.getNodeIds());

        OpsClusterEntity clusterEntity = getById(startBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(startBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }

        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);

        WsSession wsSession = wsConnectorManager.getSession(startBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        opsClusterContext.setRetSession(wsSession);

        List<String> hostIdList = opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIdList)) {
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);

            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);

            List<HostInfoHolder> hostInfoHolderList = opsHostEntities
                    .stream()
                    .map(host -> new HostInfoHolder(host, hostUserEntities
                            .stream()
                            .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                OpsHostUserEntity rootUserEntity = userEntities.stream().filter(userEntity -> "root".equals(userEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + hostInfoHolder.getHostEntity().getPublicIp() + "]root user information not found"));
                userEntities.remove(rootUserEntity);
            }

            opsClusterContext.setHostInfoHolders(hostInfoHolderList);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }

        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));
        if (Objects.nonNull(startBody.getSync()) && startBody.getSync()) {
            doStart(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doStart(opsClusterContext, false);
            });
            TaskManager.registry(startBody.getBusinessId(), future);
        }
    }

    private void doStart(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = ObjectUtil.clone(opsClusterContext);
            clusterOpsProviderManager
                    .provider(clone.getOpsClusterEntity().getVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .start(clone);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("startup exception：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException(e.getMessage());
            }
        }
    }

    @Override
    public void stop(OpsClusterBody stopBody) {
        OpsClusterContext opsClusterContext = new OpsClusterContext();
        opsClusterContext.setRole(stopBody.getRole());
        opsClusterContext.setOpNodeIds(Objects.isNull(stopBody.getNodeIds()) ? new ArrayList<>() : stopBody.getNodeIds());

        OpsClusterEntity clusterEntity = getById(stopBody.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("cluster does not exist");
        }
        opsClusterContext.setOpsClusterEntity(clusterEntity);

        List<OpsClusterNodeEntity> opsClusterNodeEntityList = opsClusterNodeService.listClusterNodeByClusterId(stopBody.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntityList)) {
            throw new OpsException("Cluster node information does not exist");
        }

        opsClusterContext.setOpsClusterNodeEntityList(opsClusterNodeEntityList);

        WsSession wsSession = wsConnectorManager.getSession(stopBody.getBusinessId()).orElseThrow(() -> new OpsException("websocket session not exist"));
        opsClusterContext.setRetSession(wsSession);

        List<String> hostIdList = opsClusterNodeEntityList.stream().map(OpsClusterNodeEntity::getHostId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(hostIdList)) {
            List<OpsHostEntity> opsHostEntities = hostFacade.listByIds(hostIdList);

            List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostIdList(hostIdList);

            List<HostInfoHolder> hostInfoHolderList = opsHostEntities
                    .stream()
                    .map(host -> new HostInfoHolder(host, hostUserEntities
                            .stream()
                            .filter(hostUser -> hostUser.getHostId().equals(host.getHostId()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());

            for (HostInfoHolder hostInfoHolder : hostInfoHolderList) {
                List<OpsHostUserEntity> userEntities = hostInfoHolder.getHostUserEntities();
                OpsHostUserEntity rootUserEntity = userEntities.stream().filter(userEntity -> "root".equals(userEntity.getUsername())).findFirst().orElseThrow(() -> new OpsException("[" + hostInfoHolder.getHostEntity().getPublicIp() + "]root user information not found"));
                userEntities.remove(rootUserEntity);
            }

            opsClusterContext.setHostInfoHolders(hostInfoHolderList);
        } else {
            throw new OpsException("Node host configuration information cannot be empty");
        }

        opsClusterContext.setOs(checkOS(opsClusterContext.getHostInfoHolders(), false));
        if (Objects.nonNull(stopBody.getSync()) && stopBody.getSync()) {
            doStop(opsClusterContext, true);
        } else {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                RequestContextHolder.setRequestAttributes(context);
                doStop(opsClusterContext, false);
            });
            TaskManager.registry(stopBody.getBusinessId(), future);
        }
    }

    private void doStop(OpsClusterContext opsClusterContext, boolean sync) {
        try {
            OpsClusterContext clone = ObjectUtil.clone(opsClusterContext);
            clusterOpsProviderManager
                    .provider(clone.getOpsClusterEntity().getVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .stop(clone);

            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
            }
        } catch (Exception e) {
            log.error("stop exception：", e);
            if (!sync) {
                wsUtil.sendText(opsClusterContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
            } else {
                throw new OpsException(e.getMessage());
            }
        }
    }

    @Override
    public List<OpsClusterVO> listEnterpriseCluster() {
        List<OpsClusterVO> allCluster = opsFacade.listCluster();
        if (CollUtil.isEmpty(allCluster)) {
            return allCluster;
        }
        List<OpsClusterVO> enterpriseClusterList = new ArrayList<>();
        allCluster.forEach(item -> {
            if (item.getVersion().equals(OpenGaussVersionEnum.ENTERPRISE.name())) {
                enterpriseClusterList.add(item);
            }
        });
        List<Future<?>> taskResult = new ArrayList<>();
        for (OpsClusterVO cluster: enterpriseClusterList) {
            OpsClusterEntity clusterEntity = getById(cluster.getClusterId());
            if (Objects.isNull(clusterEntity)) {
                throw new OpsException("Cluster information does not exist");
            }

            List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(cluster.getClusterId());
            if (CollUtil.isEmpty(opsClusterNodeEntities)) {
                throw new OpsException("The current cluster node was not found on the host");
            }

            OpsClusterNodeEntity clusterNode = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getClusterRole().equals(ClusterRoleEnum.MASTER)).findFirst().orElseThrow(() -> new OpsException("The master cluster node was not found in the cluster"));
            String installUserId = clusterNode.getInstallUserId();

            OpsHostEntity hostEntity = hostFacade.getById(clusterNode.getHostId());
            if (Objects.isNull(hostEntity)) {
                throw new OpsException("Node host information does not exist");
            }

            OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
            if (Objects.isNull(hostUserEntity)) {
                throw new OpsException("Installation user information does not exist");
            }

            Future<?> future = threadPoolTaskExecutor.submit(() -> {
                Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElseThrow(() -> new OpsException("Install user connection failed"));
                String stateResultStr = state(ommSession, OpenGaussVersionEnum.ENTERPRISE, clusterNode.getDataPath(), clusterEntity.getEnvPath());
                Map<String, Object> stateResult = JSON.parseObject(stateResultStr, Map.class);
                cluster.setClusterState((String) stateResult.get("cluster_state"));
            });
            taskResult.add(future);
        }
        for (Future<?> future: taskResult) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                String errMsg = "get cluster state from task failed: " + e.getMessage();
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
        return enterpriseClusterList;
    }

    @Override
    public ClusterSummaryVO summary() {
        ClusterSummaryVO clusterSummaryVO = new ClusterSummaryVO();
        clusterSummaryVO.setClusterNum(count());
        clusterSummaryVO.setHostNum(hostFacade.count());
        clusterSummaryVO.setNodeNum(opsClusterNodeService.count());
        return clusterSummaryVO;
    }

    public void downloadImportFile(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            List<OpsImportEntity> usersList = new ArrayList<>();
            OpsImportEntity opsImportEntity = new OpsImportEntity("1", "ip1,ip2", "ip1,ip2", "omm","gaussdb", "12345", 5432, "/home/omm/cluster_2024.bashrc", null,null);
            usersList.add(opsImportEntity);
            OutputStream out = response.getOutputStream();
            EasyExcel.write(out, OpsImportEntity.class).sheet("用户信息").doWrite(usersList);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new OpsException("download fail!");
        }
    }

    @Override
    public List<OpsImportEntity> uploadImportFile(MultipartFile file) {
        List<OpsImportEntity> opsImportEntityList = new ArrayList<>();
        OpsImportEntityListener userDataListener = new OpsImportEntityListener();
        try {
            opsImportEntityList = EasyExcel.read(file.getInputStream(), OpsImportEntity.class, userDataListener).sheet().doReadSync();
        } catch (IOException e) {
            log.error("upload fail!" + e);
            throw new OpsException("upload fail!");
        }
        return opsImportEntityList;
    }

    @Override
    public int importSuccessCount() {
        return importSuccessCount;
    }

    private String rootUserJudgeOpenGaussVersion(Session session, List<String> rootCommandList) {
        String versionType = null;
        int count = 0;
        try {
            try {
                jschUtil.executeCommand(rootCommandList.get(0), session);
                versionType = "ENTERPRISE";
            } catch (OpsException opsException) {
                log.warn(opsException.getMessage());
                count++;
            }
            try {
                jschUtil.executeCommand(rootCommandList.get(1), session);
                versionType = "LITE";
            } catch (OpsException opsException) {
                log.warn(opsException.getMessage());
                count++;
            }
            if (count == 2) {
                versionType = "MINIMAL_LIST";
            }
        } catch (IOException | InterruptedException e) {
            log.error("rootJudgeOpenGaussVersion error" + e);
        }
        return versionType;
    }

    private void markErrorInfo(OpsImportEntity opsImportEntity) {
        infoAndConn = false;
        opsImportEntity.setImportStatus("fail");
        opsImportEntity.setErrorInfo("please check publicIp or installUsername or envPath!");
    }

    private void judgeNodeConnSuccess(String publicIp, OpsImportEntity opsImportEntity, String localRole) {
        Connection connection = null;
        try {
            connection = DBUtil.getSession(publicIp, opsImportEntity.getPort(), opsImportEntity.getDatabaseUsername(), opsImportEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("please check databaseUser is or not a origianl User,if it is,Connection failed"));
        } catch (OpsException | SQLException | ClassNotFoundException e) {
            if (localRole == null || localRole.equals("")) {
                opsImportEntity.setImportStatus("fail");
            } else {
                if (localRole.equals("Primary")) {
                    opsImportEntity.setImportStatus("fail");
                    infoAndConn = false;
                }
            }
            opsImportEntity.setErrorInfo(publicIp + " Connection fail :" + e);
        } finally {
            DBUtil.closeConn(connection);
        }
    }

    private void packingClusterInfo(List<String> resultList, String versionType, OpsImportEntity opsImportEntity, OpsImportSshEntity HostAndUserId, String publicIp, int ipSequence, Session session) {
        String localRole = null;
        if (versionType.equals("ENTERPRISE")) {
            int selectCommandLength = 4;
            try {
                if (resultList.get(selectCommandLength - 1).equals("Primary")) {
                    localRole = resultList.get(selectCommandLength - 1);
                    masterIsNormal = true;
                } else {
                    localRole = resultList.get(selectCommandLength - 1);
                    opsImportEntity.setErrorInfo("check node status error!");
                }
            } catch (IndexOutOfBoundsException e) {
                markErrorInfo(opsImportEntity);
                opsImportEntity.setErrorInfo("please check if envFile has GAUSSHOME and PGDATA!");
            }
        } else if (versionType.equals("LITE")) {
            try {
                String selectLocal = MessageFormat.format(CHANGE_SUB_USER, opsImportEntity.getInstallUsername(), MessageFormat.format(THREE_IN_ONE, "source " + opsImportEntity.getEnvPath(), ";gs_ctl query -D " + resultList.get(2) + "|grep local_role|awk '\\''{print $3}'\\''|head -n 1", ""));
                localRole = jschUtil.executeCommand(selectLocal, session).getResult();
            } catch (OpsException | IOException | InterruptedException e) {
                markErrorInfo(opsImportEntity);
            }
        } else {
            localRole = ipSequence == 0 ? "Primary" : "Standby";
        }
        opsClusterEntity.setClusterId(opsImportEntity.getClusterName());
        opsClusterEntity.setVersion(versionType.equals("ENTERPRISE") ? ENTERPRISE : versionType.equals("LITE") ? LITE : MINIMAL_LIST);
        opsClusterEntity.setVersionNum(versionType.equals("ENTERPRISE") | versionType.equals("LITE") ? resultList.get(2) : resultList.get(1));
        opsClusterEntity.setDatabasePassword(opsImportEntity.getDatabasePassword());
        opsClusterEntity.setDatabaseUsername(opsImportEntity.getDatabaseUsername());
        opsClusterEntity.setPort(opsImportEntity.getPort());
        opsClusterEntity.setEnvPath(opsImportEntity.getEnvPath());
        opsClusterEntity.setInstallPath(resultList.get(0));
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId(StrUtil.uuid());
        try {
            ClusterRoleEnum ClusterRole = localRole.equals("Primary") ? MASTER : SLAVE;
            opsClusterNodeEntity.setClusterRole(ClusterRole);
        } catch (NullPointerException e) {
            log.error("lack of clusterInfo!");
        }
        judgeNodeConnSuccess(publicIp, opsImportEntity, localRole);
        opsClusterNodeEntity.setHostId(HostAndUserId.getHostId()+"");
        opsClusterNodeEntity.setInstallUserId(HostAndUserId.getHostUserId()+"");
        opsClusterNodeEntity.setInstallPath(resultList.get(0));
        opsClusterNodeEntity.setDataPath(versionType.equals("ENTERPRISE") | versionType.equals("LITE") ? resultList.get(1) : resultList.get(0) + "/data");
        opsClusterNodeEntity.setClusterId(opsImportEntity.getClusterName());
        opsClusterNodeEntityList.add(opsClusterNodeEntity);
    }

    private void selectClusterInfo(String versionType, Session session, OpsImportEntity opsImportEntity, OpsImportSshEntity HostAndUserId, String publicIp, int ipSequence) {
        String VersionNum = MessageFormat.format(THREE_IN_ONE,"source "+opsImportEntity.getEnvPath(), ";gsql -V| grep -oP \"\\d+\\.\\d+\\.\\d+\"", "");
        String enterGAUSSHOME = MessageFormat.format(THREE_IN_ONE, MessageFormat.format(ENV_PARAMETER_GREP, "GAUSSHOME=", opsImportEntity.getEnvPath()), "|grep app", RESULT_BY_SPLIT_EQUAL);
        String enterPGDATA = MessageFormat.format(THREE_IN_ONE, MessageFormat.format(ENV_PARAMETER_GREP, "PGDATA=", opsImportEntity.getEnvPath()), "|grep dn", RESULT_BY_SPLIT_EQUAL);
        String enterJudgeMasterOrSlave = MessageFormat.format(THREE_IN_ONE,"source "+opsImportEntity.getEnvPath(), ";gs_om -t status --detail|grep "+publicIp+"|awk '\\''{print $8}'\\''", "");
        String liteAndMiniGAUSSHOME = MessageFormat.format(THREE_IN_ONE, MessageFormat.format(ENV_PARAMETER_GREP, "GAUSSHOME=", opsImportEntity.getEnvPath()), "", RESULT_BY_SPLIT_EQUAL);
        String liteGAUSSDATA = MessageFormat.format(THREE_IN_ONE, MessageFormat.format(ENV_PARAMETER_GREP, "GAUSSDATA=", opsImportEntity.getEnvPath()), "", RESULT_BY_SPLIT_EQUAL);
        String command = null;
        if (versionType.equals("ENTERPRISE")) {
            command = MessageFormat.format(CHANGE_SUB_USER, opsImportEntity.getInstallUsername(), enterGAUSSHOME + ";" + enterPGDATA + ";" + VersionNum + ";" + enterJudgeMasterOrSlave);
        } else if (versionType.equals("LITE")) {
            command = MessageFormat.format(CHANGE_SUB_USER, opsImportEntity.getInstallUsername(), liteAndMiniGAUSSHOME + ";" + liteGAUSSDATA + ";" + VersionNum);
        } else {
            command = MessageFormat.format(CHANGE_SUB_USER, opsImportEntity.getInstallUsername(), liteAndMiniGAUSSHOME+";"+VersionNum);
        }
        List<String> resultList = new ArrayList<>();
        try {
            String result = jschUtil.executeCommand(command, session).getResult();
            String regex = "(\\S+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                resultList.add(matcher.group());
            }
        } catch (OpsException opsException) {
            markErrorInfo(opsImportEntity);
        } catch (IOException | InterruptedException e) {
            log.error("command fail, please check the excel info and database env file!");
        }
        packingClusterInfo(resultList, versionType, opsImportEntity, HostAndUserId, publicIp, ipSequence, session);
    }

    private boolean checkPortAndIp(String[] hosts, OpsImportEntity opsImportEntity) {
        boolean flag = false;
        for (int j = 0; j < hosts.length; j++) {
            try {
                String host = hosts[j];
                List<String> cluster_id = opsImportSshMapper.checkPublicIpAndPort(host, opsImportEntity.getPort()+"");
                if (cluster_id.get(0) != null) {
                    flag = true;
                    opsImportEntity.setImportStatus("fail");
                    opsImportEntity.setErrorInfo("The public IP and port that are inputted already exist, Similar to it are :" + cluster_id);
                }
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                log.warn("checkPublicIpAndPort, no same cluster");
            }
        }
        opsClusterEntity.setDeployType(hosts.length > 1 ? CLUSTER : SINGLE_NODE);
        return flag;
    }

    private void saveCluster(boolean sameCluster, OpsImportEntity opsImportEntity) {
        if (!sameCluster && infoAndConn && masterIsNormal) {
            saveBatch(opsClusterEntity, opsClusterNodeEntityList, opsImportEntity);
        }
        opsClusterEntityList.clear();
        opsClusterNodeEntityList.clear();
    }

    @Override
    public List<OpsImportEntity> parseExcel(List<OpsImportEntity> list) {
        String[] hosts = {};
        importSuccessCount = 0;
        if (list == null || list.isEmpty()) {
            return list;
        }
        for (int i = 0; i < list.size(); i++) {
            try {
                list.get(i).checkConfig();
            } catch (OpsException e) {
                list.get(i).setImportStatus("fail");
                list.get(i).setErrorInfo("please fill in all the blanks!" + e);
                continue;
            }
            infoAndConn = true;
            masterIsNormal = false;
            hosts = list.get(i).getPublicIp().split(",");
            List<OpsImportSshEntity> RootPortAndPasswordlist = opsImportSshMapper.queryHostInfo("root", hosts[0]);
            if (RootPortAndPasswordlist == null || RootPortAndPasswordlist.isEmpty()) {
                list.get(i).setImportStatus("fail");
                list.get(i).setErrorInfo("please import host information.");
                continue;
            }
            Session session = null;
            try {
                session = jschUtil.getSession(hosts[0], RootPortAndPasswordlist.get(0).getPort(), "root", encryptionUtils.decrypt(RootPortAndPasswordlist.get(0).getPassword())).orElseThrow(() -> new OpsException("Failed to establish a session with the host"));
            } catch (OpsException opsException) {
                log.error(opsException + hosts[0]);
                list.get(i).setImportStatus("fail");
                list.get(i).setErrorInfo(opsException + hosts[0]);
            }
            String omCommand = MessageFormat.format(CHANGE_SUB_USER, list.get(i).getInstallUsername(), MessageFormat.format(THREE_IN_ONE,"source "+list.get(i).getEnvPath(), ";gs_om -t view", ""));
            String liteCommand = MessageFormat.format(CHANGE_SUB_USER, list.get(i).getInstallUsername(), MessageFormat.format(THREE_IN_ONE,"source "+list.get(i).getEnvPath(), ";gsql -V|grep -i \"openGauss-lite\"", ""));
            List<String> rootCommandList = new ArrayList<>();
            rootCommandList.add(omCommand);
            rootCommandList.add(liteCommand);
            String versionType = rootUserJudgeOpenGaussVersion(session, rootCommandList);
            for (int j = 0; j < hosts.length; j++) {
                List<OpsImportSshEntity> HostAndUserIdlist = opsImportSshMapper.queryHostInfo(list.get(i).getInstallUsername(), hosts[j]);
                if (HostAndUserIdlist == null || HostAndUserIdlist.isEmpty()) {
                    list.get(i).setImportStatus("fail");
                    list.get(i).setErrorInfo("please import hostUser information.");
                    continue;
                }
                selectClusterInfo(versionType, session, list.get(i), HostAndUserIdlist.get(0), hosts[j], j);
            }
            boolean sameCluster = checkPortAndIp(hosts, list.get(i));
            saveCluster(sameCluster, list.get(i));
        }
        return list;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void saveBatch(OpsClusterEntity opsClusterEntity, List<OpsClusterNodeEntity> opsClusterNodeEntityList, OpsImportEntity opsImportEntity) {
        try {
            save(opsClusterEntity);
            opsClusterNodeService.saveBatch(opsClusterNodeEntityList);
            importSuccessCount++;
            opsImportEntity.setImportStatus("success");
        } catch (Exception e) {
            log.error("database saveBatch error!");
            opsImportEntity.setImportStatus("fail");
            opsImportEntity.setErrorInfo("database saveBatch error!");
            throw new RuntimeException("database rollback!");
        }
    }

    @Override
    public void downloadErrorFile(HttpServletResponse response, List<OpsImportEntity> usersList) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("错误报告", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            OutputStream out = response.getOutputStream();
            EasyExcel.write(out, OpsImportEntity.class).sheet("用户信息").doWrite(usersList);
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("downloadErrorFile fail!");
            throw new OpsException("downloadErrorFile fail!");
        }
    }

    @Override
    public void importCluster(ImportClusterBody importClusterBody) {
        importClusterBody.checkConfig();

        OpenGaussVersionEnum openGaussVersion = importClusterBody.getOpenGaussVersion();
        Integer port = null;
        String databaseUsername = null;
        String databasePassword = null;
        String masterHostId = null;
        String masterNodeInstallUserId = null;
        String masterHostUsername = null;
        String masterHostPassword = null;
        if (OpenGaussVersionEnum.MINIMAL_LIST == openGaussVersion) {
            port = importClusterBody.getMinimalistInstallConfig().getPort();
            databaseUsername = importClusterBody.getMinimalistInstallConfig().getDatabaseUsername();
            databasePassword = importClusterBody.getMinimalistInstallConfig().getDatabasePassword();
            MinimalistInstallNodeConfig masterNode = importClusterBody
                    .getMinimalistInstallConfig()
                    .getNodeConfigList().stream()
                    .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                    .findFirst().orElseThrow(() -> new OpsException("masternode not found"));
            masterHostId = masterNode.getHostId();
            masterNodeInstallUserId = masterNode.getInstallUserId();
        } else if (OpenGaussVersionEnum.LITE == openGaussVersion) {
            port = importClusterBody.getLiteInstallConfig().getPort();
            databaseUsername = importClusterBody.getLiteInstallConfig().getDatabaseUsername();
            databasePassword = importClusterBody.getLiteInstallConfig().getDatabasePassword();
            LiteInstallNodeConfig masterNode = importClusterBody.getLiteInstallConfig()
                    .getNodeConfigList()
                    .stream()
                    .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                    .findFirst()
                    .orElseThrow(() -> new OpsException("masternode not found"));
            masterHostId = masterNode.getHostId();
            masterNodeInstallUserId = masterNode.getInstallUserId();
        } else if (OpenGaussVersionEnum.ENTERPRISE == openGaussVersion) {
            port = importClusterBody.getEnterpriseInstallConfig().getPort();
            databaseUsername = importClusterBody.getEnterpriseInstallConfig().getDatabaseUsername();
            databasePassword = importClusterBody.getEnterpriseInstallConfig().getDatabasePassword();
            EnterpriseInstallNodeConfig masterNode = importClusterBody.getEnterpriseInstallConfig()
                    .getNodeConfigList()
                    .stream()
                    .filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER)
                    .findFirst()
                    .orElseThrow(() -> new OpsException("masternode not found"));
            masterHostId = masterNode.getHostId();
            masterNodeInstallUserId = masterNode.getInstallUserId();
        }

        OpsHostUserEntity masterNodeInstallUser = hostUserFacade.getById(masterNodeInstallUserId);
        if (Objects.isNull(masterNodeInstallUser)) {
            throw new OpsException("install user not found");
        } else {
            masterHostUsername = masterNodeInstallUser.getUsername();
            masterHostPassword = masterNodeInstallUser.getPassword();
        }

        OpsHostEntity hostEntity = hostFacade.getById(masterHostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host not found");
        }

        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), masterHostUsername, encryptionUtils.decrypt(masterHostPassword)).orElseThrow(() -> new OpsException("Failed to establish connection with host " + hostEntity.getPublicIp()));
        Connection connection = null;
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), port, databaseUsername, databasePassword).orElseThrow(() -> new OpsException("Connection failed"));
            String versionNum = getVersionNum(ommSession, importClusterBody.getEnvPath());
            importClusterBody.setOpenGaussVersionNum(versionNum);
            Integer majorVersion = Integer.valueOf(versionNum.substring(0, 1));
            OpenGaussVersionEnum openGaussVersionEnum = judgeOpenGaussVersion(majorVersion, ommSession, connection, importClusterBody.getEnvPath());
            boolean versionMatch = false;
            if (majorVersion >= 5) {
                if (importClusterBody.getOpenGaussVersion() == openGaussVersionEnum) {
                    versionMatch = true;
                }
            } else {
                if (importClusterBody.getOpenGaussVersion() == OpenGaussVersionEnum.ENTERPRISE) {
                    if (openGaussVersionEnum == OpenGaussVersionEnum.ENTERPRISE) {
                        versionMatch = true;
                    }
                } else {
                    if (openGaussVersionEnum != OpenGaussVersionEnum.ENTERPRISE) {
                        versionMatch = true;
                    }
                }
            }

            if (!versionMatch) {
                log.error("The selected version does not match the actual version,select version:{},actual version:{}", importClusterBody.getOpenGaussVersion(), openGaussVersionEnum);
                throw new OpsException("The selected version does not match the actual version");
            }
        } catch (OpsException e) {
            log.error("ops exception ", e);
            throw e;
        } catch (Exception e) {
            log.error("get connection fail", e);
            throw new OpsException("connection fail");
        } finally {
            if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                ommSession.disconnect();
            }

            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {

                }
            }
        }

        OpsClusterEntity opsClusterEntity = importClusterBody.toOpsClusterEntity();
        save(opsClusterEntity);

        if (OpenGaussVersionEnum.LITE == importClusterBody.getOpenGaussVersion()) {
            List<OpsClusterNodeEntity> opsClusterNodeEntities = importClusterBody.getLiteInstallConfig().toOpsClusterNodeEntityList();
            for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
                opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
            }
            opsClusterNodeService.saveBatch(opsClusterNodeEntities);
        } else if (OpenGaussVersionEnum.MINIMAL_LIST == importClusterBody.getOpenGaussVersion()) {
            List<OpsClusterNodeEntity> opsClusterNodeEntities = importClusterBody.getMinimalistInstallConfig().toOpsClusterNodeEntityList();
            for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
                opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
            }
            opsClusterNodeService.saveBatch(opsClusterNodeEntities);
        } else if (OpenGaussVersionEnum.ENTERPRISE == importClusterBody.getOpenGaussVersion()) {
            List<OpsClusterNodeEntity> opsClusterNodeEntities = importClusterBody.getEnterpriseInstallConfig().toOpsClusterNodeEntityList();
            for (OpsClusterNodeEntity opsClusterNodeEntity : opsClusterNodeEntities) {
                opsClusterNodeEntity.setClusterId(opsClusterEntity.getClusterId());
            }
            opsClusterNodeService.saveBatch(opsClusterNodeEntities);
        }
    }

    private OpenGaussVersionEnum judgeOpenGaussVersion(Integer majorVersion, Session ommSession, Connection connection, String envPath) {
        boolean enterprise = enterpriseVersion(ommSession, envPath);
        if (enterprise) {
            return OpenGaussVersionEnum.ENTERPRISE;
        }

        if (majorVersion >= 5) {
            boolean lite = liteVersion(connection);
            if (lite) {
                return OpenGaussVersionEnum.LITE;
            }

            return OpenGaussVersionEnum.MINIMAL_LIST;
        } else {
            return OpenGaussVersionEnum.MINIMAL_LIST;
        }
    }

    private boolean liteVersion(Connection connection) {
        String sql = "select version()";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String version = resultSet.getString("version");
                return version.contains("lite");
            }
        } catch (Exception e) {
            log.error("query version fail", e);
            throw new OpsException("query version fail");
        }

        return false;
    }

    private boolean enterpriseVersion(Session ommSession, String envPath) {
        String command = "which gs_om";
        JschResult jschResult = null;
        try {
            jschResult = jschUtil.executeCommand(command, ommSession, envPath);

            if (0 != jschResult.getExitCode()) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getVersionNum(Session ommSession, String envPath) {
        String command = "gsql -V";
        JschResult jschResult = null;
        try {
            jschResult = jschUtil.executeCommand(command, ommSession, envPath);

            if (0 != jschResult.getExitCode()) {
                log.error("Failed to get openGauss version, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get openGauss version");
            }

            String result = jschResult.getResult();
            String majorVersion = null;
            String patternString = "([0-9]+\\.){1,2}[0-9]+";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                majorVersion = matcher.group();
            }

            log.info("openGauss version:{}", majorVersion);
            if (StrUtil.isEmpty(majorVersion)) {
                throw new OpsException("Failed to get openGauss version");
            }
            return majorVersion;
        } catch (Exception e) {
            log.error("Failed to get openGauss version", e);
            throw new OpsException("Failed to get openGauss version");
        }
    }


    @Override
    public void monitor(String clusterId, String hostId, String businessId, ClusterRoleEnum role) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(opsClusterNodeEntity -> opsClusterNodeEntity.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node information does not exist"));

        String installUserId = nodeEntity.getInstallUserId();

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Node host information does not exist");
        }
        OpsHostUserEntity hostUserEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(hostUserEntity)) {
            throw new OpsException("Node installation user information does not exist");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("Node root user information does not exist");
        }

        WsSession wsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("response session does not exist"));
        Session ommSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), hostUserEntity.getUsername(), encryptionUtils.decrypt(hostUserEntity.getPassword())).orElseThrow(() -> new OpsException("Install user connection failed"));

        String dataPath = nodeEntity.getDataPath();
        if (OpenGaussVersionEnum.MINIMAL_LIST == clusterEntity.getVersion()) {
            if (clusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                if (role == ClusterRoleEnum.MASTER) {
                    dataPath = dataPath + "/master";
                } else {
                    dataPath = dataPath + "/slave";
                }
            } else {
                dataPath = dataPath + "/single_node";
            }
        }


        String realDataPath = dataPath;
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            Connection connection = null;
            try {
                connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), clusterEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("Unable to connect to the database"));
                doMonitor(wsSession, ommSession, clusterEntity.getVersion(), connection, realDataPath, clusterEntity.getEnvPath());
            } catch (Exception e) {
                log.error("get connection fail , ip:{} , port:{}, username:{}", hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), e);
            } finally {
                if (Objects.nonNull(connection)) {
                    try {
                        connection.close();
                    } catch (SQLException e) {

                    }
                }

                if (Objects.nonNull(ommSession) && ommSession.isConnected()) {
                    ommSession.disconnect();
                }

                wsUtil.close(wsSession);
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public List<OpsHostEntity> listClusterHost(String clusterId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        Set<String> hostIds = opsClusterNodeEntities.stream().map(OpsClusterNodeEntity::getHostId).collect(Collectors.toSet());
        return hostFacade.listByIds(hostIds);
    }

    @Override
    public boolean hasName(String name) {
        OpsClusterEntity clusterEntity = getById(name);
        return Objects.nonNull(clusterEntity);
    }

    @Override
    public void generateconf(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        WsSession retWsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("websocket session not exist"));

        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doGenerateconf(session, retWsSession, clusterEntity.getXmlConfigPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public void switchover(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        WsSession retWsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("websocket session not exist"));

        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doSwitchover(session, retWsSession, nodeEntity.getDataPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    @Override
    public void build(String clusterId, String hostId, String businessId) {
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostId)).findFirst().orElseThrow(() -> new OpsException("Cluster node information does not exist"));
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(nodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installing user information does not exist");
        }

        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        WsSession retWsSession = wsConnectorManager.getSession(businessId).orElseThrow(() -> new OpsException("websocket session not exist"));

        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            try {
                RequestContextHolder.setRequestAttributes(context);
                doBuild(session, retWsSession, nodeEntity.getDataPath(), clusterEntity.getEnvPath());
            } finally {
                if (Objects.nonNull(session) && session.isConnected()) {
                    session.disconnect();
                }
            }

        });
        TaskManager.registry(businessId, future);
    }

    private void doBuild(Session session, WsSession retWsSession, String dataPath, String envPath) {
        String command = "gs_ctl build -D " + dataPath;

        try {
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("A build error occurred, exit code {}", jschResult.getExitCode());
                throw new OpsException("A build error occurred");
            }
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("build fail", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    @Override
    public ListDir listInstallPackage(OpenGaussVersionEnum openGaussVersionEnum, Integer userId) {
        SysSettingEntity entity = sysSettingFacade.getSysSetting(userId);

        Assert.notNull(entity, "Failed to get installation package path");

        ListDir listDir = new ListDir();
        listDir.setPath(entity.getUploadPath());

        String[] extension = null;
        String[] namePart = null;

        if (OpenGaussVersionEnum.ENTERPRISE == openGaussVersionEnum) {
            extension = new String[]{"-all.tar.gz"};
        } else if (OpenGaussVersionEnum.LITE == openGaussVersionEnum) {
            namePart = new String[]{"-Lite-"};
        } else if (OpenGaussVersionEnum.MINIMAL_LIST == openGaussVersionEnum) {
            extension = new String[]{".tar.bz2"};
        } else {
            extension = new String[]{".tar.gz", ".tar.bz2"};
        }

        listDir.setFiles(listFiles(entity.getUploadPath(), openGaussVersionEnum, extension, namePart));
        return listDir;
    }

    private boolean nameFilter(File file, String[] namePart) {
        if (namePart == null || namePart.length < 1) {
            return true;
        }

        String name = file.getName();
        for (String s : namePart) {
            if (name.contains(s)) {
                return true;
            }
        }

        return false;
    }

    private boolean extensionFilter(File file, String[] extension) {
        if (extension == null || extension.length < 1) {
            return true;
        }

        String name = file.getName();
        for (String s : extension) {
            if (name.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private List<HostFile> listFiles(String uploadPath, OpenGaussVersionEnum openGaussVersionEnum, String[] extension, String[] namePart) {
        log.info("List the files under the path: {}", uploadPath);
        List<OpsPackageManagerEntity> pkgList = pkgManagerService.list();
        List<HostFile> result = new ArrayList<>();
        Assert.notEmpty(uploadPath, "Failed to enumerate files, path cannot be empty");

        File folderPath = new File(uploadPath);
        if (!folderPath.isDirectory()) {
            return result;
        }
        List<String> files = getAllFiles(uploadPath);
        if (files.size() <= 0) {
            return result;
        }
        for (String filePath : files) {
            File file = new File(filePath);
            if (!file.isFile() || !file.exists()) {
                continue;
            }
            OpsPackageManagerEntity entity = CollUtil.findOne(pkgList, item -> item.getName() != null && filePath.contains(item.getName()));
            // is managed, get info here
            if (entity != null && entity.getPackageVersion().equalsIgnoreCase(openGaussVersionEnum.toString())) {
                OpsPackageVO vo = entity.toVO();
                result.add(HostFile.build(file, vo));
            } else {
                // not managed, filter by name
                if (extensionFilter(file, extension) && nameFilter(file, namePart)) {
                    result.add(HostFile.build(file, new OpsPackageVO()));
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private String getOrDefault(String path) {
        if (StrUtil.isNotEmpty(path)) {
            return path;
        }

        return System.getProperty("user.dir");
    }

    @Override
    public HostEnv env(String hostId, OpenGaussSupportOSEnum expectedOs, String rootPassword) {
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsHostUserEntity> hostUserEntities = hostUserFacade.listHostUserByHostId(hostId);
        if (CollUtil.isEmpty(hostUserEntities)) {
            throw new OpsException("Host user information does not exist");
        }
        // use root check env
        OpsHostUserEntity userEntity = hostUserEntities
                .stream()
                .filter(hostUser -> PermissionUtils.hasRootPermission(hostUser.getUsername()))
                .findFirst()
                .orElseThrow(() -> new OpsException("user information does not exist"));
        String encryptedRootPass = rootPassword;

        if (StrUtil.isEmpty(userEntity.getPassword()) && StrUtil.isEmpty(encryptedRootPass)){
            throw new OpsException("root password cannot be empty");
        }

        if (StrUtil.isNotEmpty(userEntity.getPassword())) {
            encryptedRootPass = userEntity.getPassword();
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(encryptedRootPass))
                .orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        HostEnv hostEnv = new HostEnv();

        try {
            CountDownLatch countDownLatch = new CountDownLatch(2);
            threadPoolTaskExecutor.submit(() -> {
                hostEnv.setHardwareEnv(hardwareEnvDetect(session, expectedOs));
                countDownLatch.countDown();
            });

            threadPoolTaskExecutor.submit(() -> {
                hostEnv.setSoftwareEnv(softwareEnvDetect(session, expectedOs));
                countDownLatch.countDown();
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
            }
        } finally {
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
        }

        return hostEnv;
    }

    @Override
    public Map<String, Integer> threadPoolMonitor() {
        Map<String, Integer> res = new HashMap<>();
        int activeCount = threadPoolTaskExecutor.getActiveCount();
        int poolSize = threadPoolTaskExecutor.getPoolSize();
        int corePoolSize = threadPoolTaskExecutor.getCorePoolSize();
        int keepAliveSeconds = threadPoolTaskExecutor.getKeepAliveSeconds();

        res.put("activeCount", activeCount);
        res.put("poolSize", poolSize);
        res.put("corePoolSize", corePoolSize);
        res.put("keepAliveSeconds", keepAliveSeconds);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCluster(String clusterId) {
        if (disasterClusterMapper.queryDisasterClusterCountByClusterName(clusterId) != 0) {
            throw new OpsException("the cluster is used by disaster cluster,can't delete");
        }
        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isNotEmpty(opsClusterNodeEntities)) {
            opsClusterNodeService.removeBatchByIds(opsClusterNodeEntities.stream()
                .map(OpsClusterNodeEntity::getClusterNodeId)
                .collect(Collectors.toSet()));
        }

        removeById(clusterId);
    }

    private void doSwitchover(Session session, WsSession retWsSession, String dataPath, String envPath) {
        try {
            String command = "gs_ctl switchover -D " + dataPath;
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Active-standby switchover failed, exit code {}", jschResult.getExitCode());
                throw new OpsException("Active-standby switchover failed");
            }
        } catch (Exception e) {
            log.error("Active-standby switchover failed", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            return;
        }

        try {
            String command = "gs_om -t refreshconf";
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Active-standby switchover failed, exit code {}", jschResult.getExitCode());
                throw new OpsException("Active-standby switchover failed");
            }
        } catch (Exception e) {
            log.error("Active-standby switchover failed", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            return;
        }

        wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
    }

    private void doGenerateconf(Session session, WsSession retWsSession, String xmlConfigPath, String envPath) {
        String command = "gs_om -t generateconf -X " + xmlConfigPath + " --distribute";

        try {
            JschResult jschResult = jschUtil.executeCommand(command, envPath, session, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("A generateconf error occurred, exit code {}", jschResult.getExitCode());
                throw new OpsException("A generateconf error occurred");
            }
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            log.error("generateconf fail", e);
            wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }

    private String doCheck(OpsHostUserEntity rootUserEntity, Session session) {
        log.info("One-click self-test start");
        String command = "gs_check -e inspect";

        try {
            Map<String, String> autoResponse = new HashMap<>();
            autoResponse.put("Please enter root privileges user[root]:", "root");
            autoResponse.put("Please enter password for user[root]:", rootUserEntity.getPassword());
            JschResult jschResult = jschUtil.executeCommand(command, session, autoResponse);
            if (0 != jschResult.getExitCode()) {
                log.error("A self-test error occurred, exit code {}", jschResult.getExitCode());
            } else {
                String result = jschResult.getResult();
                return result;

            }
        } catch (Exception e) {
            log.error("One-click self-test results", e);
            if (Objects.nonNull(session) && session.isConnected()) {
                session.disconnect();
            }
            throw new OpsException("One-click self-test results");
        } finally {
            log.info("One-click self-test end");
        }
        return null;
    }

    private CheckVO parseCheckResToCheckVO(String result) {
        CheckVO checkVO = new CheckVO();
        // cluster
        CheckClusterVO checkClusterVO = new CheckClusterVO();
        checkClusterVO.setCheckClusterState(parseCheckItem(result, "CheckClusterState"));
        checkClusterVO.setCheckDBParams(parseCheckItem(result, "CheckDBParams"));
        checkClusterVO.setCheckDebugSwitch(parseCheckItem(result, "CheckDebugSwitch"));
        checkClusterVO.setCheckDirPermissions(parseCheckItem(result, "CheckDirPermissions"));
        checkClusterVO.setCheckEnvProfile(parseCheckItem(result, "CheckEnvProfile"));
        checkClusterVO.setCheckReadonlyMode(parseCheckItem(result, "CheckReadonlyMode"));
        checkClusterVO.setCheckDilateSysTab(parseCheckItem(result, "CheckDilateSysTab"));
        checkClusterVO.setCheckProStartTime(parseCheckItem(result, "CheckProStartTime"));
        checkClusterVO.setCheckMpprcFile(parseCheckItem(result, "CheckMpprcFile"));

        checkVO.setCluster(checkClusterVO);
        // database
        CheckDbVO checkDbVO = new CheckDbVO();
        checkDbVO.setCheckCurConnCount(parseCheckItem(result, "CheckCurConnCount"));
        checkDbVO.setCheckCursorNum(parseCheckItem(result, "CheckCursorNum"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTableSpace(parseCheckItem(result, "CheckTableSpace"));
        checkDbVO.setCheckSysadminUser(parseCheckItem(result, "CheckSysadminUser"));
        checkDbVO.setCheckHashIndex(parseCheckItem(result, "CheckHashIndex"));
        checkDbVO.setCheckPgxcRedistb(parseCheckItem(result, "CheckPgxcRedistb"));
        checkDbVO.setCheckNodeGroupName(parseCheckItem(result, "CheckNodeGroupName"));
        checkDbVO.setCheckTDDate(parseCheckItem(result, "CheckTDDate"));

        checkVO.setDb(checkDbVO);
        // os
        CheckOSVO checkOSVO = new CheckOSVO();
        checkOSVO.setCheckEncoding(parseCheckItem(result, "CheckEncoding"));
        checkOSVO.setCheckFirewall(parseCheckItem(result, "CheckFirewall"));
        checkOSVO.setCheckKernelVer(parseCheckItem(result, "CheckKernelVer"));
        checkOSVO.setCheckMaxHandle(parseCheckItem(result, "CheckMaxHandle"));
        checkOSVO.setCheckNTPD(parseCheckItem(result, "CheckNTPD"));
        checkOSVO.setCheckOSVer(parseCheckItem(result, "CheckOSVer"));
        checkOSVO.setCheckSysParams(parseCheckItem(result, "CheckSysParams"));
        checkOSVO.setCheckTHP(parseCheckItem(result, "CheckTHP"));
        checkOSVO.setCheckTimeZone(parseCheckItem(result, "CheckTimeZone"));
        checkOSVO.setCheckCPU(parseCheckItem(result, "CheckCPU"));
        checkOSVO.setCheckSshdService(parseCheckItem(result, "CheckSshdService"));
        checkOSVO.setCheckSshdConfig(parseCheckItem(result, "CheckSshdConfig"));
        checkOSVO.setCheckCrondService(parseCheckItem(result, "CheckCrondService"));
        checkOSVO.setCheckStack(parseCheckItem(result, "CheckStack"));
        checkOSVO.setCheckSysPortRange(parseCheckItem(result, "CheckSysPortRange"));
        checkOSVO.setCheckMemInfo(parseCheckItem(result, "CheckMemInfo"));
        checkOSVO.setCheckHyperThread(parseCheckItem(result, "CheckHyperThread"));
        checkOSVO.setCheckMaxProcMemory(parseCheckItem(result, "CheckMaxProcMemory"));
        checkOSVO.setCheckBootItems(parseCheckItem(result, "CheckBootItems"));
        checkOSVO.setCheckKeyProAdj(parseCheckItem(result, "CheckKeyProAdj"));
        checkOSVO.setCheckFilehandle(parseCheckItem(result, "CheckFilehandle"));
        checkOSVO.setCheckDropCache(parseCheckItem(result, "CheckDropCache"));

        checkVO.setOs(checkOSVO);
        // device
        CheckDeviceVO checkDeviceVO = new CheckDeviceVO();
        checkDeviceVO.setCheckBlockdev(parseCheckItem(result, "CheckBlockdev"));
        checkDeviceVO.setCheckDiskFormat(parseCheckItem(result, "CheckDiskFormat"));
        checkDeviceVO.setCheckSpaceUsage(parseCheckItem(result, "CheckSpaceUsage"));
        checkDeviceVO.setCheckInodeUsage(parseCheckItem(result, "CheckInodeUsage"));
        checkDeviceVO.setCheckSwapMemory(parseCheckItem(result, "CheckSwapMemory"));
        checkDeviceVO.setCheckLogicalBlock(parseCheckItem(result, "CheckLogicalBlock"));
        checkDeviceVO.setCheckIOrequestqueue(parseCheckItem(result, "CheckIOrequestqueue"));
        checkDeviceVO.setCheckMaxAsyIOrequests(parseCheckItem(result, "CheckMaxAsyIOrequests"));
        checkDeviceVO.setCheckIOConfigure(parseCheckItem(result, "CheckIOConfigure"));

        checkVO.setDevice(checkDeviceVO);
        // network
        CheckNetworkVO checkNetworkVO = new CheckNetworkVO();
        checkNetworkVO.setCheckMTU(parseCheckItem(result, "CheckMTU"));
        checkNetworkVO.setCheckPing(parseCheckItem(result, "CheckPing"));
        checkNetworkVO.setCheckRXTX(parseCheckItem(result, "CheckRXTX"));
        checkNetworkVO.setCheckNetWorkDrop(parseCheckItem(result, "CheckNetWorkDrop"));
        checkNetworkVO.setCheckMultiQueue(parseCheckItem(result, "CheckMultiQueue"));
        checkNetworkVO.setCheckRouting(parseCheckItem(result, "CheckRouting"));
        checkNetworkVO.setCheckNICModel(parseCheckItem(result, "CheckNICModel"));

        checkVO.setNetwork(checkNetworkVO);

        return checkVO;
    }

    private CheckItemVO parseCheckItem(String result, String item) {
        item = item + ".";
        CheckItemVO checkItemVO = new CheckItemVO();
        checkItemVO.setName(item);
        int indexStart = result.indexOf(item);
        int reIndex = result.indexOf("\n", indexStart);
        int indexEnd = result.indexOf(".............", reIndex);

        if ("CheckMpprcFile.".equals(item)) {
            indexEnd = result.indexOf("Analysis the check result", reIndex);
        }

        if (indexEnd <= indexStart) {
            log.error("index end le index start");
            return checkItemVO;
        }

        String substring = result.substring(indexStart, indexEnd);
        if (StrUtil.isNotEmpty(substring)) {
            String[] split = substring.split("\n");

            if (split.length < 2) {
                return checkItemVO;
            }

            StringBuffer msg = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
                String line = split[i];
                if (i == 0) {
                    String status = line.substring(line.lastIndexOf(".") + 1, line.indexOf("\r"));
                    checkItemVO.setStatus(status);
                } else {
                    if (StrUtil.isNotEmpty(line.trim())) {
                        msg.append(line).append("\n");
                    } else {
                        break;
                    }
                }
            }

            checkItemVO.setMsg(msg.toString());
        }

        return checkItemVO;
    }

    private void doMonitor(WsSession wsSession, Session ommSession, OpenGaussVersionEnum version, Connection connection, String dataPath, String envPath) {
        AtomicBoolean hasError = new AtomicBoolean(false);
        while (wsSession.getSession().isOpen() && !hasError.get()) {
            NodeMonitorVO nodeMonitorVO = new NodeMonitorVO();
            CountDownLatch countDownLatch = new CountDownLatch(11);
            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setTime(System.currentTimeMillis());
                } catch (Exception e) {
                    log.error("time error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }

            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setCpu(cpu(ommSession));
                } catch (Exception e) {
                    log.error("cpu monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setMemory(memory(ommSession));
                } catch (Exception e) {
                    log.error("memory monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setNet(net(ommSession));
                } catch (Exception e) {
                    log.error("net monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setState(state(ommSession, version, dataPath, envPath));
                } catch (Exception e) {
                    log.error("state monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setLock(lock(connection));
                } catch (Exception e) {
                    log.error("lock monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setSession(session(connection));
                } catch (Exception e) {
                    log.error("session monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setConnectNum(connectNum(connection));
                } catch (Exception e) {
                    log.error("connectNum monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setSessionMemoryTop10(sessionMemoryTop10(connection));
                } catch (Exception e) {
                    log.error("sessionMemoryTop10 monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setKernel(kernel(ommSession));
                } catch (Exception e) {
                    log.error("kernel monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            threadPoolTaskExecutor.submit(() -> {
                try {
                    nodeMonitorVO.setMemorySize(memorySize(ommSession));
                } catch (Exception e) {
                    log.error("memorySize monitor error : ", e);
                    hasError.set(true);
                } finally {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
                throw new OpsException("monitor error");
            }

            wsUtil.sendText(wsSession, JSON.toJSONString(nodeMonitorVO));
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
        }

    }

    private List<Map<String, String>> sessionMemoryTop10(Connection connection) {
        String sql = "SELECT * FROM pv_session_memory_detail() ORDER BY usedsize desc limit 10";
        List<Map<String, String>> res = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                HashMap<String, String> model = new HashMap<>();
                model.put(resultSet.getString("sessid"), resultSet.getString("usedsize"));
                res.add(model);
            }
        } catch (Exception e) {
            log.error("Failed to query session memory top10", e);
            throw new OpsException("Failed to query session memory top10");
        }

        return res;
    }

    private String connectNum(Connection connection) {
        String sql = "SELECT count(*) FROM (SELECT pg_stat_get_backend_idset() AS backendid) AS s";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }

        return res;
    }

    private String session(Connection connection) {
        String sql = "SELECT count(*) FROM pg_stat_activity";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }

        return res;
    }

    private String lock(Connection connection) {
        String sql = "SELECT count(*) FROM pg_locks";
        String res = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                res = resultSet.getString("count");
            }
        } catch (Exception e) {
            log.error("Failed to query the number of connections", e);
            throw new OpsException("Failed to query the number of connections");
        }

        return res;
    }

    private String memorySize(Session rootSession) {
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.MEMORY_TOTAL, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Query memory size failed, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query memory size");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query memory size", e);
            throw new OpsException("Failed to query memory size");
        }
    }

    private String kernel(Session rootSession) {
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.CPU_CORE_NUM, rootSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to query core count, exit code: {}, message: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to query the number of cores");
            } else {
                return jschResult.getResult();
            }
        } catch (Exception e) {
            log.error("Failed to query the number of cores", e);
            throw new OpsException("Failed to query the number of cores");
        }
    }

    private String state(Session ommSession, OpenGaussVersionEnum version, String dataPath, String envPath) {
        if (OpenGaussVersionEnum.ENTERPRISE == version) {
            String command = "gs_om -t status --detail";
            JschResult jschResult = null;
            try {
                Map<String, Object> res = new HashMap<>();
                try {
                    jschResult = jschUtil.executeCommand(command, ommSession, envPath);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get status info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get status information");
                }

                Map<String, String> nodeState = new HashMap<>(1);
                Map<String, String> nodeRole = new HashMap<>(1);
                Map<String, String> cmState = new HashMap<>(1);
                res.put("nodeState", nodeState);
                res.put("nodeRole", nodeRole);
                res.put("cmState", cmState);

                String result = jschResult.getResult();
                int cmIndex = result.indexOf("CMServer State");
                if (cmIndex < 0) {

                } else {
                    int splitIndex = result.indexOf("------------------", cmIndex);
                    String dataNodeStateStr = result.substring(splitIndex);

                    String[] dataNode = dataNodeStateStr.split("\n");
                    for (String s : dataNode) {
                        String[] s1 = s.replaceAll(" +", " ").split(" ");
                        if (s1.length == 6) {
                            cmState.put(s1[1], s1[5].trim());
                        }
                    }
                }

                int clusterStateIndex = result.indexOf("cluster_state");
                String clusterState = null;
                if (clusterStateIndex < 0) {

                } else {
                    int splitIndex = result.indexOf(":", clusterStateIndex);
                    int lineEndIndex = result.indexOf("\n", clusterStateIndex);
                    clusterState = result.substring(splitIndex + 1, lineEndIndex).trim();
                    res.put("cluster_state", clusterState);
                }


                int datanodeStateIndex = result.indexOf("Datanode State");
                if (datanodeStateIndex < 0) {

                } else {
                    int splitIndex = result.indexOf("------------------", datanodeStateIndex);
                    String dataNodeStateStr = result.substring(splitIndex);

                    String[] dataNode = dataNodeStateStr.split("\n");
                    for (String s : dataNode) {
                        String[] s1 = s.replaceAll(" +", " ").split(" ");

                        String state = "";

                        if (s1.length >= 9) {
                            for (int i = 8; i < s1.length; i++) {
                                state += (s1[i] + " ");
                            }
                            nodeState.put(s1[1], state.trim());
                            nodeRole.put(s1[1], s1[7]);
                        } else if (s1.length >= 8) {
                            for (int i = 7; i < s1.length; i++) {
                                state += (s1[i] + " ");
                            }
                            nodeState.put(s1[1], state.trim());
                            nodeRole.put(s1[1], s1[6]);
                        }
//                        if (s1.length == 9) {
//                            nodeState.put(s1[1], s1[8].trim());
//                            nodeRole.put(s1[1],s1[7].trim());
//                        }else if (s1.length == 8){
//                            nodeState.put(s1[1],s1[7].trim());
//                            nodeRole.put(s1[1],s1[6].trim());
//                        }
                    }
                }
                return JSON.toJSONString(res);
            } catch (IOException e) {
                log.error("Failed to get status information", e);
                throw new OpsException("Failed to get status information");
            }

        } else {
            String command = "gs_ctl status -D " + dataPath;
            JschResult jschResult = null;
            try {
                try {
                    jschResult = jschUtil.executeCommand(command, ommSession, envPath);
                } catch (InterruptedException e) {
                    throw new OpsException("thread is interrupted");
                }
                if (0 != jschResult.getExitCode()) {
                    log.error("Failed to get status info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                    throw new OpsException("Failed to get status information");
                }

                String result = jschResult.getResult();
                if (result.contains("no server running")) {
                    return "false";
                } else {
                    return "true";
                }
            } catch (IOException e) {
                log.error("Failed to get status information", e);
                throw new OpsException("Failed to get status information");
            }
        }
    }

    private List<NodeNetMonitor> net(Session rootSession) {
        List<NodeNetMonitor> res = new ArrayList<>();
        String command = SshCommandConstants.NET;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to get network info, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get network information");
            }

            String result = jschResult.getResult();
            String[] split = result.split("\n");
            for (int i = 2; i < split.length; i++) {
                String s = split[i];
                String[] line = s.split(":");
                NodeNetMonitor nodeNetMonitor = new NodeNetMonitor();
                nodeNetMonitor.setFace(StrUtil.trim(line[0]));
                nodeNetMonitor.setReceive(line[1].trim().replaceAll(" +", " ").split(" ")[0]);
                nodeNetMonitor.setTransmit(line[1].trim().replaceAll(" +", " ").split(" ")[8]);
                res.add(nodeNetMonitor);
            }
        } catch (IOException e) {
            log.error("Failed to get network information", e);
            throw new OpsException("Failed to get network information");
        }

        return res;
    }

    private String memory(Session rootSession) {
        String command = SshCommandConstants.MEMORY_USING;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Getting memory usage failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get memory usage");
            }

            return jschResult.getResult();
        } catch (IOException e) {
            log.error("Failed to get memory usage", e);
            throw new OpsException("Failed to get memory usage");
        }
    }

    private String cpu(Session rootSession) {
        String command = SshCommandConstants.CPU_USING;
        JschResult jschResult = null;
        try {
            try {
                jschResult = jschUtil.executeCommand(command, rootSession);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (0 != jschResult.getExitCode()) {
                log.error("Getting cpu usage failed, exit code: {}, log: {}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to get cpu usage");
            }

            return jschResult.getResult();
        } catch (IOException e) {
            log.error("Failed to get cpu usage", e);
            throw new OpsException("Failed to get cpu usage");
        }
    }

    private void doUnInstall(UnInstallContext unInstallContext, Boolean force) {
        try {
            UnInstallContext clone = ObjectUtil.clone(unInstallContext);
            clusterOpsProviderManager
                    .provider(clone.getOpsClusterEntity().getVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .uninstall(clone);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:0");
        } catch (Exception e) {
            if (Objects.nonNull(force) && force) {
                OpsClusterEntity opsClusterEntity = unInstallContext.getOpsClusterEntity();
                List<OpsClusterNodeEntity> opsClusterNodeEntityList = unInstallContext.getOpsClusterNodeEntityList();

                removeById(opsClusterEntity);
                opsClusterNodeService.removeByIds(opsClusterNodeEntityList);
            }
            log.error("Uninstall exception：", e);
            wsUtil.sendText(unInstallContext.getRetSession(), "FINAL_EXECUTE_EXIT_CODE:-1");
        }
    }


    private SoftwareEnv softwareEnvDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        SoftwareEnv softwareEnv = new SoftwareEnv();

        List<EnvProperty> envProperties = new CopyOnWriteArrayList<>();
        softwareEnv.setEnvProperties(envProperties);

        CountDownLatch countDownLatch = new CountDownLatch(4);

        threadPoolTaskExecutor.submit(() -> {
            // software
            envProperties.add(dependencyPropertyDetect(session, expectedOs));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // firewalld
            envProperties.add(firewallPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // user
            envProperties.add(installUserPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // other
            envProperties.add(otherPropertyDetect(session));
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("waiting for thread to be interrupted", e);
        }

        envProperties.sort(Comparator.comparingInt(EnvProperty::getSortNum));

        return softwareEnv;
    }

    private EnvProperty otherPropertyDetect(Session session) {
        EnvProperty otherProperty = new EnvProperty();
        otherProperty.setName("other");
        otherProperty.setSortNum(4);
        otherProperty.setStatus(HostEnvStatusEnum.NORMAL);
        return otherProperty;
    }

    private EnvProperty installUserPropertyDetect(Session session) {
        EnvProperty installUserProperty = new EnvProperty();
        installUserProperty.setName("install user");
        installUserProperty.setSortNum(3);
        installUserProperty.setStatus(HostEnvStatusEnum.NORMAL);
        return installUserProperty;
    }

    private EnvProperty firewallPropertyDetect(Session session) {
        EnvProperty firewallProperty = new EnvProperty();
        firewallProperty.setName("firewall");
        firewallProperty.setSortNum(2);

        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.FIREWALL, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String firewallStatus = jschResult.getResult();
                try {

                    if ("inactive".equals(firewallStatus)) {
                        firewallProperty.setStatus(HostEnvStatusEnum.NORMAL);
                    } else {
                        firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
                        firewallProperty.setStatusMessage("Please turn off the firewall");
                    }
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
                    firewallProperty.setStatusMessage("Please turn off the firewall");
                }
            } else {
                firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
                firewallProperty.setStatusMessage("Please turn off the firewall");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
            firewallProperty.setStatusMessage("Please turn off the firewall");
        }

        return firewallProperty;
    }

    private EnvProperty dependencyPropertyDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        EnvProperty dependencyProperty = new EnvProperty();
        dependencyProperty.setName("software dependency");
        dependencyProperty.setSortNum(1);
        try {
            JschResult jschResult = jschUtil.executeCommand(SshCommandConstants.DEPENDENCY, session);
            List<String> dependencyPackages = Arrays.stream(dependencyPackageNames).map(
                    dependency -> dependency + "." + expectedOs.getCpuArch()).collect(Collectors.toList());
            String dependency = jschResult.getResult();
            List<String> notInstalledPackages = new ArrayList<>();
            for (String dependencyPackage : dependencyPackages) {
                if (!dependency.contains(dependencyPackage)) {
                    notInstalledPackages.add(dependencyPackage);
                }
            }
            dependencyProperty.setStatus(HostEnvStatusEnum.NORMAL);
            if (!notInstalledPackages.isEmpty()) {
                dependencyProperty.setStatus(HostEnvStatusEnum.ERROR);
                dependencyProperty.setStatusMessage("not installed dependencies:"
                        + StringUtils.join(notInstalledPackages, ","));
            }
        } catch (Exception e) {
            log.error("Execute command exception：", e);
            dependencyProperty.setStatus(HostEnvStatusEnum.ERROR);
            dependencyProperty.setStatusMessage(e.getMessage());
        }
        return dependencyProperty;
    }

    private HardwareEnv hardwareEnvDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        HardwareEnv hardwareEnv = new HardwareEnv();
        List<EnvProperty> envProperties = new CopyOnWriteArrayList<>();
        hardwareEnv.setEnvProperties(envProperties);

        CountDownLatch countDownLatch = new CountDownLatch(6);

        threadPoolTaskExecutor.submit(() -> {
            // os
            envProperties.add(osPropertyDetect(session, expectedOs));
            countDownLatch.countDown();
        });


        threadPoolTaskExecutor.submit(() -> {
            // os version
            envProperties.add(osVersionPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // memory
            envProperties.add(freeMemoryPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // CPU Core Num
            envProperties.add(cpuCoreNumPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // CPU
            envProperties.add(cpuFrequencyPropertyDetect(session));
            countDownLatch.countDown();
        });

        threadPoolTaskExecutor.submit(() -> {
            // Disk
            envProperties.add(freeHardDiskPropertyDetect(session));
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("waiting for thread to be interrupted", e);
        }

        envProperties.sort(Comparator.comparingInt(EnvProperty::getSortNum));

        return hardwareEnv;
    }

    private EnvProperty freeHardDiskPropertyDetect(Session session) {
        EnvProperty freeHardDiskProperty = new EnvProperty();
        freeHardDiskProperty.setName("free hard disk space");
        freeHardDiskProperty.setSortNum(6);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.FREE_HARD_DISK, session);
            } catch (InterruptedException e) {
                throw new OpsException("waiting for thread to be interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String freeHardDisk = jschResult.getResult();
                try {
                    int freeHardDiskGB = calcDisk(freeHardDisk);
                    freeHardDiskProperty.setValue(freeHardDiskGB + "G");

                    freeHardDiskProperty.setStatus(HostEnvStatusEnum.NORMAL);

                    int suggestedNum = 2;
                    if (freeHardDiskGB < suggestedNum) {
                        freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
                        freeHardDiskProperty.setStatusMessage("min 2.0GB");
                    }
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
                    freeHardDiskProperty.setStatusMessage("min 2.0GB");
                }
            } else {
                freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
                freeHardDiskProperty.setStatusMessage("min 2.0GB");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
            freeHardDiskProperty.setStatusMessage("min 2.0GB");
        }
        return freeHardDiskProperty;
    }

    private Integer calcDiskUsed(String diskInfo, String path) {
        Integer res = 0;
        String[] split = diskInfo.split("\n");

        for (String s : split) {
            String[] s1 = s.replaceAll( "\\s+", " " ).split(" ");
            if ("/".equals(s1[5])) {
                return Integer.parseInt(s1[4].substring(0, s1[4].length()-1));
            }

            if (path.contains(s1[5])) {
                return Integer.parseInt(s1[4].substring(0, s1[4].length() - 1));
            }
        }

        if (res == 0) {
            for (String s : split) {
                String[] s1 = s.replaceAll( "\\s+", " " ).split(" ");
                int used = Integer.parseInt(s1[4].substring(0, s1[4].length() - 1));
                if (used > res) {
                    res = used;
                }
            }
        }

        return res;
    }

    private int calcDisk(String freeHardDisk) {
        Integer res = 0;
        String[] split = freeHardDisk.split("\n");
        for (String s : split) {
            try {
                res += Integer.parseInt(s.replace("G", " ").trim());
            } catch (Exception ignore) {

            }
        }
        return res;
    }

    private EnvProperty cpuFrequencyPropertyDetect(Session session) {
        EnvProperty cpuFrequencyProperty = new EnvProperty();
        cpuFrequencyProperty.setName("CPU frequency");
        cpuFrequencyProperty.setSortNum(5);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.CPU_FREQUENCY, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String cpuFrequency = jschResult.getResult();
                try {
                    cpuFrequencyProperty.setValue(cpuFrequency);

                    double cpuCoreNum = Double.parseDouble(cpuFrequency.substring(0, cpuFrequency.length() - 3));
                    cpuFrequencyProperty.setStatus(HostEnvStatusEnum.NORMAL);

                    int suggestedNum = 2;
                    if (cpuCoreNum < suggestedNum) {
                        cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
                        cpuFrequencyProperty.setStatusMessage("min 2.0GHz");
                    }
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
                    cpuFrequencyProperty.setStatusMessage("min 2.0GHz");
                }
            } else {
                cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
                cpuFrequencyProperty.setStatusMessage("min 2.0GHz");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
            cpuFrequencyProperty.setStatusMessage("min 2.0GHz");
        }
        return cpuFrequencyProperty;
    }

    private EnvProperty cpuCoreNumPropertyDetect(Session session) {
        EnvProperty cpuCoreNumProperty = new EnvProperty();
        cpuCoreNumProperty.setName("Number of CPU cores");
        cpuCoreNumProperty.setSortNum(4);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.CPU_CORE_NUM, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String cpuCore = jschResult.getResult();
                try {
                    cpuCoreNumProperty.setValue(cpuCore);

                    int cpuCoreNum = Integer.parseInt(cpuCore);
                    cpuCoreNumProperty.setStatus(HostEnvStatusEnum.NORMAL);

                    int suggestedNum = 8;
                    if (cpuCoreNum < suggestedNum) {
                        cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
                        cpuCoreNumProperty.setStatusMessage("Minimum 8 cores");
                    }
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
                    cpuCoreNumProperty.setStatusMessage("Minimum 8 cores");
                }
            } else {
                cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
                cpuCoreNumProperty.setStatusMessage("Minimum 8 cores");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
            cpuCoreNumProperty.setStatusMessage("Minimum 8 cores");
        }
        return cpuCoreNumProperty;
    }

    private EnvProperty freeMemoryPropertyDetect(Session session) {
        EnvProperty freeMemoryProperty = new EnvProperty();
        freeMemoryProperty.setName("available memory");
        freeMemoryProperty.setSortNum(3);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.FREE_MEMORY, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String freeMemory = jschResult.getResult();
                try {
                    freeMemoryProperty.setValue(freeMemory + "GB");

                    int freeMemoryGB = Integer.parseInt(freeMemory);
                    freeMemoryProperty.setStatus(HostEnvStatusEnum.NORMAL);

                    int suggestedGB = 32;
                    if (freeMemoryGB < suggestedGB) {
                        freeMemoryProperty.setStatus(HostEnvStatusEnum.WARMING);
                        freeMemoryProperty.setStatusMessage("32GB or more is recommended");
                    }
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    freeMemoryProperty.setStatus(HostEnvStatusEnum.ERROR);
                    freeMemoryProperty.setStatusMessage("32GB or more is recommended");
                }
            } else {
                freeMemoryProperty.setStatus(HostEnvStatusEnum.ERROR);
                freeMemoryProperty.setStatusMessage("32GB or more is recommended");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            freeMemoryProperty.setStatus(HostEnvStatusEnum.ERROR);
            freeMemoryProperty.setStatusMessage("32GB or more is recommended");
        }

        return freeMemoryProperty;
    }

    private EnvProperty osVersionPropertyDetect(Session session) {
        EnvProperty osVersionProperty = new EnvProperty();
        osVersionProperty.setName("operating system version");
        osVersionProperty.setSortNum(2);
        try {
            JschResult jschResult = null;
            try {
                jschResult = jschUtil.executeCommand(SshCommandConstants.OS_VERSION, session);
            } catch (InterruptedException e) {
                throw new OpsException("thread is interrupted");
            }
            if (jschResult.getExitCode() == 0) {
                String osVersion = jschResult.getResult();
                try {
                    osVersionProperty.setValue(osVersion);
                    osVersionProperty.setStatus(HostEnvStatusEnum.NORMAL);
                } catch (Exception e) {
                    log.error("Parse command response error", e);
                    osVersionProperty.setStatus(HostEnvStatusEnum.ERROR);
                    osVersionProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
                }
            } else {
                osVersionProperty.setValue("unknown");
                osVersionProperty.setStatus(HostEnvStatusEnum.ERROR);
                osVersionProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
            }
        } catch (IOException e) {
            log.error("Parse command response error：", e);
            osVersionProperty.setStatus(HostEnvStatusEnum.ERROR);
            osVersionProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
        }

        return osVersionProperty;
    }

    private EnvProperty osPropertyDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        EnvProperty osProperty = new EnvProperty();
        osProperty.setName("operating system");
        osProperty.setSortNum(1);

        try {
            String os = getOS(session);
            String cpuArch = getCpuArch(session);
            osProperty.setValue(os);

            if (expectedOs.match(os, cpuArch)) {
                osProperty.setStatus(HostEnvStatusEnum.NORMAL);
            } else {
                osProperty.setStatus(HostEnvStatusEnum.ERROR);
                osProperty.setStatusMessage("The operating system does not match the installation package information");
            }

            if( !"centos".equalsIgnoreCase( os.trim() ) ) {
                osProperty.setStatus(HostEnvStatusEnum.WARMING);
                osProperty.setStatusMessage("Please check if the umask value is 0022");
            }
        } catch (Exception e) {
            log.error("Parse command response error：", e);

            osProperty.setStatus(HostEnvStatusEnum.ERROR);
            osProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
        }

        return osProperty;
    }

    private List<String> getAllFiles(String path) {
        List<String> fileList = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    fileList.addAll(getAllFiles(f.getPath()));
                } else {
                    try {
                        fileList.add(f.getCanonicalPath());
                    } catch (IOException ex) {
                        log.error("get file path error: " + ex.getMessage());
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public void batchConfigGucSetting(GucSettingDto gucSettingDto) {
        OpsClusterEntity clusterEntity = getById(gucSettingDto.getClusterId());
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(gucSettingDto.getHostId());
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = opsClusterNodeService.listClusterNodeByClusterId(clusterEntity.getClusterId());
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information is empty");
        }

        OpsClusterNodeEntity nodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getHostId().equals(hostEntity.getHostId())).findFirst().orElseThrow(() -> new OpsException("Cluster node configuration not found"));
        String installUserId = nodeEntity.getInstallUserId();
        OpsHostUserEntity userEntity = hostUserFacade.getById(installUserId);
        if (Objects.isNull(userEntity)) {
            throw new OpsException("Installation user information does not exist");
        }
        Session session = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(), encryptionUtils.decrypt(userEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish session with host"));

        boolean needRestart = false;
        for (GucSettingVO settingVO: gucSettingDto.getSettings()) {
            if (!settingVO.getHasChanged()) {
                continue;
            }
            if (settingVO.getContext().equals(GucSettingContextEnum.POSTMASTER.getCode())) {
                needRestart = true;
            }
            String dataPath = gucSettingDto.getDataPath();
            if (clusterEntity.getVersion().equals(OpenGaussVersionEnum.MINIMAL_LIST)) {
                dataPath = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Master node configuration not found")).getDataPath();
                if (clusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                    dataPath = dataPath + "/master";
                } else {
                    dataPath = dataPath + "/single_node";
                }
            }
            clusterOpsProviderManager
                    .provider(clusterEntity.getVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .configGucSetting(jschUtil, session, clusterEntity, gucSettingDto.getIsApplyToAllNode(), dataPath, settingVO);
        }
        if (needRestart) {
            OpsClusterBody clusterBody = new OpsClusterBody();
            clusterBody.setClusterId(clusterEntity.getClusterId());
            clusterBody.setSync(true);
            List<String> nodeIds = new ArrayList<>();
            opsClusterNodeEntities.forEach((item) -> {
                nodeIds.add(item.getClusterNodeId());
            });
            clusterBody.setNodeIds(nodeIds);
            try {
                restart(clusterBody);
            } catch (OpsException ex) {
                String errMsg = "Restart cluster failed: " + ex.getMessage();
                log.error(errMsg);
                throw new OpsException(errMsg);
            }
        }
    }

    @Override
    public List<GucSettingVO> getGucSettingList(String clusterId, String hostId) throws OpsException {
        Connection connection = null;
        OpsClusterEntity clusterEntity = getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }
        try {
            connection = DBUtil.getSession(hostEntity.getPublicIp(), clusterEntity.getPort(), clusterEntity.getDatabaseUsername(), clusterEntity.getDatabasePassword()).orElseThrow(() -> new OpsException("Unable to connect to the database"));
            return queryGucSettingList(connection);
        } catch (SQLException | ClassNotFoundException e) {
            String errMsg = "get connection fail: " + e.getMessage();
            log.error(errMsg);
            throw new OpsException(errMsg);
        } finally {
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    log.error("close connection failed: " + ex.getMessage());
                }
            }
        }
    }

    private List<GucSettingVO> queryGucSettingList(Connection connection) throws OpsException {
        List<GucSettingVO> res = new ArrayList<>();
        String sql = "select * from pg_settings";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GucSettingVO gucSettingVO = new GucSettingVO();
                gucSettingVO.setName(resultSet.getString("name"));
                gucSettingVO.setValue(resultSet.getString("setting"));
                gucSettingVO.setVarType(resultSet.getString("vartype"));
                gucSettingVO.setUnit(resultSet.getString("unit"));
                gucSettingVO.setMinVal(resultSet.getString("min_val"));
                gucSettingVO.setMaxVal(resultSet.getString("max_val"));
                gucSettingVO.setShortDesc(resultSet.getString("short_desc"));
                gucSettingVO.setExtraDesc(resultSet.getString("extra_desc"));
                gucSettingVO.setEnumVals(resultSet.getString("enumvals"));
                gucSettingVO.setDefaultVal(resultSet.getString("boot_val"));
                gucSettingVO.setContext(resultSet.getString("context"));
                res.add(gucSettingVO);
            }
        } catch (SQLException ex) {
            String errMsg = "Querying guc setting failed: " + ex.getMessage();
            log.error(errMsg);
            throw new OpsException(errMsg);
        }
        return res;
    }
}
