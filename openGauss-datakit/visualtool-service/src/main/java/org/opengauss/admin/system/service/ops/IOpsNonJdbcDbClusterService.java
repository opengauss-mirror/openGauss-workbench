/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;

import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterInputDto;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

import java.util.List;

/**
 * IOpsNonJdbcDbClusterService
 *
 * @since 2025/11/6
 */
public interface IOpsNonJdbcDbClusterService extends IService<OpsNonJdbcDbClusterEntity> {
    /**
     * Add non-jdbc db cluster
     *
     * @param clusterInput cluster input
     */
    void add(JdbcDbClusterInputDto clusterInput);

    /**
     * Delete non-jdbc db cluster by cluster id
     *
     * @param clusterId cluster id
     */
    void delete(String clusterId);

    /**
     * Batch delete non-jdbc db cluster by cluster ids
     *
     * @param ids cluster ids
     */
    void batchDelete(List<Long> ids);

    /**
     * List non-jdbc db cluster by name
     *
     * @param name cluster name
     * @return non-jdbc db cluster info
     */
    List<OpsNonJdbcDbClusterEntity> listByName(String name);

    /**
     * List non-jdbc db cluster by name and type
     *
     * @param name cluster name
     * @param type cluster type
     * @return non-jdbc db cluster info
     */
    List<OpsNonJdbcDbClusterEntity> listByNameAndType(String name, String type);

    /**
     * List non-jdbc db cluster by type
     *
     * @param dbType cluster type
     * @return non-jdbc db cluster info
     */
    List<JdbcDbClusterVO> listByType(DbTypeEnum dbType);

    /**
     * Get non-jdbc db cluster info by name and ip
     *
     * @param name cluster name
     * @param ip cluster ip
     * @return non-jdbc db cluster info
     */
    List<JdbcDbClusterVO> getJdbcClusterVosByNameAndIp(String name, String ip);

    /**
     * Get non-jdbc db cluster info by name, ip and type
     *
     * @param name cluster name
     * @param ip cluster ip
     * @param type cluster type
     * @return non-jdbc db cluster info
     */
    List<JdbcDbClusterVO> getJdbcClusterVosByNameIpAndType(String name, String ip, String type);

    /**
     * Get non-jdbc db cluster version by cluster id and db type
     *
     * @param clusterId cluster id
     * @param dbType cluster type
     * @return non-jdbc db cluster version
     */
    String version(String clusterId, DbTypeEnum dbType);
}
