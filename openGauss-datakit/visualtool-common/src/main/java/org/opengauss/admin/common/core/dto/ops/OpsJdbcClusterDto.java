/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package org.opengauss.admin.common.core.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;

import java.util.List;

/**
 * JDBC cluster DTO
 *
 * @since 2026/7/15
 */
@Data
@AllArgsConstructor
public class OpsJdbcClusterDto {
    private OpsJdbcDbClusterEntity clusterEntity;
    private List<OpsJdbcDbClusterNodeEntity> clusterNodeEntityList;
}
