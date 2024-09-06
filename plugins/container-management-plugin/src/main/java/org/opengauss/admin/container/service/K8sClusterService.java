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
 * K8sClusterService.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/service/K8sClusterService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.service;

import org.opengauss.admin.container.beans.K8sCluster;

import java.util.List;
import java.util.Map;

/**
 * K8sClusterService
 *
 * @since 2024-08-29
 */
public interface K8sClusterService {
    /**
     * 添加k8s集群配置信息
     *
     * @param k8sCluster cluster
     * @return string
     */
    String addK8sClusterInfo(K8sCluster k8sCluster);

    /**
     * 删除k8s集群信息
     *
     * @param k8sId id
     * @return string
     */
    String deleteK8sClusterInfo(String k8sId);

    /**
     * 修改k8s集群配置信息
     *
     * @param k8sCluster cluster
     * @return string
     */
    String updateK8sClusterInfo(K8sCluster k8sCluster);

    /**
     * 查看k8s集群配置信息
     *
     * @param k8sId id
     * @return list
     */
    List<K8sCluster> getK8sClusterInfo(String k8sId);

    /**
     * 分页查看k8s集群配置信息
     *
     * @param k8sId    id
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @return map
     */
    Map<String, Object> getK8sClusterPageInfo(String k8sId, Integer pageNum, Integer pageSize);
}
