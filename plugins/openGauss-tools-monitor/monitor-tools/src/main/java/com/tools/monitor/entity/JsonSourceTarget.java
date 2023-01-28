/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import lombok.Data;

import java.util.List;

/**
 * JsonSourceTarget
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class JsonSourceTarget {
    private List<SysSourceTarget> sysSourceTarget;
}
