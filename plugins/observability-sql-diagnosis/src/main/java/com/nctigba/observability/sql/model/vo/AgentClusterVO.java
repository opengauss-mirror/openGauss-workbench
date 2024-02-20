/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  LogClusterVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/model/vo/LogClusterVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * LogClusterVO
 *
 * @author luomeng
 * @since 2024/1/25
 */
@Data
public class AgentClusterVO {
    private String clusterId;
    private String clusterName;
    private DeployTypeEnum dbType;
    private List<AgentClusterNodeVO> clusterNodes;
}
