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
 * K8sApiClientRegister.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/kubernetes/K8sApiClientRegister.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.kubernetes;

import io.kubernetes.client.openapi.ApiClient;

import java.util.HashMap;
import java.util.Map;

/**
 * K8sApiClientRegister
 *
 * @since 2024-7-20 13:19
 **/
public class K8sApiClientRegister {
    /**
     * apiClient缓存
     */
    private static final Map<String, ApiClient> cache = new HashMap<>();

    /**
     * 注册apiclient到缓存
     *
     * @param key       apiclien的唯一标识
     * @param apiClient apiclient对象
     */
    public static void register(String key, ApiClient apiClient) {
        cache.put(key, apiClient);
    }

    /**
     * 从缓存获取apiclient
     *
     * @param key apiclient唯一标识
     * @return ApiClient
     */
    public static ApiClient apiClient(String key) {
        return cache.get(key);
    }

    /**
     * 从缓存删除apiClient（取消注册）
     *
     * @param key apiClient唯一标识
     */
    public static void unRegister(String key) {
        cache.remove(key);
    }
}
