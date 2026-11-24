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

package org.opengauss.admin.common.core.dto.ops;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

import org.apache.commons.lang3.ObjectUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

/**
 * Ops jdbc db cluster import dto
 *
 * @since 2026/7/15
 */
@Data
@ExcelIgnoreUnannotated
public class OpsJdbcClusterImportDto {
    @ExcelProperty(value = "集群名称（Cluster Name）", index = 0)
    private String clusterName;
    @ExcelProperty(value = "数据库类型（Database Type, supported: openGauss, MySQL, PostgreSQL）", index = 1)
    private String databaseType;
    @ExcelProperty(value = "节点IP（Node IP）", index = 2)
    private String ip;
    @ExcelProperty(value = "端口（Port）", index = 3)
    private String port;
    @ExcelProperty(value = "用户名（Username）", index = 4)
    private String username;
    @ExcelProperty(value = "密码（Password）", index = 5)
    private String password;

    private DbTypeEnum dbType;
    private Integer portInt;
    private String errorMsg;

    @Override
    public String toString() {
        return "OpsJdbcClusterImportDto{"
                + "clusterName='" + clusterName + '\''
                + ", databaseType='" + databaseType + '\''
                + ", ip='" + ip + '\''
                + ", port='" + port + '\''
                + ", username='" + username + '\''
                + ", errorMsg='" + errorMsg + '\''
                + '}';
    }

    /**
     * Check if import row has error.
     *
     * @return boolean
     */
    public boolean hasError() {
        return ObjectUtils.isNotEmpty(errorMsg);
    }

    /**
     * Convert OpsJdbcClusterImportDto to OpsJdbcDbClusterNodeEntity.
     *
     * @return OpsJdbcDbClusterNodeEntity
     */
    public OpsJdbcDbClusterNodeEntity convertToOpsJdbcClusterNodeEntity() {
        OpsJdbcDbClusterNodeEntity clusterNodeEntity = new OpsJdbcDbClusterNodeEntity();
        clusterNodeEntity.setIp(ip);
        clusterNodeEntity.setPort(port);
        clusterNodeEntity.setUsername(username);
        clusterNodeEntity.setPassword(password);
        clusterNodeEntity.setUrl(generateJdbcUrl());
        return clusterNodeEntity;
    }

    /**
     * Generate jdbc url.
     *
     * @return jdbc url
     */
    public String generateJdbcUrl() {
        if (dbType == null) {
            return "Unknown Database Type";
        }
        return dbType.generateJdbcUrl(ip, portInt);
    }
}
