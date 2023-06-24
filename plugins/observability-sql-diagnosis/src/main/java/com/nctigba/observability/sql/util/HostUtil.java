/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import com.nctigba.observability.sql.service.ClusterManager;
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
public class HostUtil {
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
