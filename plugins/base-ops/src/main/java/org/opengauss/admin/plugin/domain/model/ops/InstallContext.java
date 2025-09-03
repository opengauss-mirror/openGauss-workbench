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
 * InstallContext.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/InstallContext.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.enums.ops.DatabaseKernelArch;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lhf
 * @date 2022/8/4 22:56
 **/
@Slf4j
@Data
public class InstallContext implements Cloneable {
    private String installUsername;
    private OpenGaussVersionEnum openGaussVersion;

    private String openGaussVersionNum;

    private OpenGaussSupportOSEnum os;

    private InstallModeEnum installMode;

    private DeployTypeEnum deployType;

    private String installPackagePath;
    private String installPackageLocalPath;

    private EnterpriseInstallConfig enterpriseInstallConfig;

    private MinimalistInstallConfig minimalistInstallConfig;

    private LiteInstallConfig liteInstallConfig;

    private String clusterId;

    private WsSession retSession;
    private RetBuffer retBuffer;

    private String envPath;
    private String envAbsolutePath;

    private List<HostInfoHolder> hostInfoHolders;

    public void checkConfig() {
        checkRetSessionConfig();
        checkTaskConfig();
    }

    public void checkTaskConfig() {
        if (Objects.isNull(openGaussVersion)) {
            throw new OpsException("The OpenGauss version is incorrect");
        }

        if (StrUtil.isEmpty(openGaussVersionNum)) {
            throw new OpsException("The OpenGauss version number is incorrect");
        }

        if (Objects.isNull(installMode)) {
            throw new OpsException("Incorrect installation mode");
        }

        if (Objects.isNull(deployType)) {
            throw new OpsException("Incorrect deployment mode");
        }

        if (StrUtil.isEmpty(installPackagePath)) {
            throw new OpsException("The installation package path is incorrect");
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
            if (enterpriseInstallConfig.getDatabaseKernelArch() == DatabaseKernelArch.MASTER_SLAVE) {
                try {
                    String[] splitVersionNums = openGaussVersionNum.split("[_-]");
                    int ogVerNum = Integer.parseInt(splitVersionNums[0].replaceAll("\\.", ""));
                    int minNodeSize = ogVerNum > 311 ? 2 : 3;
                    if (clusterDeploy && enterpriseInstallConfig.getIsInstallCM() && nodeSize < minNodeSize) {
                        throw new OpsException("In traditional master_slave cluster mode, at least "
                                + minNodeSize + " nodes needed to be installed");
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
                    log.error("Parsing the openGauss version number failed.");
                }
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

            liteInstallConfig.checkConfig();
        }
    }

    private void checkRetSessionConfig() {
        if (Objects.isNull(retSession)) {
            throw new OpsException("Response connection error");
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
        opsClusterEntity.setDatabaseUsername("gaussdb");
        if (!StrUtil.isEmpty(envPath)) {
            opsClusterEntity.setEnvPath(envPath);
        }
        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            opsClusterEntity.setInstallPackagePath(enterpriseInstallConfig.getInstallPackagePath());
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
            opsClusterEntity.setXmlConfigPath(enterpriseInstallConfig.getInstallPackagePath() + "/cluster_config.xml");
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            opsClusterEntity.setInstallPackagePath(liteInstallConfig.getInstallPackagePath());
            opsClusterEntity.setPort(liteInstallConfig.getPort());
            opsClusterEntity.setDatabasePassword(liteInstallConfig.getDatabasePassword());
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            opsClusterEntity.setInstallPackagePath(minimalistInstallConfig.getInstallPackagePath());
            opsClusterEntity.setPort(minimalistInstallConfig.getPort());
            opsClusterEntity.setDatabasePassword(minimalistInstallConfig.getDatabasePassword());
        }

        return opsClusterEntity;
    }

    /**
     * Get host info holder map
     *
     * @return map
     */
    public Map<String, HostInfoHolder> getHostInfoHolderMap() {
        if (ObjectUtils.isEmpty(hostInfoHolders)) {
            return new HashMap<>();
        }
        return hostInfoHolders
                .stream()
                .collect(Collectors.toMap(val -> val.getHostEntity().getHostId(), Function.identity()));
    }

    /**
     * Legal Cloning Methods
     *
     * @return InstallContext
     */
    @Override
    public InstallContext clone() {
        try {
            return (InstallContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new OpsException("Failed to clone InstallContext" + e);
        }
    }
}
