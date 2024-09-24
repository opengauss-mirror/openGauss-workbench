/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * InstallContextConfigFunctionInstance.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/function/InstallContextConfigFunctionInstance.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterTaskNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.*;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * InstallContextConfigFunctionInstance
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Service
public class InstallContextConfigFunctionInstance {
    private Map<OpenGaussVersionEnum, InstallContextConfigFunction> configFunctions = new HashMap<>();

    /**
     * 轻量版 LITE install context config function
     * cluster mode only support one primary and one standby, need to install on different nodes manually
     */
    private final InstallContextConfigFunction lite = (installContext, task, taskNodeList) -> {
        Assert.isTrue(taskNodeList.size() >= 1 && taskNodeList.size() <= 2,
                "lite cluster mode only support one primary and one standby, " +
                        "need to install on different nodes manually");
        LiteInstallConfig config = new LiteInstallConfig();
        config.setInstallPackagePath(task.getInstallPackagePath());
        config.setPort(task.getDatabasePort());
        config.setDatabaseUsername(task.getDatabaseUsername());
        config.setDatabasePassword(task.getDatabasePassword());
        List<LiteInstallNodeConfig> nodeConfigList = new ArrayList<>();
        Map<String, HostInfoHolder> hostInfoHolderMap = installContext.getHostInfoHolderMap();
        for (OpsClusterTaskNodeEntity node : taskNodeList) {
            LiteInstallNodeConfig nodeConfig = new LiteInstallNodeConfig();
            nodeConfig.setClusterRole(node.getNodeType());
            nodeConfig.setHostId(node.getHostId());
            nodeConfig.setInstallUserId(node.getHostUserId());
            nodeConfig.setRootPassword(getHostRootPassword(hostInfoHolderMap.get(node.getHostId())));
            nodeConfig.setInstallPath(task.getInstallPath());
            nodeConfig.setDataPath(node.getDataPath());
            nodeConfigList.add(nodeConfig);
        }
        config.setNodeConfigList(nodeConfigList);
        installContext.setLiteInstallConfig(config);
    };

    /**
     * 极简版 MINIMAL_LIST install context config function
     * cluster mode only support one master and one standby, config will be installed automatically on the same node
     */
    private final InstallContextConfigFunction minimal = (installContext, task, taskNodeList) -> {
        Assert.isTrue(taskNodeList.size() == 1,
                "minimal_list cluster mode only support one master and one standby, " +
                        "config will be installed automatically on the same node");
        MinimalistInstallConfig config = new MinimalistInstallConfig();
        config.setInstallPackagePath(task.getInstallPackagePath());
        config.setPort(task.getDatabasePort());
        config.setDatabaseUsername(task.getDatabaseUsername());
        config.setDatabasePassword(task.getDatabasePassword());
        List<MinimalistInstallNodeConfig> nodeConfigList = new ArrayList<>();
        Map<String, HostInfoHolder> hostInfoHolderMap = installContext.getHostInfoHolderMap();
        for (OpsClusterTaskNodeEntity node : taskNodeList) {
            MinimalistInstallNodeConfig nodeConfig = new MinimalistInstallNodeConfig();
            nodeConfig.setHostId(node.getHostId());
            nodeConfig.setInstallUserId(node.getHostUserId());
            nodeConfig.setRootPassword(getHostRootPassword(hostInfoHolderMap.get(node.getHostId())));
            nodeConfig.setInstallPath(task.getInstallPath());
            nodeConfig.setIsInstallDemoDatabase(true);
            nodeConfig.setClusterRole(node.getNodeType());
            nodeConfigList.add(nodeConfig);
            config.setNodeConfigList(nodeConfigList);
        }
        installContext.setMinimalistInstallConfig(config);
    };

    /**
     * 企业版 ENTERPRISE install context config function
     * cluster mode node list size must between 1 and 10
     */
    private final InstallContextConfigFunction enterprise = (installContext, task, taskNodeList) -> {
        Assert.isTrue(taskNodeList.size() >= 1 && taskNodeList.size() <= 10,
                "enterprise cluster mode node list size must between 1 and 10");
        EnterpriseInstallConfig config = new EnterpriseInstallConfig();
        BeanUtils.copyProperties(task, config);
        config.setPort(task.getDatabasePort());
        config.setDatabaseKernelArch(DatabaseKernelArch.MASTER_SLAVE);
        config.setIsInstallCM(task.getEnableCmTool());

        List<EnterpriseInstallNodeConfig> nodeConfigList = new ArrayList<>();
        Map<String, HostInfoHolder> hostInfoHolderMap = installContext.getHostInfoHolderMap();
        for (OpsClusterTaskNodeEntity node : taskNodeList) {
            EnterpriseInstallNodeConfig nodeConfig = new EnterpriseInstallNodeConfig();
            BeanUtils.copyProperties(node, nodeConfig);
            // cm tool will be install at tools path
            nodeConfig.setCmPort(node.getCmPort());
            nodeConfig.setIsCMMaster(node.getIsCmMaster());
            nodeConfig.setCmDataPath(node.getCmDataPath());
            HostInfoHolder hostInfoHolder = hostInfoHolderMap.get(node.getHostId());
            OpsHostEntity host = hostInfoHolder.getHostEntity();
            nodeConfig.setPublicIp(host.getPublicIp());
            nodeConfig.setPrivateIp(host.getPrivateIp());
            nodeConfig.setAzName(node.getAzOwner());
            nodeConfig.setHostname(host.getHostname());
            nodeConfig.setClusterRole(node.getNodeType());
            Map<String, OpsHostUserEntity> userMap = hostInfoHolder.getHostUserEntities().stream().collect(Collectors.toMap(OpsHostUserEntity::getHostUserId, Function.identity()));
            OpsHostUserEntity nodeUser = userMap.get(node.getHostUserId());
            nodeConfig.setInstallUserId(nodeUser.getHostUserId());
            nodeConfig.setInstallUsername(nodeUser.getUsername());
            nodeConfig.setRootPassword(getHostRootPassword(hostInfoHolder));
            nodeConfigList.add(nodeConfig);
        }
        config.setNodeConfigList(nodeConfigList);
        installContext.setEnterpriseInstallConfig(config);
    };

    /**
     * constructor
     */
    public InstallContextConfigFunctionInstance() {
        configFunctions.put(OpenGaussVersionEnum.LITE, lite);
        configFunctions.put(OpenGaussVersionEnum.MINIMAL_LIST, minimal);
        configFunctions.put(OpenGaussVersionEnum.ENTERPRISE, enterprise);
    }

    /**
     * get InstallContextConfig function by OpenGaussVersionEnum
     *
     * @param version version
     * @return check function
     */
    public InstallContextConfigFunction getInstallContextConfig(OpenGaussVersionEnum version) {
        return configFunctions.get(version);
    }

    private String getHostRootPassword(HostInfoHolder hostInfoHolder) {
        String hostId = hostInfoHolder.getHostEntity().getHostId();
        return hostInfoHolder.getHostUserEntities()
                .stream()
                .filter(user -> user.getUsername().equals("root"))
                .findAny().orElseThrow(() -> new OpsException("hostId:" + hostId + "not found root user"))
                .getPassword();
    }
}