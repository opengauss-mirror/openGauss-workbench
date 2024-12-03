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
 * PodApi.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/api/PodApi.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes.api;

import com.google.gson.JsonSyntaxException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.kubernetes.K8sClientBuilder;
import org.opengauss.admin.container.util.HttpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * PodApi
 *
 * @since 2024-8-26 16:39
 **/
@Component
public class PodApi {
    private static final Logger logger = LoggerFactory.getLogger(PodApi.class);

    /**
     * 获取指定名字的pod，失败或者没有就null
     *
     * @param k8sCluster 集群
     * @param namespace  分区ns
     * @param podName    实例名字
     * @return Optional
     */
    public Optional<V1Pod> getPodsByNamespacesAndPodName(K8sCluster k8sCluster, String namespace, String podName) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CoreV1Api coreV1Api = K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCoreV1Api(clusterId);
        try {
            ApiResponse<V1Pod> apiResponse = coreV1Api.readNamespacedPodWithHttpInfo(podName, namespace, null);
            if (HttpsUtil.isSuccess(apiResponse.getStatusCode())) {
                return Optional.ofNullable(apiResponse.getData());
            }
            logger.warn("get pod failed. podName info={" + podName + "}");
            return Optional.empty();
        } catch (ApiException e) {
            logger.error("get podByName error", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 查询pod列表
     *
     * @param k8sCluster    集群（所属域）
     * @param namespace     命名空间
     * @param labelSelector 标签选择器
     * @param fieldSelector 字段选择器
     * @return list
     */
    public List<V1Pod> getPodsByNamespacesAndSelector(K8sCluster k8sCluster, String namespace, String labelSelector,
                                                      String fieldSelector) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CoreV1Api coreV1Api = K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCoreV1Api(clusterId);
        try {
            ApiResponse<V1PodList> apiResponse = coreV1Api.listNamespacedPodWithHttpInfo(namespace, null, null, null,
                    fieldSelector, labelSelector, null, null, null, null, false);
            if (HttpsUtil.isSuccess(apiResponse.getStatusCode())) {
                return apiResponse.getData().getItems();
            }
            logger.warn("get list pod failed , info={" + labelSelector + "}");
            return Collections.emptyList();
        } catch (ApiException e) {
            logger.error("get pod list error", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 删除pod
     *
     * @param k8sCluster 集群（所属域）
     * @param namespace  命名空间
     * @param podName    pod名称
     * @return boolean
     */
    public Boolean deletePodByNamespacedAndName(K8sCluster k8sCluster, String namespace, String podName) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CoreV1Api coreV1Api = K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCoreV1Api(clusterId);
        try {
            ApiResponse<V1Pod> apiResponse = coreV1Api.deleteNamespacedPodWithHttpInfo(podName, namespace, null,
                    null, null, null, null, null);
            if (HttpsUtil.isSuccess(apiResponse.getStatusCode())) {
                return Boolean.TRUE;
            }
        } catch (JsonSyntaxException e) {
            return Boolean.TRUE;
        } catch (ApiException e) {
            logger.error("delete pod failed", e.getMessage());
        }
        return Boolean.FALSE;
    }
}

