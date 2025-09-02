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
 * ImportClusterBody.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/ImportClusterBody.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/10/8 15:47
 **/
@Data
public class ImportClusterBody {

    private OpenGaussVersionEnum openGaussVersion;

    private String openGaussVersionNum;

    private InstallModeEnum installMode;

    private DeployTypeEnum deployType;

    private EnterpriseInstallConfig enterpriseInstallConfig;

    private MinimalistInstallConfig minimalistInstallConfig;

    private LiteInstallConfig liteInstallConfig;

    private String envPath;

    private String xmlConfigPath;

    private String clusterId;

    private String clusterName;


    public void checkConfig() {
        if (Objects.isNull(openGaussVersion)) {
            throw new OpsException("The OpenGauss version is incorrect");
        }

        if (Objects.isNull(installMode)) {
            throw new OpsException("Incorrect installation mode");
        }

        if (Objects.isNull(deployType)) {
            throw new OpsException("Incorrect deployment mode");
        }

        if (StrUtil.isEmpty(clusterId)) {
            throw new OpsException("Cluster ID error");
        }

        boolean clusterDeploy = deployType == DeployTypeEnum.CLUSTER;

        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            if (Objects.isNull(enterpriseInstallConfig)) {
                throw new OpsException("The enterprise version is incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(enterpriseInstallConfig.getNodeConfigList()) ? 0 :
                    enterpriseInstallConfig.getNodeConfigList().size();
            if (clusterDeploy && enterpriseInstallConfig.getIsInstallCM() && nodeSize < 2) {
                throw new OpsException("In cluster mode, a maximum of two nodes can be installed");
            }

            if (StrUtil.isEmpty(enterpriseInstallConfig.getDatabaseUsername())) {
                throw new OpsException("The user name cannot be empty");
            }

            enterpriseInstallConfig.checkConfig();
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            if (Objects.isNull(minimalistInstallConfig)) {
                throw new OpsException("The minimalist version was incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(minimalistInstallConfig.getNodeConfigList()) ? 0 :
                    minimalistInstallConfig.getNodeConfigList().size();
            if (nodeSize > 1) {
                throw new OpsException("The minimalist version can only be installed on a single host");
            }

            if (StrUtil.isEmpty(minimalistInstallConfig.getDatabaseUsername())) {
                throw new OpsException("The user name cannot be empty");
            }

            minimalistInstallConfig.checkConfig();
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            if (Objects.isNull(liteInstallConfig)) {
                throw new OpsException("The lightweight version was incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(liteInstallConfig.getNodeConfigList()) ? 0 :
                    liteInstallConfig.getNodeConfigList().size();
            if (clusterDeploy && nodeSize < 2) {
                throw new OpsException("In cluster mode, a maximum of two nodes can be installed");
            }

            if (StrUtil.isEmpty(liteInstallConfig.getDatabaseUsername())) {
                throw new OpsException("The user name cannot be empty");
            }

            liteInstallConfig.checkConfig();
        }
    }

    public OpsClusterEntity toOpsClusterEntity() {
        OpsClusterEntity opsClusterEntity = new OpsClusterEntity();
        opsClusterEntity.setCreateTime(new Date());
        opsClusterEntity.setClusterId(clusterId);
        opsClusterEntity.setVersion(openGaussVersion);
        opsClusterEntity.setVersionNum(openGaussVersionNum);
        opsClusterEntity.setInstallMode(installMode);
        opsClusterEntity.setDeployType(deployType);
        opsClusterEntity.setClusterName(clusterName);
        if (!StrUtil.isEmpty(envPath)) {
            opsClusterEntity.setEnvPath(envPath);
        }
        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            opsClusterEntity.setDatabaseUsername(enterpriseInstallConfig.getDatabaseUsername());
            opsClusterEntity.setDatabasePassword(enterpriseInstallConfig.getDatabasePassword());
            opsClusterEntity.setInstallPath(enterpriseInstallConfig.getInstallPath());
            opsClusterEntity.setLogPath(enterpriseInstallConfig.getLogPath());
            opsClusterEntity.setTmpPath(enterpriseInstallConfig.getTmpPath());
            opsClusterEntity.setOmToolsPath(enterpriseInstallConfig.getOmToolsPath());
            opsClusterEntity.setCorePath(enterpriseInstallConfig.getCorePath());
            opsClusterEntity.setPort(enterpriseInstallConfig.getPort());
            boolean isDcfEnable = enterpriseInstallConfig.getEnableDCF();
            opsClusterEntity.setEnableDcf(isDcfEnable);
            if (isDcfEnable) {
                opsClusterEntity.setDcfPort(enterpriseInstallConfig.getDcfPort());
            }
            opsClusterEntity.setInstallPackagePath(enterpriseInstallConfig.getInstallPackagePath());
            if (StrUtil.isEmpty(xmlConfigPath)) {
                opsClusterEntity.setXmlConfigPath(enterpriseInstallConfig.getInstallPackagePath()
                        + "/cluster_config.xml");
            } else {
                opsClusterEntity.setXmlConfigPath(xmlConfigPath);
            }
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            opsClusterEntity.setPort(liteInstallConfig.getPort());
            opsClusterEntity.setDatabaseUsername(liteInstallConfig.getDatabaseUsername());
            opsClusterEntity.setDatabasePassword(liteInstallConfig.getDatabasePassword());
            opsClusterEntity.setInstallPackagePath(liteInstallConfig.getInstallPackagePath());
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            opsClusterEntity.setPort(minimalistInstallConfig.getPort());
            opsClusterEntity.setDatabaseUsername(minimalistInstallConfig.getDatabaseUsername());
            opsClusterEntity.setDatabasePassword(minimalistInstallConfig.getDatabasePassword());
            opsClusterEntity.setInstallPackagePath(minimalistInstallConfig.getInstallPackagePath());
        }
        return opsClusterEntity;
    }
}
