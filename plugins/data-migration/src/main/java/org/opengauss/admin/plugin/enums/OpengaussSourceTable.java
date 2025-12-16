/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * OpengaussSourceTable
 *
 * @since 2025/11/27
 */
@Getter
public enum OpengaussSourceTable {
    OPS_CLUSTER("ops_cluster"),
    JDBC_CLUSTER("ops_jdbcdb_cluster");

    private final String tableName;

    OpengaussSourceTable(String tableName) {
        this.tableName = tableName;
    }
}
