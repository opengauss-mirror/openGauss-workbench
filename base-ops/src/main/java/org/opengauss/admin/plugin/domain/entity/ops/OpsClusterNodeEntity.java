package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lhf
 * @date 2022/8/18 09:12
 **/
@Data
@TableName("ops_cluster_node")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterNodeEntity extends BaseEntity {
    @TableId
    private String clusterNodeId;
    private ClusterRoleEnum clusterRole;
    private String hostId;
    @TableField(exist = false)
    private String rootPassword;
    private String installUserId;
    private String installPath;
    private String dataPath;
    private String pkgPath;
    private Boolean installDemoDatabase;
    private String clusterId;
    /**
     * Whether to install CM
     */
    @TableField("is_install_cm")
    private Boolean isInstallCM;
    /**
     * Whether it is the CM master node
     */
    @TableField("is_cm_master")
    private Boolean isCMMaster;
    /**
     * CM data path
     */
    private String cmDataPath;
    /**
     * CM port
     */
    private Integer cmPort;
    /**
     * data node xlog path
     */
    private String xlogPath;
}
