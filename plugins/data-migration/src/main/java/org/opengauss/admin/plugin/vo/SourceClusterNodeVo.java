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
