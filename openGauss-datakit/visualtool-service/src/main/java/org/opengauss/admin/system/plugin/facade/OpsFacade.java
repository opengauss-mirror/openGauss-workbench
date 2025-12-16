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

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
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

    @Autowired
    private IOpsClusterNodeService opsClusterNodeService;

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
        return opsClusterService.isOpsClusterExists(nodeId);
    }

    /**
     * Get opsClusterVO by cluster id
     *
     * @param clusterId cluster id
     * @return OpsClusterVO
     */
    public OpsClusterVO getOpsClusterVOByClusterId(String clusterId) {
        return opsClusterService.getOpsClusterVoByClusterId(clusterId);
    }

    /**
     * get opsClusterVO by node id
     *
     * @param nodeId node id
     * @return OpsClusterVO
     */
    public OpsClusterVO getOpsClusterVOByNodeId(String nodeId) {
        return opsClusterService.getOpsClusterVoByNodeId(nodeId);
    }

    /**
     * Get opsClusterNodeVO by node id
     *
     * @param nodeId node id
     * @return OpsClusterNodeVO
     */
    public OpsClusterNodeVO getOpsClusterNodeVOByNodeId(String nodeId) {
        return opsClusterNodeService.getOpsClusterNodeVoByNodeId(nodeId);
    }
}
