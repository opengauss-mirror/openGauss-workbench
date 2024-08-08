/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * ClusterTaskProviderManager.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/ClusterTaskProviderManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.ClusterTaskProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cluster Installation Service Provider Manager
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Component
public class ClusterTaskProviderManager {
    private static final ConcurrentHashMap<OpenGaussVersionEnum, ClusterTaskProvider> REGISTRY = new ConcurrentHashMap<>();

    /**
     * registry the provider of the specified version
     *
     * @param version  version
     * @param provider provider
     */
    public static void registry(OpenGaussVersionEnum version, ClusterTaskProvider provider) {
        REGISTRY.put(version, provider);
    }

    /**
     * get the provider of the specified version
     *
     * @param version version
     * @return provider
     */
    public Optional<ClusterTaskProvider> provider(OpenGaussVersionEnum version) {
        return Optional.ofNullable(REGISTRY.get(version));
    }
}
