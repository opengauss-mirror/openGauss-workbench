/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;

/**
 * TargetClusterNodeVo
 *
 * @since 2025/12/1
 */
@Data
public class TargetClusterNodeVo {
    private String clusterNodeId;
    private String ip;
    private String port;
    private boolean isPrimary;

    /**
     * Convert JdbcDbClusterNodeVO to TargetClusterNodeVo
     *
     * @param clusterNodeVo JdbcDbClusterNodeVO
     * @return TargetClusterNodeVo
     */
    public static TargetClusterNodeVo of(JdbcDbClusterNodeVO clusterNodeVo) {
        TargetClusterNodeVo targetClusterNodeVo = new TargetClusterNodeVo();
        targetClusterNodeVo.setClusterNodeId(clusterNodeVo.getClusterNodeId());
        targetClusterNodeVo.setIp(clusterNodeVo.getIp());
        targetClusterNodeVo.setPort(clusterNodeVo.getPort());
        return targetClusterNodeVo;
    }

    /**
     * Convert OpsClusterNodeVO to TargetClusterNodeVo
     *
     * @param clusterNodeVo OpsClusterNodeVO
     * @return TargetClusterNodeVo
     */
    public static TargetClusterNodeVo of(OpsClusterNodeVO clusterNodeVo) {
        TargetClusterNodeVo targetClusterNodeVo = new TargetClusterNodeVo();
        targetClusterNodeVo.setClusterNodeId(clusterNodeVo.getNodeId());
        targetClusterNodeVo.setIp(clusterNodeVo.getPublicIp());
        targetClusterNodeVo.setPort(clusterNodeVo.getDbPort().toString());
        return targetClusterNodeVo;
    }
}
