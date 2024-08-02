/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterTaskEntity.java
 *
 * IDENTIFICATION
 *  plugins/base-ops/src/main/java/org/opengauss/admin/plugin/domain/entity/ops/OpsClusterTaskEntity.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.ClusterEnvCheckResultEnum;
import org.opengauss.admin.plugin.domain.model.ops.OpsClusterTaskNodeVO;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.springframework.beans.BeanUtils;


/**
 * OpsClusterTaskEntity
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Data
@TableName("ops_cluster_task_node")
@EqualsAndHashCode(callSuper = true)
public class OpsClusterTaskNodeEntity extends BaseEntity {
    @TableId
    @TableField(fill = FieldFill.INSERT)
    private String clusterNodeId;
    private String clusterId;
    private String hostId;
    private String hostUserId;
    private ClusterRoleEnum nodeType;
    private String dataPath;
    private String azOwner;
    private String azPriority;
    private ClusterEnvCheckResultEnum envCheckResult;
    private String envCheckDetail;
    private Boolean isCmMaster;
    private String cmDataPath;
    private Integer cmPort;
    public OpsClusterTaskNodeVO toVo() {
        OpsClusterTaskNodeVO opsClusterNodeVO = new OpsClusterTaskNodeVO();
        BeanUtils.copyProperties(this, opsClusterNodeVO);
        return opsClusterNodeVO;
    }
}
