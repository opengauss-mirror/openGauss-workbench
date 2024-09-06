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
 * OpenGaussClusterStatusCondition.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/beans/kubernetes
 * /OpenGaussClusterStatusCondition.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.beans.kubernetes;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 集群status condition
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterStatusCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<String> ips;
    private String nodeName;
    private String nodeIp;
    private String phase;
    private String state;
    private String message;
}
