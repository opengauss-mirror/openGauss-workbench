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
 * MinimalistInstallNodeConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/node/MinimalistInstallNodeConfig.java
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
 * Minimalist installation node configuration
 *
 * @author lhf
 * @date 2022/8/4 23:17
 **/
@Data
public class MinimalistInstallNodeConfig {

    private ClusterRoleEnum clusterRole;

    private String hostId;

    private String rootPassword;

    private String installUserId;

    private String installPath;

    private Boolean isInstallDemoDatabase;

    public void checkConfig() {
        if (Objects.isNull(clusterRole)) {
            throw new OpsException("Node role error");
        }

        if (StrUtil.isEmpty(hostId)) {
            throw new OpsException("host information error");
        }

        if (StrUtil.isEmpty(installPath)) {
            throw new OpsException("installation directory error");
        }

        if (Objects.isNull(isInstallDemoDatabase)) {
            isInstallDemoDatabase = false;
        }
    }

    public OpsClusterNodeEntity toOpsClusterNodeEntity() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId(StrUtil.uuid());
        opsClusterNodeEntity.setClusterRole(clusterRole);
        opsClusterNodeEntity.setHostId(hostId);
        opsClusterNodeEntity.setInstallUserId(installUserId);
        opsClusterNodeEntity.setInstallPath(installPath);
        opsClusterNodeEntity.setDataPath(installPath+"/data");
        opsClusterNodeEntity.setPkgPath(installPath.substring(0, installPath.lastIndexOf("/")));
        opsClusterNodeEntity.setInstallDemoDatabase(isInstallDemoDatabase);
        return opsClusterNodeEntity;
    }
}
