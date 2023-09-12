/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nctigba.observability.instance.util.MessageSourceUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

import java.util.List;
import java.util.Optional;

/**
 * SyncSituationDto
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class SyncSituationDto {
    private String clusterId;
    private String nodeId;
    private String hostIp;
    private String nodeName;
    private String primaryAddr;
    private String localAddr;
    private String role;
    private State nodeState;
    private String sync;
    private State syncState;
    private String syncPriority;
    private String receivedDelay;
    private String writeDelay;
    private String replayDelay;
    @JsonIgnore
    private String walSyncState;

    /**
     * getDefaultSituationDto
     *
     * @param syncSituation SyncSituation
     * @param clusterNodes List<OpsClusterNodeVO>
     * @param clusterId String
     * @return SyncSituationDto
     */
    public static SyncSituationDto getDefaultSituationDto(SyncSituation syncSituation,
                                                          List<OpsClusterNodeVO> clusterNodes, String clusterId) {
        SyncSituationDto dto = new SyncSituationDto();
        dto.setClusterId(clusterId);
        Optional<OpsClusterNodeVO> nodeVO = clusterNodes.stream().filter(
                n -> n.getPublicIp().equals(syncSituation.getHostIp())).findFirst();
        nodeVO.ifPresent(node -> dto.setNodeId(node.getNodeId()));
        dto.setHostIp(syncSituation.getHostIp());
        dto.setSync(syncSituation.getSync());
        dto.setSyncState(syncSituation.getWalSyncState());
        dto.setSyncPriority(syncSituation.getSyncPriority());
        dto.setReceivedDelay(syncSituation.getReceivedDelay());
        dto.setWriteDelay(syncSituation.getWriteDelay());
        dto.setReplayDelay(syncSituation.getReplayDelay());
        return dto;
    }

    public void setNodeState(String state) {
        this.nodeState = new State("cluster.node.state." + state);
    }

    public String getRole() {
        return MessageSourceUtil.getMsg("cluster.node.role." + role);
    }

    public String getSync() {
        return MessageSourceUtil.getMsg("cluster.node.sync." + sync);
    }

    public void setSyncState(String state) {
        this.syncState = new State("cluster.node.syncState." + state);
    }
}
