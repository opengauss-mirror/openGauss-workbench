/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package org.opengauss.visualtool.api.model.ops;

import lombok.Builder;
import lombok.Data;

/**
 * jdbc database cluster node
 *
 * @since 2024/10/30
 */
@Data
@Builder
public class JdbcDbClusterNode {
    private String clusterNodeId;
    private String url;
    private String username;
    private String password;
}
