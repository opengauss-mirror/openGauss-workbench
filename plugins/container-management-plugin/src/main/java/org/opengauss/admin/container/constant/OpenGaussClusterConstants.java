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

package org.opengauss.admin.container.constant;

/**
 * OenGaussClusterConstants
 *
 * @author huangshiya
 * @since 2024/5/20
 */
public class OpenGaussClusterConstants {
    /**
     * 集群中运行的状态
     */
    public static final String CLUSTER_STATE_RUNNING = "Running";

    /**
     * 集群主备切换时 参数 状态的值
     */
    public static final String CLUSTER_SWITCHOVER_STATUS = "begin";

    /**
     * cleanup镜像类型
     */
    public static final String OPENGAUSS_CLUSTER_IMAGE_TYPE_CLEANUP = "cleanup";
}
