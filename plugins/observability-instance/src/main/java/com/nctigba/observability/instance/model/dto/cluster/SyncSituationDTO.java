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
 *  SyncSituationDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/cluster/SyncSituationDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

import java.util.List;
import java.util.Optional;

/**
 * SyncSituationDTO
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class SyncSituationDTO {
    private String clusterId;
    private String nodeId;
    private String hostIp;
    private String nodeName;
    private String primaryAddr;
    private String localAddr;
    private String role;
    private StateDTO nodeState;
    private String sync;
    private StateDTO syncState;
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
     * @return SyncSituationDTO
     */
    public static SyncSituationDTO getDefaultSituationDto(
        SyncSituationDelayDTO syncSituation,
        List<OpsClusterNodeVO> clusterNodes, String clusterId) {
        SyncSituationDTO dto = new SyncSituationDTO();
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
        this.nodeState = new StateDTO("cluster.node.state." + state);
    }

    public void setNodeState(StateDTO state) {
        this.nodeState = state;
    }

    public String getRole() {
        return MessageSourceUtils.getMsg("cluster.node.role." + role);
    }

    public String getSync() {
        return MessageSourceUtils.getMsg("cluster.node.sync." + sync);
    }

    public void setSyncState(String state) {
        this.syncState = new StateDTO("cluster.node.syncState." + state);
    }

    public void setSyncState(StateDTO state) {
        this.syncState = state;
    }
}
