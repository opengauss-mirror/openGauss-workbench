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
