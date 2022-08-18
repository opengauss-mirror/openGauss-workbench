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

    private String installPath;

    private String logPath;

    private String tmpPath;

    private String omToolsPath;

    private String corePath;

    private Integer port;

    private Boolean enableDCF;

    private String databaseUsername;

    private String databasePassword;

    private List<EnterpriseInstallNodeConfig> nodeConfigList;
    /**
     * AZ ID
     */
    private String azId;
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

        if (StrUtil.isEmpty(azId) || StrUtil.isEmpty(azName)) {
            throw new OpsException("AZ information is incorrect");
        }

        if (Objects.isNull(enableDCF)) {
            enableDCF = false;
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
