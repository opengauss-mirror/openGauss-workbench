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
 * EnterpriseInstallConfig.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/EnterpriseInstallConfig.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.node.EnterpriseInstallNodeConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Enterprise Edition installation configuration
 *
 * @author lhf
 * @date 2022/8/4 23:16
 **/
@Data
public class EnterpriseInstallConfig implements ClusterInstallConfig {
    private String installPackagePath;

    private String installPath;

    private String logPath;

    private String tmpPath;

    private String omToolsPath;

    private String corePath;

    private Integer port;

    private Boolean enableDCF;

    private String databaseUsername;

    private String databasePassword;

    private Boolean isInstallCM;

    private List<EnterpriseInstallNodeConfig> nodeConfigList;

    /**
     * AZ Name
     */
    private String azName;

    @Override
    public void checkConfig() {
        if (StrUtil.isEmpty(installPath)) {
            throw new OpsException("wrong installation path");
        }

        if (StrUtil.isEmpty(logPath)) {
            throw new OpsException("Log path is incorrect");
        }

        if (StrUtil.isEmpty(tmpPath)) {
            throw new OpsException("The temporary file path is incorrect");
        }

        if (StrUtil.isEmpty(omToolsPath)) {
            throw new OpsException("om Path error");
        }

        if (StrUtil.isEmpty(corePath)) {
            throw new OpsException("Database core item error");
        }

        if (Objects.isNull(port)) {
            throw new OpsException("Port error");
        }

        if (Objects.isNull(enableDCF)) {
            enableDCF = false;
        }

        if (Objects.isNull(isInstallCM)) {
            throw new OpsException("Is it wrong to install CM");
        }

        if (CollUtil.isEmpty(nodeConfigList)) {
            throw new OpsException("The cluster node is incorrectly configured");
        }

        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            enterpriseInstallNodeConfig.checkConfig();
        }
    }

    public List<OpsClusterNodeEntity> toOpsClusterNodeEntityList() {
        List<OpsClusterNodeEntity> list = new ArrayList<>();

        for (EnterpriseInstallNodeConfig enterpriseInstallNodeConfig : nodeConfigList) {
            list.add(enterpriseInstallNodeConfig.toOpsClusterNodeEntity());
        }

        return list;
    }
}
