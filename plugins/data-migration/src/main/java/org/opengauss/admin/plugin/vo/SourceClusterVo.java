/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterVO;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * SourceClusterVo
 *
 * @since 2025/11/11
 */
@Data
public class SourceClusterVo {
    private String clusterId;
    private String name;
    private String versionNum;
    private DbTypeEnum dbType;
    private DeployTypeEnum deployType;
    private List<SourceClusterNodeVo> nodes;

    /**
     * Convert JdbcDbClusterVO to SourceClusterVo
     *
     * @param jdbcDbClusterVO JdbcDbClusterVO
     * @return SourceClusterVo
     */
    public static SourceClusterVo of(JdbcDbClusterVO jdbcDbClusterVO) {
        SourceClusterVo sourceClusterVo = new SourceClusterVo();
        sourceClusterVo.setClusterId(jdbcDbClusterVO.getClusterId());
        sourceClusterVo.setName(jdbcDbClusterVO.getName());
        sourceClusterVo.setVersionNum(jdbcDbClusterVO.getVersionNum());
        sourceClusterVo.setDbType(jdbcDbClusterVO.getDbType());
        sourceClusterVo.setDeployType(jdbcDbClusterVO.getDeployType());
        sourceClusterVo.setNodes(jdbcDbClusterVO.getNodes().stream().map(SourceClusterNodeVo::of).toList());
        return sourceClusterVo;
    }
}
