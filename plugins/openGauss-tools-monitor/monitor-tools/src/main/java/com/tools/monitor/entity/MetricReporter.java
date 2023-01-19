/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import lombok.Data;

import java.util.List;

/**
 * MetricReporter
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class MetricReporter {
    private String metric;

    private List<String> labes;

    private Object value;
}
