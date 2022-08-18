package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.common.enums.ops.InstallModeEnum;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/12 09:01
 **/
@Data
@TableName("ops_cluster")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterEntity extends BaseEntity {
    @TableId
    private String clusterId;
    private OpenGaussVersionEnum version;
    private String versionNum;
    private InstallModeEnum installMode;
    private DeployTypeEnum deployType;
    private String clusterName;
    private String installPackagePath;
    private String databaseUsername;
    private String databasePassword;

    private String installPath;
    private String logPath;
    private String tmpPath;
    private String omToolsPath;
    private String corePath;
    private Integer port;
    private Boolean enableDcf;
}
