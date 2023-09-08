/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.dto.cluster;

import java.util.List;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import com.nctigba.observability.instance.util.MessageSourceUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClustersDto
 *
 * @author liupengfei
 * @since 2023/8/25
 */
@Data
@NoArgsConstructor
public class ClustersDto {
    private String clusterId;
    private String versionNum;
    private String version;
    private String envPath;
    private Integer nodeCount;
    private String arch;

    /**
     * get ClustersDto
     *
     * @param opsClusterVO OpsClusterVO
     * @return ClustersDto
     */
    public static ClustersDto of(OpsClusterVO opsClusterVO) {
        ClustersDto clustersDto = new ClustersDto();
        clustersDto.setClusterId(opsClusterVO.getClusterId());
        clustersDto.setVersionNum(opsClusterVO.getVersionNum());
        clustersDto.setEnvPath(opsClusterVO.getEnvPath());
        clustersDto.setVersion(opsClusterVO.getVersion());
        clustersDto.setNodeCount(opsClusterVO.getClusterNodes().size());
        List<OpsClusterNodeVO> nodes = opsClusterVO.getClusterNodes();
        Long primaryNum = nodes.stream().filter(node -> "MASTER".equalsIgnoreCase(node.getClusterRole())).count();
        Long standbyNum = nodes.stream().filter(node -> !"MASTER".equalsIgnoreCase(node.getClusterRole())).count();
        clustersDto.setArch(MessageSourceUtil.getMsg("cluster.arch", primaryNum.toString(), standbyNum.toString()));
        return clustersDto;
    }
}
