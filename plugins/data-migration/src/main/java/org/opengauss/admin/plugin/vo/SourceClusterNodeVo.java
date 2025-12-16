/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeVO;

/**
 * SourceClusterNodeVo
 *
 * @since 2025/11/11
 */
@Data
public class SourceClusterNodeVo {
    private String clusterNodeId;
    private String ip;
    private String port;

    /**
     * Convert JdbcDbClusterNodeVO to SourceClusterNodeVo
     *
     * @param clusterNodeVO JdbcDbClusterNodeVO
     * @return SourceClusterNodeVo
     */
    public static SourceClusterNodeVo of(JdbcDbClusterNodeVO clusterNodeVO) {
        SourceClusterNodeVo sourceClusterNodeVo = new SourceClusterNodeVo();
        sourceClusterNodeVo.setClusterNodeId(clusterNodeVO.getClusterNodeId());
        sourceClusterNodeVo.setIp(clusterNodeVO.getIp());
        sourceClusterNodeVo.setPort(clusterNodeVO.getPort());
        return sourceClusterNodeVo;
    }
}
