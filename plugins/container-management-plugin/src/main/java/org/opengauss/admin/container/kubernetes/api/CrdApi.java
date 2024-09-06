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
 * CrdApi.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/api/CrdApi.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.internal.LinkedTreeMap;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.PatchUtils;
import okhttp3.Call;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.beans.kubernetes.CrdDefinition;
import org.opengauss.admin.container.kubernetes.K8sClientBuilder;
import org.opengauss.admin.container.kubernetes.K8sTypeTokenRegister;
import org.opengauss.admin.container.util.HttpsUtil;
import org.opengauss.admin.container.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * CrdApi
 *
 * @since 2024-8-26 16:39
 **/
@Component
public class CrdApi {
    private static final Logger logger = LoggerFactory.getLogger(CrdApi.class);

    /**
     * 查询crd资源
     *
     * @param k8sCluster    集群（所属域）
     * @param crdDefinition crd的标识信息
     * @param name          名称
     * @param namespace     命名空间
     * @param tClass        crd对应对象类型
     * @return tClass
     */
    @Nullable
    public <T> T getCrdByNameAndNamespace(K8sCluster k8sCluster, CrdDefinition crdDefinition, String name,
                                          String namespace, Class<T> tClass) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CustomObjectsApi customObjectsApi =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCustomObjectsApi(clusterId);

        try {
            Call localVarCall = customObjectsApi.getNamespacedCustomObjectCall(crdDefinition.getGroup(),
                    crdDefinition.getVersion(), namespace, crdDefinition.getPlural(), name, null);
            Type localVarReturnType = K8sTypeTokenRegister.getType(tClass);
            ApiResponse<Object> apiResponse = customObjectsApi.getApiClient().execute(localVarCall, localVarReturnType);
            if (HttpsUtil.isSuccess(apiResponse.getStatusCode())) {
                Object crResult = apiResponse.getData();
                return JSONObject.parseObject(JSON.toJSONString(crResult), tClass);
            }
        } catch (ApiException e) {
            logger.error(String.format("get crd failed , %s", e.getResponseBody()));
        }
        return null;
    }

    /**
     * 查询crd资源列表
     *
     * @param k8sCluster    集群（所属域）
     * @param crdDefinition crd的标识信息
     * @param labelSelector 标签选择器
     * @param fieldSelector 字段选择器
     * @param tClass        crd对应对象类型
     * @return tClass
     */
    public <T> List<T> getCrdListByNamespaceAndSelector(K8sCluster k8sCluster, CrdDefinition crdDefinition,
                                                        String labelSelector, String fieldSelector, Class<T> tClass) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CustomObjectsApi customObjectsApi =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCustomObjectsApi(clusterId);
        try {
            ApiResponse<Object> response =
                    customObjectsApi.listNamespacedCustomObjectWithHttpInfo(crdDefinition.getGroup(),
                            crdDefinition.getVersion(), crdDefinition.getNamespace(), crdDefinition.getPlural(), null,
                            null, null, fieldSelector, labelSelector, null, null, null, null, false);
            if (HttpsUtil.isSuccess(response.getStatusCode())) {
                Object data = response.getData();
                if (data instanceof LinkedTreeMap) {
                    LinkedTreeMap result = (LinkedTreeMap) data;
                    logger.info("getCRDSByNamespaceAndSelector result size is {} ", result.size());
                    Object items = result.get("items");
                    if (items != null) {
                        return JsonUtils.objToArray(items, tClass, true);
                    }
                } else {
                    logger.error("Unexpected data type: {}", data.getClass().getName());
                }
            }
        } catch (ApiException e) {
            logger.error("get crd error , parameter is :  apiServer -> {}, namespace -> {}  , label is {} , field is "
                            + "{} ; error is {} ", apiServerUrl, crdDefinition.getNamespace(), labelSelector,
                    fieldSelector, e.getResponseBody());
        }
        return Collections.emptyList();
    }

    /**
     * 创建crd资源
     *
     * @param k8sCluster    集群（所属域）
     * @param crdDefinition crd的标识信息
     * @param body          crd资源数据
     * @return boolean
     */
    public Boolean createCrd(K8sCluster k8sCluster, CrdDefinition crdDefinition, Object body) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CustomObjectsApi customObjectsApi =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCustomObjectsApi(clusterId);
        try {
            ApiResponse<Object> info =
                    customObjectsApi.createNamespacedCustomObjectWithHttpInfo(crdDefinition.getGroup(),
                            crdDefinition.getVersion(), crdDefinition.getNamespace(), crdDefinition.getPlural(), body,
                            null, null, null);
            if (HttpsUtil.isSuccess(info.getStatusCode())) {
                logger.info("create crd success");
                return Boolean.TRUE;
            }
        } catch (ApiException e) {
            logger.error("create crd error : {}", e.getResponseBody());
        }
        return Boolean.FALSE;
    }

    /**
     * 删除crd资源
     *
     * @param k8sCluster    集群（所属域）
     * @param crdDefinition crd的标识信息
     * @param name          crd资源名称
     * @return boolean
     */
    public Boolean deleteCrdByName(K8sCluster k8sCluster, CrdDefinition crdDefinition, String name) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CustomObjectsApi customObjectsApi =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCustomObjectsApi(clusterId);
        try {
            V1DeleteOptions v1DeleteOptions = new V1DeleteOptions();
            ApiResponse<Object> info =
                    customObjectsApi.deleteNamespacedCustomObjectWithHttpInfo(crdDefinition.getGroup(),
                            crdDefinition.getVersion(), crdDefinition.getNamespace(), crdDefinition.getPlural(), name,
                            0, false, null, null, v1DeleteOptions);
            if (HttpsUtil.isSuccess(info.getStatusCode())) {
                logger.info("delete crd success : {}/{}", crdDefinition, name);
                return Boolean.TRUE;
            }
        } catch (ApiException e) {
            logger.error("delete crd error : {}", e.getResponseBody());
        }
        return Boolean.FALSE;
    }

    /**
     * 更新crd资源
     *
     * @param k8sCluster    集群（所属域）
     * @param crdDefinition crd的标识信息
     * @param name          crd资源名称
     * @param body          crd变更数据
     * @return boolean
     */
    public Boolean updateCRDByNamespaced(K8sCluster k8sCluster, CrdDefinition crdDefinition, String name, Object body) {
        String apiServerUrl = k8sCluster.getApiServer() + ":" + k8sCluster.getPort();
        String token = k8sCluster.getToken();
        String clusterId = k8sCluster.getId();
        CustomObjectsApi customObjectsApi =
                K8sClientBuilder.builder().apiServer(apiServerUrl).token(token).buildCustomObjectsApi(clusterId);
        logger.info("start update crd : {}", JsonUtils.objToStrWithTZ(body));
        try {
            PatchUtils.patch(Object.class,
                    () -> customObjectsApi.patchNamespacedCustomObjectCall(crdDefinition.getGroup(),
                            crdDefinition.getVersion(), crdDefinition.getNamespace(), crdDefinition.getPlural(), name,
                            new V1Patch(JsonUtils.objToStrWithTZ(body)), null, null, null, null),
                    V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH, customObjectsApi.getApiClient());
            return Boolean.TRUE;
        } catch (ApiException e) {
            logger.error("update crd error", e.getMessage());
            return Boolean.FALSE;
        }
    }
}
