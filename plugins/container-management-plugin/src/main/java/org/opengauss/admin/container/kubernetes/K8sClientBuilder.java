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
 * K8sClientBuilder.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/K8sClientBuilder.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.credentials.AccessTokenAuthentication;

/**
 * k8s客户端创建工具
 *
 * @since 2024-8-26 16:39
 */
public class K8sClientBuilder {
    private ApiClient apiClient;
    private String token;
    private String apiServer;

    /**
     * 私有构造方法
     */
    private K8sClientBuilder() {
    }

    /**
     * K8sClientBuilder对象构建器
     *
     * @return K8sClientBuilder
     */
    public static K8sClientBuilder builder() {
        return new K8sClientBuilder();
    }

    /**
     * 设置apiserver地址
     *
     * @param apiServer apiserver地址（一般是http(s)协议）
     * @return K8sClientBuilder
     */
    public K8sClientBuilder apiServer(String apiServer) {
        this.apiServer = apiServer;
        return this;
    }

    /**
     * 设置访问授权token
     *
     * @param token token
     * @return K8sClientBuilder
     */
    public K8sClientBuilder token(String token) {
        if (token != null) {
            this.token = token;
        }
        return this;
    }

    /**
     * 构建ApiClient（缓存中若不存在则新构建并放入缓存）
     *
     * @param id ApiClient唯一标识
     * @return K8sClientBuilder
     */
    public K8sClientBuilder buildApiClient(String id) {
        if (K8sApiClientRegister.apiClient(id) == null) {
            ClientBuilder clientBuilder = new ClientBuilder().setBasePath(this.apiServer).setVerifyingSsl(false)
                    .setAuthentication(new AccessTokenAuthentication(this.token));
            this.apiClient = clientBuilder.build();
            K8sApiClientRegister.register(id, this.apiClient);
        } else {
            this.apiClient = K8sApiClientRegister.apiClient(id);
        }
        return this;
    }

    /**
     * 构建coreApi
     *
     * @param id apiClient唯一标识
     * @return CoreV1Api
     */
    public CoreV1Api buildCoreV1Api(String id) {
        buildApiClient(id);
        return new CoreV1Api(this.apiClient);
    }

    /**
     * 构建customApi
     *
     * @param id apiClient唯一标识
     * @return CustomObjectsApi
     */
    public CustomObjectsApi buildCustomObjectsApi(String id) {
        buildApiClient(id);
        return new CustomObjectsApi(this.apiClient);
    }

    /**
     * 构建appApi
     *
     * @param id apiClient唯一标识
     * @return AppsV1Api
     */
    public AppsV1Api buildAppsV1Api(String id) {
        buildApiClient(id);
        return new AppsV1Api(this.apiClient);
    }
}
