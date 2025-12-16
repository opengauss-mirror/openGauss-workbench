/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opengauss.admin.common.core.domain.BaseEntity;

/**
 * OpsNonJdbcDbClusterNodeEntity
 *
 * @since 2025/11/6
 */
@Data
@TableName("ops_non_jdbc_db_cluster_node")
@EqualsAndHashCode(callSuper = true)
public class OpsNonJdbcDbClusterNodeEntity extends BaseEntity {
    @TableId
    private String clusterNodeId;
    private String clusterId;
    private String ip;
    private String port;
    private String username;
    private String password;
    private String url;
}
