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
 * IOpsClusterService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IOpsClusterService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.ClusterSummaryVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lhf
 * @date 2022/8/6 17:37
 **/
public interface IOpsClusterService extends IService<OpsClusterEntity> {

    List<OpsClusterVO> listCluster();

    ClusterSummaryVO summary();

    void monitor(String clusterId, String hostId, String businessId);

    Map<String, Integer> threadPoolMonitor();

    long countByHostId(String hostId);

    String connectNum(Connection connection);

    String session(Connection connection);

    String lock(Connection connection);

    CheckSummaryVO check(String clusterId, String rootPassword);
}
