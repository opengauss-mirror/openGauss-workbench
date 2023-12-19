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
 *  ClusterController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/controller/ClusterController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.controller;

import java.util.List;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.log.service.ClusterManager;

@RestController
@RequestMapping("/logSearch/api/v1")
public class ClusterController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/clusters")
    public List<OpsClusterVO> listCluster() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        return clusters;
    }
}