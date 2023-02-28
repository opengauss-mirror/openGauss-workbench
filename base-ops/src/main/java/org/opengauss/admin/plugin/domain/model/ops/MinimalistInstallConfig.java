package org.opengauss.admin.plugin.domain.model.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.node.MinimalistInstallNodeConfig;
import lombok.Data;

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

    public List<OpsClusterNodeEntity> toOpsClusterNodeEntityList() {
        ArrayList<OpsClusterNodeEntity> opsClusterNodeEntities = new ArrayList<>();

        for (MinimalistInstallNodeConfig nodeConfig : nodeConfigList) {
            opsClusterNodeEntities.add(nodeConfig.toOpsClusterNodeEntity());
        }

        return opsClusterNodeEntities;
    }
}
