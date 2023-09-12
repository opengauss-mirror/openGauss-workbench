/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import com.nctigba.observability.instance.util.MessageSourceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.Map;
import java.util.Optional;

/**
 * ClusterStateDto
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterStateDto {
    private String clusterId;
    private State clusterState;
    private String desc;
    private String primaryNodeId;

    /**
     * get ClusterStateDto
     *
     * @param clusterVO OpsClusterVO
     * @param stateCache ClusterHealthState
     * @return ClusterStateDto
     */
    public static ClusterStateDto of(OpsClusterVO clusterVO, ClusterHealthState stateCache) {
        ClusterStateDto dto = new ClusterStateDto();
        dto.setClusterId(clusterVO.getClusterId());
        String state = Optional.ofNullable(stateCache.getClusterState()).orElse(
                "Unknown");
        dto.setClusterState(state);
        dto.setDesc(state);
        Optional<Map.Entry<String, String>> primary = stateCache.getNodeRole().entrySet().stream().filter(
                en -> en.getValue().equals("Primary")).findFirst();
        if (primary.isPresent()) {
            String primaryIp = primary.get().getKey();
            OpsClusterNodeVO nodeVO = clusterVO.getClusterNodes().stream().filter(
                    node -> node.getPublicIp().equals(primaryIp)).findFirst().get();
            dto.setPrimaryNodeId(nodeVO.getNodeId());
        }
        return dto;
    }

    public void setClusterState(String clusterState) {
        this.clusterState = new State("cluster.state.value." + clusterState);
    }

    public void setDesc(String state) {
        this.desc = MessageSourceUtil.getMsg("cluster.state.desc." + state);
    }
}
