/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * ClusterOpsProviderManager.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/ClusterOpsProviderManager.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.ClusterOpsProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cluster Installation Service Provider Manager
 *
 * @author lhf
 * @date 2022/8/12 09:08
 **/
@Component
public class ClusterOpsProviderManager {
    private static final ConcurrentHashMap<String, ClusterOpsProvider> REGISTRY = new ConcurrentHashMap<>();

    public static void registry(OpenGaussVersionEnum version, ClusterOpsProvider provider) {
        REGISTRY.put(version.name(), provider);
    }


    public Optional<ClusterOpsProvider> provider(OpenGaussVersionEnum version) {
        return Optional.ofNullable(REGISTRY.get(version.name()));
    }
}
