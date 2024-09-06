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
 * CrdDefinition.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/kubernetes/CrdDefinition.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans.kubernetes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * crd的标识信息
 * 请求路径： /apis/{group}/{version}/namespaces/{namespace}/{plural}/{name}
 * 只记录Crd的统一不变信息，namespace记录默认分区，name由外部参数决定
 *
 * @since 2024-03-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrdDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * <optional> kind is normally the CamelCased singular type, eg: FlinkCluster
     */
    private String kind;

    /**
     * group name to use for REST API: /apis/<group>/<version>, eg: flinkoperator.k8s.io
     */
    private String group;

    /**
     * version name to use for REST API: /apis/<group>/<version>, eg: v1beta1
     */
    private String version;

    /**
     * plural name to be used in the URL: /apis/<group>/<version>/<plural>, eg: flinkclusters
     */
    private String plural;

    /**
     * <optional> singular name to be used as an alias on the CLI and for display, eg: flinkcluster
     */
    private String singular;

    /**
     * <optional> default namespace used for CRD, eg: bigdata-flink
     */
    private String namespace;
}
