/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.container.service.dto;

import lombok.Data;
import org.opengauss.admin.container.beans.K8sCluster;
import org.opengauss.admin.container.beans.kubernetes.OpenGaussClusterCrd;

/**
 * ClusterCheckParams
 *
 * @since 2024-08-29
 */
@Data
public class ClusterCheckParams {
    /**
     * K8sCluster
     */
    private K8sCluster k8sCluster;

    /**
     * OpenGaussClusterCrd
     */
    private OpenGaussClusterCrd openGaussClusterCrd;

    /**
     * opengaussName
     */
    private String opengaussName;

    /**
     * namespace
     */
    private String namespace;
}
