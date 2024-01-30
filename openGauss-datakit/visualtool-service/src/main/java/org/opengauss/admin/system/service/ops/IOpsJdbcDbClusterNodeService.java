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
 * IOpsJdbcDbClusterNodeService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsJdbcDbClusterNodeService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lhf
 * @date 2023/1/13 11:08
 **/
public interface IOpsJdbcDbClusterNodeService extends IService<OpsJdbcDbClusterNodeEntity> {
    void del(String clusterNodeId);

    /**
     * find jdbc cluster by ip, port and username
     *
     * @param ip ip
     * @param port port
     * @param username username
     * @return jdbc cluster info
     */
    OpsJdbcDbClusterNodeEntity getClusterNodeByIpAndPort(String ip, String port, String username);

    Set<String> fuzzyQueryClusterIdsByIp(String ip);

    Map<String, List<OpsJdbcDbClusterNodeEntity>> mapClusterNodesByClusterId(Set<String> clusterIds);

    void delByClusterId(String clusterId);

    void update(String clusterNodeId, JdbcDbClusterNodeInputDto clusterNodeInput);

    void add(String clusterId, JdbcDbClusterNodeInputDto clusterNodeInput);

    boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput);

    Map<String,Object> monitor(String clusterNodeId, String businessId);
}
