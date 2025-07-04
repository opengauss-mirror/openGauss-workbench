/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.agent.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Metric
 *
 * @author: wangchao
 * @Date: 2025/3/15 11:51
 * @Description: Metric
 * @since 7.0.0-RC2
 **/
@Getter
@NoArgsConstructor
public class Metric {
    private String name;
    private String description;
    private String unit;
    private String type;
    @Setter
    private List<DataPoint> points;

    /**
     * constructor
     *
     * @param name metric name
     * @param description metric description
     * @param unit metric unit
     * @param type metric type
     */
    public Metric(String name, String description, String unit, String type) {
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.type = type;
    }

    @Override
    public String toString() {
        return "\n|---------- Metric ----------\n| Name: " + name + "\n| Description: " + description + "\n| Unit: "
            + unit + "\n| Type: " + type + "\n|------ Data Points ------" + (points == null || points.isEmpty()
            ? "\n|(No data points)"
            : points.stream().map(dp -> "\n| " + dp.toString().replace("\n", "\n| ")).collect(Collectors.joining()))
            + "\n|--------------------";
    }
}
