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

import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataPoint
 *
 * @author: wangchao
 * @Date: 2025/3/15 11:53
 * @Description: DataPoint
 * @since 7.0.0-RC2
 **/
@Data
public class DataPoint {
    private String value;
    private Instant epochNanos;
    private Instant startEpochNanos;
    private Map<String, String> attributes = new HashMap<>();

    @Override
    public String toString() {
        return "value=" + value;
    }

    /**
     * used display data details
     *
     * @return string
     */
    public String detail() {
        return "{value='" + value + '\'' + ", startEpochNanos='" + startEpochNanos + '\'' + ",  epochNanos='"
            + epochNanos + '\'' + ",  attributes=" + formatAttributes() + "}";
    }

    private String formatAttributes() {
        return attributes.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "='" + entry.getValue() + "'")
            .collect(Collectors.joining(", ", "{", "}"));
    }
}
