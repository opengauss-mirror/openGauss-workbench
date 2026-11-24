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

package com.nctigba.alert.monitor.model.query;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/4/14 14:55
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertRecordQuery {
    private String type;
    private String clusterNodeId;

    /**
     * When processing a GET request, the platform is unable to convert the string ("yyyy-MM-dd HH:mm:ss")
     * parameter to a date.
     */
    private String startTime; // yyyy-MM-dd HH:mm:ss
    private String endTime; // yyyy-MM-dd HH:mm:ss
    private String recordStatus;
    private String alertStatus;
    private String alertLevel;
}
