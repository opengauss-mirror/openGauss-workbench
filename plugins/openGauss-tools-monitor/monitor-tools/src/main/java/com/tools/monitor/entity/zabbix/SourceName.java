/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity.zabbix;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * SourceName
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SourceName {
    private String connectName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;
}
