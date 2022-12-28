package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.enums.ops.UpgradeTypeEnum;

/**
 * @author lhf
 * @date 2022/12/9 11:09
 **/
@Data
public class UpgradeContext {
    private String hostPublicIp;
    private String rootPassword;
    private Integer hostPort;
    private WsSession retSession;
    private String clusterConfigXmlPath;
    private UpgradeTypeEnum upgradeType;
    private String upgradePackagePath;
    private String sepEnvFile;
    private String installUsername;
    private String installUserPassword;
    private OpsClusterEntity clusterEntity;
    private OpsClusterNodeEntity opsClusterNodeEntity;
}
