/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.vo;

import lombok.Data;

import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * openGauss cluster vo
 *
 * @since 2025/12/1
 */
@Data
public class OpengaussClusterVo {
    private String clusterId;
    private String clusterName;
    private String version;
    private String versionNum;
    private DeployTypeEnum deployType;
    private List<OpengaussClusterNodeVo> clusterNodes;
}
