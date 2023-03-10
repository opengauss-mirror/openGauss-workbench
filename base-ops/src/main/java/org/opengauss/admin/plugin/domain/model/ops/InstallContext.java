package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.InstallModeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/8/4 22:56
 **/
@Data
public class InstallContext implements Cloneable {

    private OpenGaussVersionEnum openGaussVersion;

    private String openGaussVersionNum;

    private OpenGaussSupportOSEnum os;

    private InstallModeEnum installMode;

    private DeployTypeEnum deployType;

    private String installPackagePath;

    private EnterpriseInstallConfig enterpriseInstallConfig;

    private MinimalistInstallConfig minimalistInstallConfig;

    private LiteInstallConfig liteInstallConfig;

    private String clusterId;

    private WsSession retSession;

    private List<HostInfoHolder> hostInfoHolders;

    public void checkConfig() {
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

        if (Objects.isNull(retSession)) {
            throw new OpsException("Response connection error");
        }

        boolean clusterDeploy = deployType == DeployTypeEnum.CLUSTER;

        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            if (Objects.isNull(enterpriseInstallConfig)) {
                throw new OpsException("The enterprise version is incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(enterpriseInstallConfig.getNodeConfigList()) ? 0 : enterpriseInstallConfig.getNodeConfigList().size();
            if (clusterDeploy && enterpriseInstallConfig.getIsInstallCM() && nodeSize < 3) {
                throw new OpsException("In cluster mode, a maximum of three nodes can be installed");
            }

            enterpriseInstallConfig.checkConfig();
        } else if (openGaussVersion == OpenGaussVersionEnum.MINIMAL_LIST) {
            if (Objects.isNull(minimalistInstallConfig)) {
                throw new OpsException("The minimalist version was incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(minimalistInstallConfig.getNodeConfigList()) ? 0 : minimalistInstallConfig.getNodeConfigList().size();
            if (nodeSize > 1) {
                throw new OpsException("The minimalist version can only be installed on a single host");
            }

            minimalistInstallConfig.checkConfig();
        } else if (openGaussVersion == OpenGaussVersionEnum.LITE) {
            if (Objects.isNull(liteInstallConfig)) {
                throw new OpsException("The lightweight version was incorrectly installed and configured");
            }

            int nodeSize = CollUtil.isEmpty(liteInstallConfig.getNodeConfigList()) ? 0 : liteInstallConfig.getNodeConfigList().size();
            if (clusterDeploy && nodeSize < 2) {
                throw new OpsException("In cluster mode, a maximum of two nodes can be installed");
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
        opsClusterEntity.setDatabaseUsername("gaussdb");
        if (openGaussVersion == OpenGaussVersionEnum.ENTERPRISE) {
            opsClusterEntity.setInstallPackagePath(enterpriseInstallConfig.getInstallPackagePath());
            opsClusterEntity.setDatabasePassword(enterpriseInstallConfig.getDatabasePassword());
            opsClusterEntity.setInstallPath(enterpriseInstallConfig.getInstallPath());
            opsClusterEntity.setLogPath(enterpriseInstallConfig.getLogPath());
            opsClusterEntity.setTmpPath(enterpriseInstallConfig.getTmpPath());
            opsClusterEntity.setOmToolsPath(enterpriseInstallConfig.getOmToolsPath());
            opsClusterEntity.setCorePath(enterpriseInstallConfig.getCorePath());
            opsClusterEntity.setPort(enterpriseInstallConfig.getPort());
            opsClusterEntity.setEnableDcf(enterpriseInstallConfig.getEnableDCF());
            opsClusterEntity.setXmlConfigPath(enterpriseInstallConfig.getInstallPackagePath()+"/cluster_config.xml");
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
}
