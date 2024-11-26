/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CollectPeriod
 *
 * @author liu
 * @since 2023-10-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("collect_period")
public class CollectPeriod {
    @TableId(value = "task_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long taskId;
    private String host;
    private String hostUser;
    private String taskName;
    private String startTime;
    private String endTime;
    private String timeInterval;
    private String pid;
    private String filePath;
    private String currentStatus;
    @JsonProperty("switchStatus")
    private boolean isSwitchStatus;
}
