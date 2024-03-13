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
 *  ClusterStateDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/main/java/com/nctigba/observability/instance/model/dto/cluster/ClusterStateDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

import com.nctigba.observability.instance.util.MessageSourceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import java.util.Map;
import java.util.Optional;

/**
 * ClusterStateDTO
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterStateDTO {
    private String clusterId;
    private StateDTO clusterState;
    private String desc;
    private String primaryNodeId;

    /**
     * get ClusterStateDTO
     *
     * @param clusterVO OpsClusterVO
     * @param stateCache ClusterHealthState
     * @return ClusterStateDTO
     */
    public static ClusterStateDTO of(OpsClusterVO clusterVO, ClusterHealthStateDTO stateCache) {
        ClusterStateDTO dto = new ClusterStateDTO();
        dto.setClusterId(clusterVO.getClusterId());
        String state = Optional.ofNullable(stateCache.getClusterState()).orElse(
                "Unknown");
        dto.setClusterState(state);
        dto.setDesc(state);
        Optional<Map.Entry<String, String>> primary = stateCache.getNodeRole().entrySet().stream().filter(
                en -> en.getValue().equals("Primary")).findFirst();
        primary.ifPresent(stringStringEntry -> dto.setPrimaryNodeId(stringStringEntry.getKey()));
        return dto;
    }

    public void setClusterState(String clusterState) {
        this.clusterState = new StateDTO("cluster.state.value." + clusterState);
    }

    public void setDesc(String state) {
        this.desc = MessageSourceUtils.getMsg("cluster.state.desc." + state);
    }
}
