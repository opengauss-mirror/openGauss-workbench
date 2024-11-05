/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model.ops;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * jdbc database cluster
 *
 * @since 2024/10/30
 */
@Data
@Builder
public class JdbcDbCluster {
    private String clusterId;
    private String clusterName;
    private String deployType;
    private List<JdbcDbClusterNode> nodes;
}
