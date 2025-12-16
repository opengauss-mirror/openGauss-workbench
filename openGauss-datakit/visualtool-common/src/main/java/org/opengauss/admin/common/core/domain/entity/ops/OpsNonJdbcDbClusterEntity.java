/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.common.core.domain.entity.ops;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opengauss.admin.common.core.domain.BaseEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

/**
 * OpsNonJdbcDbClusterEntity
 *
 * @since 2025/11/6
 */
@Data
@TableName("ops_non_jdbc_db_cluster")
@EqualsAndHashCode(callSuper = true)
public class OpsNonJdbcDbClusterEntity extends BaseEntity {
    @TableId
    private String clusterId;
    private String name;
    private DbTypeEnum dbType;
    private DeployTypeEnum deployType;
    private String versionNum;
}
