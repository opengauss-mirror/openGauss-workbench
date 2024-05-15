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
 *  ClusterHealthStateDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/cluster/ClusterHealthStateDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

import lombok.Data;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.HashMap;
import java.util.Map;

/**
 * ClusterHealthState
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
public class ClusterHealthStateDTO {
    private String clusterState;
    private Map<String, String> nodeState;
    private Map<String, String> nodeRole;
    private Map<String, String> nodeName;
    private Map<String, String> cmState;

    /**
     * init ClusterHealthStateDTO
     * @param cluster OpsClusterVO
     * @return ClusterHealthStateDTO
     */
    public static ClusterHealthStateDTO init(OpsClusterVO cluster) {
        ClusterHealthStateDTO dto = new ClusterHealthStateDTO();
        dto.setClusterState("Unknown");
        Map<String, String> nodeState = new HashMap<>();
        Map<String, String> nodeRole = new HashMap<>();
        Map<String, String> nodeName = new HashMap<>();
        Map<String, String> cmState = new HashMap<>();
        dto.setNodeState(nodeState);
        dto.setNodeRole(nodeRole);
        dto.setNodeName(nodeName);
        dto.setCmState(cmState);
        for (OpsClusterNodeVO clusterNode : cluster.getClusterNodes()) {
            nodeState.put(clusterNode.getNodeId(), "Unknown");
            nodeRole.put(clusterNode.getNodeId(), "Unknown");
            nodeName.put(clusterNode.getNodeId(), "Unknown");
            cmState.put(clusterNode.getNodeId(), "Unknown");
        }
        return dto;
    }
}
