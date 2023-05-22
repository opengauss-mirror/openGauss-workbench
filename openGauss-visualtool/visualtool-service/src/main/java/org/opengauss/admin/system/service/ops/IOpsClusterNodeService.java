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
 * IOpsClusterNodeService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsClusterNodeService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.common.core.dto.ops.ClusterNodeDto;

import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/8/18 09:15
 **/
public interface IOpsClusterNodeService extends IService<OpsClusterNodeEntity> {
    List<OpsClusterNodeEntity> listClusterNodeByClusterId(String clusterId);

    long countByHostId(String hostId);

    long countByHostUserId(String hostUserId);

    Map<String, List<OpsClusterNodeEntity>> listClusterNodeByClusterIds(List<String> clusterIds);

    /**
     * get ClusterNodeDto info by clusterNodeId
     *
     * @param clusterNodeId clusterNodeId
     * @return ClusterNodeDto
     */
    ClusterNodeDto getClusterNodeDtoByNodeId(String clusterNodeId);
}
