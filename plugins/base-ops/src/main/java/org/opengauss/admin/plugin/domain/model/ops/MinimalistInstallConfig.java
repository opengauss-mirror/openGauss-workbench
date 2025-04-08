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
 * MinimalistInstallConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/MinimalistInstallConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import lombok.Data;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimalist installation configuration
 *
 * @author lhf
 * @date 2022/8/4 23:14
 **/
@Data
public class MinimalistInstallConfig implements ClusterInstallConfig {
    private String installPackagePath;

    private Integer port;

    private String databaseUsername;

    private String databasePassword;

    private List<MinimalistInstallNodeConfig> nodeConfigList;

    @Override
    public void checkConfig() {
        if (StrUtil.isEmpty(databasePassword)) {
            throw new OpsException("The database password format is incorrect");
        }

        if (CollUtil.isEmpty(nodeConfigList)) {
            throw new OpsException("The cluster node is incorrectly configured");
        }

        for (MinimalistInstallNodeConfig minimalistInstallNodeConfig : nodeConfigList) {
            minimalistInstallNodeConfig.checkConfig();
        }
    }

    /**
     * toOpsClusterNodeEntityList
     *
     * @param opsClusterEntity OpsClusterEntity
     * @return List<OpsClusterNodeEntity>
     */
    public List<OpsClusterNodeEntity> toOpsClusterNodeEntityList(OpsClusterEntity opsClusterEntity) {
        ArrayList<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();
        for (MinimalistInstallNodeConfig nodeConfig : nodeConfigList) {
            String completeDataPath = nodeConfig.getInstallPath() + "/data";
            if (StrUtil.isEmpty(nodeConfig.getDataPath())) {
                if (opsClusterEntity.getDeployType() == DeployTypeEnum.CLUSTER) {
                    String masterDataPath = completeDataPath + "/master";
                    nodeConfig.setDataPath(masterDataPath);
                    nodeConfig.setClusterRole(ClusterRoleEnum.MASTER);
                    opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
                    String slaveDataPath = completeDataPath + "/slave";
                    nodeConfig.setDataPath(slaveDataPath);
                    nodeConfig.setClusterRole(ClusterRoleEnum.SLAVE);
                    opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
                } else {
                    completeDataPath = completeDataPath + "/single_node";
                    nodeConfig.setDataPath(completeDataPath);
                    opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
                }
            } else {
                opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
            }
        }
        return opsClusterNodeEntities;
    }
}
