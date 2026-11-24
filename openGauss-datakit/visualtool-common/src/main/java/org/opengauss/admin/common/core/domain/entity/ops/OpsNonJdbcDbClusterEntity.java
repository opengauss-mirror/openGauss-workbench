/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
