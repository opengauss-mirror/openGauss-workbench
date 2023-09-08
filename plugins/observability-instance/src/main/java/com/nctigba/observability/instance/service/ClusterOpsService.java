/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.dto.cluster.ClusterStateDto;
import com.nctigba.observability.instance.dto.cluster.ClustersDto;
import com.nctigba.observability.instance.dto.cluster.NodeRelationDto;
import com.nctigba.observability.instance.dto.cluster.SyncSituationDto;

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
     * @return ClustersDto
     */
    List<ClustersDto> listClusters();

    /**
     * nodes
     *
     * @param clusterId String
     * @return List<JSONObject>
     */
    List<JSONObject> nodes(String clusterId);

    /**
     * relation
     *
     * @param clusterId String
     * @return List<NodeRelationDto>
     */
    List<NodeRelationDto> relation(String clusterId);

    /**
     * allClusterState
     *
     * @return ClusterStateDto
     */
    List<ClusterStateDto> allClusterState();

    /**
     * allStandbyNodes
     *
     * @return SyncSituationDto
     */
    List<SyncSituationDto> allStandbyNodes();

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
}
