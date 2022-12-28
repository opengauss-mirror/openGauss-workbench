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
