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

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.OpengaussSourceTable;

import java.util.List;

/**
 * TargetClusterVo
 *
 * @since 2025/12/1
 */
@Data
public class TargetClusterVo {
    private String clusterId;
    private String name;
    private String versionNum;
    private DeployTypeEnum deployType;
    private OpengaussSourceTable sourceTable;
    private List<TargetClusterNodeVo> nodes;
    private boolean isUserMaster;

    /**
     * Convert JdbcDbClusterVO to TargetClusterVo
     *
     * @param jdbcDbClusterVO JdbcDbClusterVO
     * @return TargetClusterVo
     */
    public static TargetClusterVo of(JdbcDbClusterVO jdbcDbClusterVO) {
        TargetClusterVo targetClusterVo = new TargetClusterVo();
        targetClusterVo.setClusterId(jdbcDbClusterVO.getClusterId());
        targetClusterVo.setName(jdbcDbClusterVO.getName());
        targetClusterVo.setVersionNum(jdbcDbClusterVO.getVersionNum());
        targetClusterVo.setDeployType(jdbcDbClusterVO.getDeployType());
        targetClusterVo.setSourceTable(OpengaussSourceTable.JDBC_CLUSTER);
        targetClusterVo.setNodes(jdbcDbClusterVO.getNodes().stream().map(TargetClusterNodeVo::of).toList());
        return targetClusterVo;
    }

    /**
     * Convert OpsClusterVO to TargetClusterVo
     *
     * @param opsClusterVO OpsClusterVO
     * @return TargetClusterVo
     */
    public static TargetClusterVo of(OpsClusterVO opsClusterVO) {
        TargetClusterVo targetClusterVo = new TargetClusterVo();
        targetClusterVo.setClusterId(opsClusterVO.getClusterId());
        targetClusterVo.setName(opsClusterVO.getClusterName());
        targetClusterVo.setVersionNum(opsClusterVO.getVersionNum());
        targetClusterVo.setDeployType(opsClusterVO.getDeployType());
        targetClusterVo.setSourceTable(OpengaussSourceTable.OPS_CLUSTER);
        targetClusterVo.setNodes(opsClusterVO.getClusterNodes().stream().map(TargetClusterNodeVo::of).toList());
        return targetClusterVo;
    }
}
