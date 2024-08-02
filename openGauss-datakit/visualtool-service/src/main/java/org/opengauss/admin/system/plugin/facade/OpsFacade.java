/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/OpsFacade.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhf
 * @date 2022/10/10 13:35
 **/
@Service
public class OpsFacade {

    @Autowired
    private IOpsClusterService opsClusterService;

    public List<OpsClusterVO> listCluster() {
        return opsClusterService.listCluster();
    }

    public CheckSummaryVO check(String clusterId, String rootPassword){
        return opsClusterService.check(clusterId, rootPassword);
    }

    /**
     * determine whether the node is in ops cluster
     *
     * @param nodeId node id
     * @return boolean
     */
    public boolean isNodeInOpsCluster(String nodeId) {
        return opsClusterService.listCluster().stream()
                .map(OpsClusterVO::getClusterNodes)
                .flatMap(List::stream)
                .anyMatch(node -> nodeId.equals(node.getNodeId()));
    }

    /**
     * get opsClusterVO by node id
     *
     * @param nodeId node id
     * @return OpsClusterVO
     */
    public OpsClusterVO getOpsClusterVOByNodeId(String nodeId) {
        for (OpsClusterVO opsClusterVO : listCluster()) {
            if (opsClusterVO.getClusterNodes().stream().anyMatch(node -> nodeId.equals(node.getNodeId()))) {
                return opsClusterVO;
            }
        }
        return null;
    }
}
