/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.domain.model.ops;

import lombok.Data;

/**
 * receive web request body
 *
 * @author wbd
 * @since 2024/1/29 17:05
 **/
@Data
public class DisasterBody {
    private String primaryClusterName;

    private String primaryDeviceManager;

    private String standbyClusterName;

    private String standbyDeviceManager;

    private String clusterId;

    // 监控和解除容灾关系使用
    private String businessId;

    // install和switchover使用
    private String primaryBusinessId;

    private String standbyBusinessId;
}
