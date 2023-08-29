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
 * LiteInstallConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/LiteInstallConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.node.LiteInstallNodeConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight edition installation configuration
 *
 * @author lhf
 * @date 2022/8/4 23:15
 **/
@Data
public class LiteInstallConfig implements ClusterInstallConfig {
    private String installPackagePath;
    private Integer port;
    private String databaseUsername;
    private String databasePassword;

    private List<LiteInstallNodeConfig> nodeConfigList;

    @Override
    public void checkConfig() {
        if (Objects.isNull(port)) {
            throw new OpsException("Incorrect port number");
        }
        if (StrUtil.isEmpty(databasePassword)) {
            throw new OpsException("The database password format is incorrect");
        }

        if (CollUtil.isEmpty(nodeConfigList)) {
            throw new OpsException("The cluster node is incorrectly configured");
        }

        for (LiteInstallNodeConfig liteInstallNodeConfig : nodeConfigList) {
            liteInstallNodeConfig.checkConfig();
        }
    }

    public List<OpsClusterNodeEntity> toOpsClusterNodeEntityList() {
        ArrayList<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();

        for (LiteInstallNodeConfig nodeConfig : nodeConfigList) {
            opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
        }

        return opsClusterNodeEntities;
    }
}
