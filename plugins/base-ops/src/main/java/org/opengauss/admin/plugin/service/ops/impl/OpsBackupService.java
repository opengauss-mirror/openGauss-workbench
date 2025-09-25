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
 * OpsBackupService.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsBackupService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsBackupEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterBody;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.mapper.ops.OpsBackupMapper;
import org.opengauss.admin.plugin.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.plugin.utils.WsUtil;
import org.opengauss.admin.plugin.vo.ops.BackupInputDto;
import org.opengauss.admin.plugin.vo.ops.BackupVO;
import org.opengauss.admin.plugin.vo.ops.RecoverInputDto;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.service.ops.IOpsBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * @author lhf
 * @date 2022/11/5 09:48
 **/
@Slf4j
@Service
public class OpsBackupService extends ServiceImpl<OpsBackupMapper, OpsBackupEntity> implements IOpsBackupService {
    @Autowired
    private IOpsClusterService clusterService;
    @Autowired
    private IOpsClusterNodeService clusterNodeService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private OpsBackupMapper backupMapper;
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private  WsConnectorManager wsConnectorManager;
    @Override
    public void backup(BackupInputDto backup) {
        String clusterId = backup.getClusterId();

        OpsClusterEntity clusterEntity = clusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        List<OpsClusterNodeEntity> opsClusterNodeEntities = clusterNodeService.listClusterNodeByClusterId(clusterId);
        if (CollUtil.isEmpty(opsClusterNodeEntities)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsClusterNodeEntity masterNodeEntity = opsClusterNodeEntities.stream().filter(node -> node.getClusterRole() == ClusterRoleEnum.MASTER).findFirst().orElseThrow(() -> new OpsException("Node information not found"));
        String hostId = masterNodeEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("Master node host information not found");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(masterNodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Master node installation user information not found");
        }

        WsSession retWsSession = wsConnectorManager.getSession(backup.getBusinessId()).orElseThrow(()->new OpsException("websocket session not exist"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);

            try {
                String name = StrUtil.uuid() + ".sql";
                doBackUp(clusterEntity, hostEntity, installUserEntity, backup, retWsSession, name,clusterEntity.getEnvPath());
                wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");

                OpsBackupEntity opsBackupEntity = new OpsBackupEntity();
                opsBackupEntity.setClusterId(clusterId);
                opsBackupEntity.setHostId(hostId);
                opsBackupEntity.setClusterNodeId(masterNodeEntity.getClusterNodeId());
                opsBackupEntity.setRemark(backup.getRemark());
                opsBackupEntity.setBackupPath(backup.getBackupPath() + "/" + name);
                opsBackupEntity.setCreateTime(new Date());
                save(opsBackupEntity);
            } catch (Exception e) {
                log.error("Backup failed：", e);
                wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            }
        });

        TaskManager.registry(backup.getBusinessId(), future);
    }

    @Override
    public void recover(String id, RecoverInputDto recover) {
        OpsBackupEntity backupEntity = getById(id);
        if (Objects.isNull(backupEntity)) {
            throw new OpsException("Backup information does not exist");
        }

        String clusterId = backupEntity.getClusterId();
        OpsClusterEntity clusterEntity = clusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        String hostId = backupEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        String clusterNodeId = backupEntity.getClusterNodeId();
        OpsClusterNodeEntity clusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsHostUserEntity rootUserEntity = hostUserFacade.getRootUserByHostId(hostId);
        if (Objects.isNull(rootUserEntity)) {
            throw new OpsException("root user information not found");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(clusterNodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("Installation user does not exist");
        }

        WsSession retWsSession = wsConnectorManager.getSession(recover.getBusinessId()).orElseThrow(()->new OpsException("websocket session not exist"));
        RequestAttributes context = RequestContextHolder.currentRequestAttributes();
        Future<?> future = threadPoolTaskExecutor.submit(() -> {
            RequestContextHolder.setRequestAttributes(context);

            try {
                doRecover(backupEntity, clusterEntity, clusterNodeEntity, hostEntity, rootUserEntity, installUserEntity,recover.getBusinessId(), retWsSession);
                wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:0");
            } catch (Exception e) {
                log.error("Recovery failed：", e);
                wsUtil.sendText(retWsSession, "FINAL_EXECUTE_EXIT_CODE:-1");
            }
        });

        TaskManager.registry(recover.getBusinessId(), future);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(String id) {
        OpsBackupEntity backupEntity = getById(id);
        if (Objects.isNull(backupEntity)) {
            throw new OpsException("Backup information does not exist");
        }

        String clusterId = backupEntity.getClusterId();
        OpsClusterEntity clusterEntity = clusterService.getById(clusterId);
        if (Objects.isNull(clusterEntity)) {
            throw new OpsException("Cluster information does not exist");
        }

        String hostId = backupEntity.getHostId();
        OpsHostEntity hostEntity = hostFacade.getById(hostId);
        if (Objects.isNull(hostEntity)) {
            throw new OpsException("host information does not exist");
        }

        String clusterNodeId = backupEntity.getClusterNodeId();
        OpsClusterNodeEntity clusterNodeEntity = clusterNodeService.getById(clusterNodeId);
        if (Objects.isNull(clusterNodeEntity)) {
            throw new OpsException("Cluster node information does not exist");
        }

        OpsHostUserEntity installUserEntity = hostUserFacade.getById(clusterNodeEntity.getInstallUserId());
        if (Objects.isNull(installUserEntity)) {
            throw new OpsException("install user information not found");
        }

        doDel(backupEntity, hostEntity, installUserEntity);

        removeById(backupEntity);
    }

    @Override
    public Page<BackupVO> pageBackup(Page page, String clusterId) {
        return backupMapper.pageBackup(page,clusterId);
    }

    private void doDel(OpsBackupEntity backupEntity, OpsHostEntity hostEntity, OpsHostUserEntity installUserEntity) {
        Session installUserSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        try {
            String command = "rm -rf " + backupEntity.getBackupPath();
            JschResult jschResult = jschUtil.executeCommand(command, installUserSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Failed to delete backup，exitCode:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to delete backup");
            }
        } catch (Exception e) {
            log.error("Failed to delete backup", e);
            throw new OpsException("Failed to delete backup");
        }finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()){
                installUserSession.disconnect();
            }
        }
    }

    private void doRecover(OpsBackupEntity backupEntity, OpsClusterEntity clusterEntity, OpsClusterNodeEntity clusterNodeEntity, OpsHostEntity hostEntity, OpsHostUserEntity rootUserEntity, OpsHostUserEntity installUserEntity, String businessId, WsSession retWsSession) {

        Session installUserSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));
        try {
            String chmodNewDataPath = String.format("gsql -d postgres -p %s -W %s -f %s",
                clusterEntity.getPort(), encryptionUtils.decrypt(clusterEntity.getDatabasePassword()),
                backupEntity.getBackupPath());
            JschResult jschResult = jschUtil.executeCommand(chmodNewDataPath, clusterEntity.getEnvPath(), installUserSession, retWsSession);
            if (0 != jschResult.getExitCode()) {
                log.error("Recovery failed，exitCode:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Recovery failed");
            }
        } catch (Exception e) {
            log.error("Recovery failed", e);
            throw new OpsException("Recovery failed");
        }finally {
            if (Objects.nonNull(installUserSession) && installUserSession.isConnected()){
                installUserSession.disconnect();
            }
        }

        OpsClusterBody opsClusterBody = new OpsClusterBody();
        opsClusterBody.setClusterId(clusterEntity.getClusterId());
        opsClusterBody.setBusinessId(businessId);
        opsClusterBody.setSync(Boolean.TRUE);
        clusterService.restart(opsClusterBody);
    }

    private void doBackUp(OpsClusterEntity clusterEntity, OpsHostEntity hostEntity, OpsHostUserEntity installUserEntity, BackupInputDto backup, WsSession retWsSession, String name, String envPath) {
        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to establish connection with host"));

        try {
            String createPathCommand = MessageFormat.format(SshCommandConstants.CREATE_DIR_AND_CHECK_WRITE_PERM,
                    backup.getBackupPath(), "not allow write backup file");
            JschResult jschResult = jschUtil.executeCommand(createPathCommand, rootSession, retWsSession);
            if (jschResult.getExitCode() != 0 || jschResult.getResult().contains("not allow write backup file")) {
                log.error("Failed to create backup directory，exitCode:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Failed to create backup directory");
            }
        } catch (Exception e) {
            log.error("Failed to create backup directory", e);
            throw new OpsException("Failed to create backup directory");
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()) {
                rootSession.disconnect();
            }
        }

        Session installSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), installUserEntity.getUsername(), encryptionUtils.decrypt(installUserEntity.getPassword())).orElseThrow(() -> new OpsException("Failed to connect to master node host"));
        try {
            String backupCommand = "gs_dumpall -p " + clusterEntity.getPort() + " -f " + backup.getBackupPath() + "/" + name;
            JschResult jschResult = jschUtil.executeCommand(backupCommand, envPath, installSession, retWsSession);
            if (jschResult.getExitCode() != 0) {
                log.error("Backup failed，exitCode:{},msg:{}", jschResult.getExitCode(), jschResult.getResult());
                throw new OpsException("Backup failed");
            }
        } catch (Exception e) {
            log.error("Backup failed", e);
            throw new OpsException("Backup failed");
        } finally {
            if (Objects.nonNull(installSession) && installSession.isConnected()) {
                installSession.disconnect();
            }
        }
    }
}
