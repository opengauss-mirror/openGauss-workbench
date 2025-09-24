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
 * TaskLiteProvider.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/provider/ProviderManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.UploadInfo;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.enums.ops.OpsClusterTaskStatusEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsPackageManagerEntity;
import org.opengauss.admin.plugin.domain.model.ops.HostInfoHolder;
import org.opengauss.admin.plugin.domain.model.ops.InstallContext;
import org.opengauss.admin.plugin.domain.model.ops.RetBuffer;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.factory.OperateLogFactory;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskNodeService;
import org.opengauss.admin.plugin.service.ops.IOpsClusterTaskService;
import org.opengauss.admin.plugin.service.ops.IOpsPackageManagerV2Service;
import org.opengauss.admin.plugin.service.ops.impl.ClusterTaskProviderManager;
import org.opengauss.admin.plugin.service.ops.impl.function.InstallContextConfigFunction;
import org.opengauss.admin.plugin.service.ops.impl.function.InstallContextConfigFunctionInstance;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ProviderManager
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Service
public class ProviderManager {
    private static final String TASK_PROVIDER_NAME = "business_InstallProvider_";
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    private BlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();
    @Resource
    private InstallContextConfigFunctionInstance installContextConfigFunctionInstance;
    @Resource
    private ClusterTaskProviderManager clusterTaskProviderManager;
    @Resource
    private IOpsPackageManagerV2Service opsPackageManagerV2Service;

    @Resource
    @Lazy
    private IOpsClusterTaskNodeService opsClusterTaskNodeService;
    @Resource
    @Lazy
    private IOpsClusterTaskService opsClusterTaskService;

    /**
     * add task to queue
     *
     * @param taskId taskId
     */
    public void addClusterTask(String taskId) {
        taskQueue.add(taskId);
    }

    /**
     * current task queue is not empty, and the current provider task count is less than 5
     *
     * @return boolean
     */
    public boolean hasLimitResource() {
        return !taskQueue.isEmpty() && TaskManager.executeCount(TASK_PROVIDER_NAME) < 5;
    }

    /**
     * Retrieve the local path corresponding to the saved binary installation package for the specified packageId
     *
     * @param packageId packageId
     * @return package local storage path
     */
    private String getPackageLocalPath(String packageId) {
        OpsPackageManagerEntity packageEntity = opsPackageManagerV2Service.getById(packageId);
        Assert.isTrue(Objects.nonNull(packageEntity), "package " + packageId + " not exist");
        UploadInfo packagePath = packageEntity.getPackagePath();
        return packagePath.getRealPath() + "/" + packagePath.getName();
    }

    /**
     * Check whether there is a task waiting in the queue
     *
     * @return boolean
     */
    public boolean hasTaskWaiting() {
        return !taskQueue.isEmpty();
    }

    /**
     * Retrieve the taskId from the queue
     *
     * @return task
     */
    public String poll() {
        return taskQueue.poll();
    }

    /**
     * Retrieve the businessId based on the clusterId
     *
     * @param clusterId clusterId
     * @return businessId
     */
    public String getBusinessId(String clusterId) {
        return "business_InstallProvider_" + clusterId;
    }

    /**
     * populate the installContext
     *
     * @param task task
     * @return installContext
     */
    public InstallContext populateInstallContext(OpsClusterTaskEntity task) {
        InstallContext installContext = new InstallContext();
        try {
            installContext.setClusterId(task.getClusterId());
            installContext.setOpenGaussVersion(task.getVersion());
            installContext.setOpenGaussVersionNum(task.getVersionNum());
            installContext.setInstallMode(InstallModeEnum.OFF_LINE);
            installContext.setInstallPackagePath(task.getInstallPackagePath());
            OpsHostUserEntity installUser = hostUserFacade.getById(task.getHostUserId());
            installContext.setInstallUsername(installUser.getUsername());
            installContext.setInstallPackageLocalPath(getPackageLocalPath(task.getPackageId()));
            installContext.setEnvPath(task.getEnvPath());
            installContext.setDeployType(task.getDeployType());
            String clusterId = installContext.getClusterId();
            List<OpsClusterTaskNodeEntity> taskNodeList = opsClusterTaskNodeService.listByClusterTaskId(clusterId);
            populateInstallEnvPath(installContext);
            populateInstallContextHostHolder(installContext, taskNodeList);
            populateInstallContextOs(installContext);
            populateInstallContextInstallConfig(installContext, task, taskNodeList);
        } catch (Exception e) {
            throw new OpsException("Failed to populate install context , error : " + e.getMessage());
        }
        return installContext;
    }

    private void populateInstallEnvPath(InstallContext installContext) {
        String envPath = installContext.getEnvPath();
        if (StrUtil.isEmpty(envPath)) {
            envPath = SshCommandConstants.DEFAULT_ENV_BASHRC;
            installContext.setEnvPath(envPath);
        }
        if (SshCommandConstants.DEFAULT_ENV_BASHRC.equals(envPath)) {
            installContext.setEnvAbsolutePath("/home/" + installContext.getInstallUsername() + "/.bashrc");
        } else {
            installContext.setEnvAbsolutePath(envPath);
        }
    }

    private void populateInstallContextInstallConfig(InstallContext installContext, OpsClusterTaskEntity task,
                                                     List<OpsClusterTaskNodeEntity> taskNodeList) {
        OpenGaussVersionEnum version = installContext.getOpenGaussVersion();
        InstallContextConfigFunction installContextConfig =
                installContextConfigFunctionInstance.getInstallContextConfig(version);
        installContextConfig.accept(installContext, task, taskNodeList);
    }

    /**
     * Fill in the os information in the installation context according to the context HostHolder .
     * must be called after populateInstallContextHostHolder
     *
     * @param installContext installation context
     */
    private void populateInstallContextOs(InstallContext installContext) {
        List<HostInfoHolder> hostInfoHolders = installContext.getHostInfoHolders();
        Assert.isTrue(CollUtil.isNotEmpty(hostInfoHolders), "HostInfoHolder is empty");
        OpsHostEntity host = hostInfoHolders.get(0).getHostEntity();
        Assert.isTrue(Objects.nonNull(host), "HostEntity is null");
        installContext.setOs(OpenGaussSupportOSEnum.of(host.getOs(), host.getOsVersion(), host.getCpuArch()));
    }

    private void populateInstallContextHostHolder(InstallContext installContext,
                                                  List<OpsClusterTaskNodeEntity> entityNodeList) {
        List<HostInfoHolder> hostInfoHolders = new ArrayList<>();
        Set<String> hostIdSet = new HashSet<>();
        entityNodeList.forEach(node -> {
            Assert.isTrue(!hostIdSet.contains(node.getHostId()), "HostId is repeated");
            hostIdSet.add(node.getHostId());
            OpsHostEntity host = hostFacade.getById(node.getHostId());
            List<OpsHostUserEntity> hostUserList = hostUserFacade.listHostUserByHostId(node.getHostId());
            HostInfoHolder hostInfoHolder = new HostInfoHolder();
            hostInfoHolder.setHostEntity(host);
            hostInfoHolder.setHostUserEntities(hostUserList);
            hostInfoHolders.add(hostInfoHolder);
        });
        installContext.setHostInfoHolders(hostInfoHolders);
    }

    /**
     * do install task base on installContext
     *
     * @param installContext installContext
     */
    public void doInstall(InstallContext installContext) {
        String clusterId = installContext.getClusterId();
        RetBuffer retBuffer = installContext.getRetBuffer();
        boolean isInstallSucc = true;
        String remark = "";
        try {
            retBuffer.sendText("START_EXECUTE_INSTALL_PROVIDER");
            clusterTaskProviderManager
                    .provider(installContext.getOpenGaussVersion())
                    .orElseThrow(() -> new OpsException("The current version does not support"))
                    .install(installContext);
            retBuffer.sendText("FINAL_EXECUTE_EXIT_CODE:0");
            OperateLogFactory.operateInstall(clusterId, retBuffer.getRetBuffer());
        } catch (Exception e) {
            retBuffer.sendText(e.toString());
            retBuffer.sendText("FINAL_EXECUTE_EXIT_CODE:-1");
            OperateLogFactory.operateInstall(clusterId, retBuffer.getRetBuffer());
            isInstallSucc = false;
            remark = e.getMessage();
            log.error("doInstall error:", e);
            throw new OpsException(e.getMessage());
        } finally {
            if (!isInstallSucc) {
                opsClusterTaskService.updateClusterTaskStatus(clusterId, OpsClusterTaskStatusEnum.FAILED, remark);
            } else {
                opsClusterTaskService.updateClusterTaskStatus(clusterId, OpsClusterTaskStatusEnum.SUCCESS,
                        "Install completed");
            }
        }
    }

    /**
     * force remove install task
     *
     * @param taskId taskId
     */
    public void remove(String taskId) {
        taskQueue.remove(taskId);
    }
}
