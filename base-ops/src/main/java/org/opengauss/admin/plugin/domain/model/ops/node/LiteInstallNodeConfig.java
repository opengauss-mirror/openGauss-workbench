package org.opengauss.admin.plugin.domain.model.ops.node;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import lombok.Data;

import java.util.Objects;

/**
 * Lite install node configuration
 *
 * @author lhf
 * @date 2022/8/4 23:17
 **/
@Data
public class LiteInstallNodeConfig {

    private ClusterRoleEnum clusterRole;

    private String hostId;

    private String rootPassword;

    private String installUserId;

    private String installPath;

    private String dataPath;

    public OpsClusterNodeEntity toOpsClusterNodeEntity() {
        OpsClusterNodeEntity opsClusterNodeEntity = new OpsClusterNodeEntity();
        opsClusterNodeEntity.setClusterNodeId(StrUtil.uuid());
        opsClusterNodeEntity.setClusterRole(clusterRole);
        opsClusterNodeEntity.setHostId(hostId);
        opsClusterNodeEntity.setInstallUserId(installUserId);
        opsClusterNodeEntity.setInstallPath(installPath);
        opsClusterNodeEntity.setDataPath(dataPath);
        opsClusterNodeEntity.setPkgPath(installPath.substring(0, installPath.lastIndexOf("/")));
        return opsClusterNodeEntity;
    }

    public void checkConfig() {
        if (Objects.isNull(clusterRole)) {
            throw new OpsException("Node role error");
        }

        if (StrUtil.isEmpty(hostId)) {
            throw new OpsException("host information error");
        }

        if (StrUtil.isEmpty(rootPassword)){
            throw new OpsException("root password error");
        }

        if (StrUtil.isEmpty(installPath)) {
            throw new OpsException("installation directory error");
        }

        if (StrUtil.isEmpty(dataPath)) {
            throw new OpsException("data directory error");
        }
    }
}
