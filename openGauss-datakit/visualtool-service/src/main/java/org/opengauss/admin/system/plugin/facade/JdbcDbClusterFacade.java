/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * JdbcDbClusterFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/JdbcDbClusterFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsNonJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsJdbcDbClusterService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterNodeService;
import org.opengauss.admin.system.service.ops.IOpsNonJdbcDbClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/14 22:22
 **/
@Service
public class JdbcDbClusterFacade {
    @Autowired
    private IOpsJdbcDbClusterService opsJdbcDbClusterService;

    @Autowired
    private IOpsJdbcDbClusterNodeService opsJdbcDbClusterNodeService;

    @Autowired
    private IOpsNonJdbcDbClusterService opsNonJdbcDbClusterService;

    @Autowired
    private IOpsNonJdbcDbClusterNodeService opsNonJdbcDbClusterNodeService;

    public List<JdbcDbClusterVO> listAll(String type) {
        return opsJdbcDbClusterService.listByType(type);
    }

    /**
     * List jdbc clusters by database type
     *
     * @param dbType database type
     * @return jdbc clusters
     */
    public List<JdbcDbClusterVO> listJdbcClusters(DbTypeEnum dbType) {
        return opsJdbcDbClusterService.listByType(dbType);
    }

    /**
     * Get jdbc db cluster vo by cluster id
     *
     * @param clusterId cluster id
     * @return jdbc db cluster vo
     */
    public JdbcDbClusterVO getJdbcDbClusterVoByClusterId(String clusterId) {
        return opsJdbcDbClusterService.getJdbcClusterVoByClusterId(clusterId);
    }

    /**
     * List non-jdbc clusters by database type
     *
     * @param dbType database type
     * @return non-jdbc clusters
     */
    public List<JdbcDbClusterVO> listNonJdbcClusters(DbTypeEnum dbType) {
        return opsNonJdbcDbClusterService.listByType(dbType);
    }

    /**
     * Get jdbc db cluster node by node id
     *
     * @param nodeId node id
     * @return jdbc db cluster node
     */
    public OpsJdbcDbClusterNodeEntity getJdbcDbClusterNode(String nodeId) {
        return opsJdbcDbClusterNodeService.getById(nodeId);
    }

    /**
     * Get non-jdbc db cluster node by node id
     *
     * @param nodeId node id
     * @return non-jdbc db cluster node
     */
    public OpsNonJdbcDbClusterNodeEntity getNonJdbcDbClusterNode(String nodeId) {
        return opsNonJdbcDbClusterNodeService.getById(nodeId);
    }
}
