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
 *  HostUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/HostUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import com.nctigba.observability.sql.service.impl.ClusterManager;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * HostUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Component
public class HostUtils {
    @Autowired
    private ClusterManager clusterManager;

    public String getHostId(String nodeId) {
        List<OpsClusterVO> clusterVOList = clusterManager.getAllOpsCluster();
        for (OpsClusterVO clusterVO : clusterVOList) {
            List<OpsClusterNodeVO> clusterNodes = clusterVO.getClusterNodes();
            for (OpsClusterNodeVO nodeVO : clusterNodes) {
                if (nodeVO.getNodeId().equals(nodeId)) {
                    return nodeVO.getHostId();
                }
            }
        }
        return "";
    }
}
