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
 * OpsClusterNodeEntity.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/domain/entity/ops/OpsClusterNodeEntity.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.common.core.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;
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

    private String dssDataLunLinkPath;

    private String xlogLunLinkPath;

    private String cmVotingLunLinkPath;

    private String cmSharingLunLinkPath;
}
