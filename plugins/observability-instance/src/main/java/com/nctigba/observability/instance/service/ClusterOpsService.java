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
 *  ClusterOpsService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/ClusterOpsService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.observability.instance.model.dto.cluster.ClusterStateDTO;
import com.nctigba.observability.instance.model.dto.cluster.ClusterStatisticsDTO;
import com.nctigba.observability.instance.model.dto.cluster.ClustersDTO;
import com.nctigba.observability.instance.model.dto.cluster.SwitchRecordDTO;
import com.nctigba.observability.instance.model.dto.cluster.SyncSituationDTO;

import java.util.List;
import java.util.Map;

/**
 * ClusterOpsService
 *
 * @author liupengfei
 * @since 2023/8/11
 */
public interface ClusterOpsService {
    /**
     * listClusters
     *
     * @return ClustersDTO
     */
    List<ClustersDTO> listClusters();

    /**
     * nodes
     *
     * @param clusterId String
     * @return JSONObject
     */
    JSONObject nodes(String clusterId);

    /**
     * allClusterState
     *
     * @return ClusterStateDTO
     */
    List<ClusterStateDTO> allClusterState();

    /**
     * allStandbyNodes
     *
     * @return SyncSituationDTO
     */
    List<SyncSituationDTO> allStandbyNodes();

    /**
     * clusterMetrics
     *
     * @param clusterId String
     * @param start Long
     * @param end Long
     * @param step Integer
     * @return Map<String, Object>
     */
    Map<String, Object> clusterMetrics(String clusterId, Long start, Long end, Integer step);

    /**
     * statistics
     *
     * @return ClusterStatisticsDTO
     */
    ClusterStatisticsDTO statistics();

    /**
     * switchRecord
     *
     * @param clusterId String
     * @param start Long
     * @param end Long
     * @return List<SwitchRecordDTO>
     */
    Page<SwitchRecordDTO> switchRecord(String clusterId, Long start, Long end, Integer pageSize, Integer pageNum);

    /**
     * switchRecordUpdate
     *
     * @param clusterId String
     */
    void switchRecordUpdate(String clusterId);
}
