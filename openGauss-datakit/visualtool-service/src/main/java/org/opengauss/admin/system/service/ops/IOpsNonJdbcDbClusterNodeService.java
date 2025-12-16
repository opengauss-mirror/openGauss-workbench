/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

import java.util.List;

/**
 * IOpsNonJdbcDbClusterNodeService
 *
 * @since 2025/11/6
 */
public interface IOpsNonJdbcDbClusterNodeService extends IService<OpsNonJdbcDbClusterNodeEntity> {
    /**
     * Ping the non-JDBC database cluster node
     *
     * @param clusterNodeInput The non-JDBC database cluster node input
     * @return true if the node is reachable, false otherwise
     */
    boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput);

    /**
     * Delete the non-JDBC database cluster nodes by cluster ID
     *
     * @param clusterId The non-JDBC database cluster ID
     */
    void deleteByClusterId(String clusterId);

    /**
     * Delete the non-JDBC database cluster nodes by cluster IDs
     *
     * @param ids The non-JDBC database cluster node IDs
     */
    void deleteByClusterIds(List<Long> ids);

    /**
     * List the non-JDBC database cluster nodes by cluster IDs
     *
     * @param clusterIds The non-JDBC database cluster IDs
     * @return The non-JDBC database cluster nodes
     */
    List<OpsNonJdbcDbClusterNodeEntity> listByClusterIds(List<String> clusterIds);

    /**
     * List the non-JDBC database cluster nodes by IP and cluster IDs
     *
     * @param ip         The non-JDBC database cluster node IP
     * @param clusterIds The non-JDBC database cluster IDs
     * @return The non-JDBC database cluster nodes
     */
    List<OpsNonJdbcDbClusterNodeEntity> listByIpAndClusterIds(String ip, List<String> clusterIds);

    /**
     * Get the non-JDBC database cluster node by cluster ID
     *
     * @param clusterId The non-JDBC database cluster ID
     * @return The non-JDBC database cluster node
     */
    OpsNonJdbcDbClusterNodeEntity getOneByClusterId(String clusterId);

    /**
     * Monitor the non-JDBC database cluster node
     *
     * @param dbType       The non-JDBC database type
     * @param clusterNodeId The non-JDBC database cluster node ID
     * @param businessId   The business ID
     */
    void monitor(DbTypeEnum dbType, String clusterNodeId, String businessId);
}
