package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;
import org.opengauss.admin.plugin.enums.ops.UpgradeTypeEnum;

/**
 * @author lhf
 * @date 2022/12/9 10:19
 **/
@Data
public class UpgradeBody {
    private String clusterId;
    private UpgradeTypeEnum upgradeType;
    private String upgradePackagePath;
    private String clusterConfigXmlPath;
    private String sepEnvFile;
    private String businessId;
    private String hostId;
    private String hostRootPassword;
}
