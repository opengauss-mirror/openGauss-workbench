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
 */

package org.opengauss.admin.container.beans;

import lombok.Data;

import java.io.Serializable;

/**
 * 集群主节点切换数据模型
 *
 * @since 2024-08-29
 */
@Data
public class OpenGaussClusterSwitchoverDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据库ID
     */
    private Long id;

    /**
     * 目标主节点名字
     */
    private String targetMasterPod;
}

