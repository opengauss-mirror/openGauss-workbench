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
 *  ClustersDTO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/model/dto/cluster/ClustersDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.model.dto.cluster;

import java.util.List;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import com.nctigba.observability.instance.util.MessageSourceUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClustersDTO
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class ClustersDTO {
    private String clusterId;
    private String versionNum;
    private String version;
    private String envPath;
    private Integer nodeCount;
    private String arch;

    /**
     * get ClustersDTO
     *
     * @param opsClusterVO OpsClusterVO
     * @return ClustersDTO
     */
    public static ClustersDTO of(OpsClusterVO opsClusterVO) {
        ClustersDTO clustersDto = new ClustersDTO();
        clustersDto.setClusterId(opsClusterVO.getClusterId());
        clustersDto.setVersionNum(opsClusterVO.getVersionNum());
        clustersDto.setEnvPath(opsClusterVO.getEnvPath());
        clustersDto.setVersion(opsClusterVO.getVersion());
        clustersDto.setNodeCount(opsClusterVO.getClusterNodes().size());
        List<OpsClusterNodeVO> nodes = opsClusterVO.getClusterNodes();
        Long primaryNum = nodes.stream().filter(node -> "MASTER".equalsIgnoreCase(node.getClusterRole())).count();
        Long standbyNum = nodes.stream().filter(node -> !"MASTER".equalsIgnoreCase(node.getClusterRole())).count();
        clustersDto.setArch(MessageSourceUtils.getMsg("cluster.arch", primaryNum.toString(), standbyNum.toString()));
        return clustersDto;
    }
}
