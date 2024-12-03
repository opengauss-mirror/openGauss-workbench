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
 * DeploymentApi.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/api/DeploymentApi.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes.api;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.exception.MarsRuntimeException;
import org.opengauss.admin.container.kubernetes.K8sClientBuilder;
import org.opengauss.admin.container.util.HttpsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * DeploymentApi
 *
 * @since 2024-8-26 16:39
 **/
@Component
public class DeploymentApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentApi.class);

    /**
     * 查询deployment资源列表
     *
     * @param k8sCluster    集群（所属域）
     * @param namespace     命名空间
     * @param fieldSelector 字段选择器
     * @param labelSelector 标签选择器
     * @return list
     */
    public List<V1Deployment> getDeploymentsByNamespacesAndSelectorHttpInfo(K8sCluster k8sCluster, String namespace,
                                                                            String fieldSelector,
                                                                            String labelSelector) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        AppsV1Api api =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(k8sCluster.getToken()).buildAppsV1Api(k8sCluster.getId());
        try {
            ApiResponse<V1DeploymentList> v1DeploymentListApiResponse =
                    api.listNamespacedDeploymentWithHttpInfo(namespace, null, null, null, fieldSelector,
                            labelSelector, null, null, null, null, false);
            if (HttpsUtil.isSuccess(v1DeploymentListApiResponse.getStatusCode())) {
                return v1DeploymentListApiResponse.getData().getItems();
            } else {
                LOGGER.warn("get deploy http failed,statusCode:{}", v1DeploymentListApiResponse.getStatusCode());
                return Collections.emptyList();
            }
        } catch (ApiException e) {
            LOGGER.error("get deploy failed:{}", e.getResponseBody());
            throw new MarsRuntimeException("获取 deployment 失败");
        }
    }
}
