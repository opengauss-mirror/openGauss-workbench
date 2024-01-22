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

package org.opengauss.tun.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ParameterRecommendation
 *
 * @author liu liu
 * @since 2023-12-05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("parameter_recommendation")
@Builder
public class ParameterRecommendation {
    private String trainingId;  // TrainingConfig的id 关联集群名称
    @TableId("parameter_id")
    private String parameterId;
    private String clusterName;
    private String db;
    private String startTime;
    @TableField(exist = false)
    private String endTime;
    @TableField(exist = false)
    private List<String> timeInterval;
    private String initialTps;
    private String optimalTps;
    private String performanceImprovement;
    private String initialParameterValues;
    private String adviseParameterValues;
}
