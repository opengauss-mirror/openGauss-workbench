/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

import java.util.List;

/**
 * NodeRelationDto
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class NodeRelationDto {
    private String hostIp;
    private String role;
    private String state;
    private String replayDelay;
    private String syncState;
    private List<NodeRelationDto> children;

    /**
     * getDefaultRelationDto
     *
     * @param stateCache ClusterHealthState
     * @param node OpsClusterNodeVO
     * @param standbyList List<NodeRelationDto>
     * @return NodeRelationDto
     */
    public static NodeRelationDto getDefaultRelationDto(ClusterHealthState stateCache, OpsClusterNodeVO node,
                                                        List<NodeRelationDto> standbyList) {
        NodeRelationDto primary = new NodeRelationDto();
        primary.setHostIp(node.getPublicIp());
        setNodeState(primary, stateCache);
        primary.setChildren(standbyList);
        return primary;
    }

    /**
     * setNodeState
     *
     * @param node NodeRelationDto
     * @param stateCache ClusterHealthState
     */
    public static void setNodeState(NodeRelationDto node, ClusterHealthState stateCache) {
        node.setRole(stateCache.getNodeRole().getOrDefault(node.getHostIp(), "Unknown"));
        node.setState(stateCache.getNodeState().getOrDefault(node.getHostIp(), "Unknown"));
    }
}
