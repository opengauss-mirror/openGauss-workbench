/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.instance.model.dto.cluster;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;

import java.util.List;

/**
 * NodeRelationDTO
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class NodeRelationDTO {
    private String nodeId;
    private String hostIp;
    private String role;
    private String replayDelay;
    private String syncState;
    private List<NodeRelationDTO> children;

    /**
     * getDefaultRelationDto
     *
     * @param node OpsClusterNodeVO
     * @param standbyList List<NodeRelationDTO>
     * @return NodeRelationDTO
     */
    public static NodeRelationDTO getDefaultRelationDto(OpsClusterNodeVO node,
                                                        List<NodeRelationDTO> standbyList) {
        NodeRelationDTO primary = new NodeRelationDTO();
        init(primary, node);
        primary.setChildren(standbyList);
        return primary;
    }

    /**
     * init
     *
     * @param target NodeRelationDTO
     * @param node OpsClusterNodeVO
     */
    public static void init(NodeRelationDTO target, OpsClusterNodeVO node) {
        target.setNodeId(node.getNodeId());
        target.setHostIp(node.getPublicIp());
    }
}
