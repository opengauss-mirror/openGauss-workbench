/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * K8sClusterCacheManager.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/cache
 * /K8sClusterCacheManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service.cache;

import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.exception.MarsRuntimeException;
import org.opengauss.admin.container.mapper.K8sClusterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * K8sClusterCacheManager
 *
 * @since 2024-08-29
 */
@Component
public class K8sClusterCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sClusterCacheManager.class);
    private static final Map<String, K8sCluster> clusterCacheMap = new ConcurrentHashMap<>();

    @Autowired
    private K8sClusterMapper k8sClusterMapper;

    /**
     * 初始化k8s集群信息至缓存
     *
     * @return list
     */
    public List<K8sCluster> initK8sClusterCache() {
        LOGGER.info("Initialize k8s cluster cache");
        List<K8sCluster> k8sClusterList = k8sClusterMapper.selectList(null);
        if (CollectionUtils.isEmpty(k8sClusterList)) {
            LOGGER.warn("Get k8s cluster list empty");
            return Collections.emptyList();
        }
        LOGGER.info("Initialize k8s cluster cache, number of clusters:{}", k8sClusterList.size());

        // 存放缓存
        putCluster(k8sClusterList);
        return k8sClusterList;
    }

    /**
     * k8s集群信息存放缓存
     *
     * @param k8sClusterList k8s集群列表
     */
    public void putCluster(List<K8sCluster> k8sClusterList) {
        if (CollectionUtils.isEmpty(k8sClusterList)) {
            LOGGER.warn("The k8s cluster list to be updated is empty, do not update cache");
            return;
        }
        Map<String, K8sCluster> k8sClusterMap = new HashMap<>();
        for (K8sCluster k8sCluster : k8sClusterList) {
            String id = k8sCluster.getId();
            if (StringUtils.isNotBlank(id)) {
                k8sClusterMap.put(id, k8sCluster);
            }
        }
        if (CollectionUtils.isEmpty(k8sClusterMap)) {
            LOGGER.warn("No available k8s cluster, cache not updated");
            return;
        }
        clusterCacheMap.clear();
        clusterCacheMap.putAll(k8sClusterMap);
    }

    /**
     * 获取所有k8s集群列表
     *
     * @return list
     */
    public List<K8sCluster> listCluster() {
        if (CollectionUtils.isEmpty(clusterCacheMap)) {
            LOGGER.warn("Retrieve k8s cluster information from cache as empty, initialize data");
            return initK8sClusterCache();
        }
        List<K8sCluster> clusters = new ArrayList<>();
        if (!clusterCacheMap.isEmpty()) {
            clusters.addAll(clusterCacheMap.values());
        }
        return clusters;
    }

    /**
     * 根据集群ID获取单个k8s集群信息
     *
     * @param clusterId id
     * @return K8sCluster
     */
    public K8sCluster getCluster(String clusterId) {
        Assert.hasText(clusterId);
        K8sCluster cacheData = clusterCacheMap.getOrDefault(clusterId, null);
        if (cacheData != null) {
            return cacheData;
        }
        List<K8sCluster> clusters = initK8sClusterCache();
        for (K8sCluster cluster : clusters) {
            if (cluster.getId().equals(clusterId)) {
                return cluster;
            }
        }
        throw new MarsRuntimeException("Abnormal acquisition of k8s cluster information");
    }

    /**
     * 重置缓存
     */
    public void resetCluster() {
        LOGGER.info("reset cache");
        clusterCacheMap.clear();
        // 重置缓存
        initK8sClusterCache();
    }
}
