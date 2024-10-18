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
 * EnterpriseInstallNodeConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/node/EnterpriseInstallNodeConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops.node;

import cn.hutool.core.util.StrUtil;

import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;

import lombok.Data;

import java.util.Objects;

/**
 * Enterprise Edition Installation Node Configuration
 *
 * @author lhf
 * @date 2022/8/4 23:16
 **/
@Data
public class EnterpriseInstallNodeConfig {
    /**
     * Cluster role
     */
    private ClusterRoleEnum clusterRole;
    /**
     * host ID
     */
    private String hostId;

    private String rootPassword;
    /**
     * Public IP
     */
    private String publicIp;
    /**
     * Intranet IP
     */
    private String privateIp;
    /**
     * hostname
     */
    private String hostname;

    private String installUserId;

    private String installUsername;

    private Boolean isCMMaster;

    private String cmDataPath;

    private Integer cmPort;

    private Integer dcfPort;

    private String dataPath;

    private String azName;

    private String azPriority;

    /**
     * Check configuration
     *
     * @param isInstallCM isInstallCM
     */
    public void checkConfig(Boolean isInstallCM) {
        if (Objects.isNull(clusterRole)) {
            throw new OpsException("Cluster role error");
        }
        if (StrUtil.isEmpty(hostId)) {
            throw new OpsException("wrong host id");
        }
        if (StrUtil.isEmpty(publicIp)) {
            throw new OpsException("public IP error");
        }
        if (StrUtil.isEmpty(privateIp)) {
            throw new OpsException("Intranet IP error");
        }
        if (StrUtil.isEmpty(hostname)) {
            throw new OpsException("wrong hostname");
        }
        if (StrUtil.isEmpty(installUserId)) {
            throw new OpsException("install user error");
        }
        if (isInstallCM && StrUtil.isEmpty(cmDataPath)) {
            throw new OpsException("cm data path error");
        }
        if (StrUtil.isEmpty(dataPath)) {
            throw new OpsException("[" + hostname + "]data path error");
        }
    }

    public OpsClusterNodeEntity toOpsClusterNodeEntity() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId(StrUtil.uuid());
        opsClusterNodeEntity.setClusterRole(clusterRole);
        opsClusterNodeEntity.setHostId(hostId);
        opsClusterNodeEntity.setInstallUserId(installUserId);
        opsClusterNodeEntity.setDataPath(dataPath);
        opsClusterNodeEntity.setIsCMMaster(isCMMaster);
        opsClusterNodeEntity.setCmDataPath(cmDataPath);
        opsClusterNodeEntity.setCmPort(cmPort);
        return opsClusterNodeEntity;
    }
}
