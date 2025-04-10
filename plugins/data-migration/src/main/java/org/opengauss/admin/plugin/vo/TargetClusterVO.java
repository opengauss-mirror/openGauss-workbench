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
 * TargetClusterVO.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/vo/TargetClusterVO.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.vo;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * @className: TargetClusterVO
 * @author: xielibo
 * @date: 2023-03-23 12:24
 **/
@Data
public class TargetClusterVO {
    private String clusterId;
    private String clusterName;
    private String version;
    private String versionNum;
    private DeployTypeEnum deployType;
    private List<TargetClusterNodeVO> clusterNodes;
}
