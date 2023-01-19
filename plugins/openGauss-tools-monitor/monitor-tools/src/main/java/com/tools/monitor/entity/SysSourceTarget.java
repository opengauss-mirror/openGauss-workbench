/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * SysSourceTarget
 *
 * @author liu
 * @since 2022-10-01
 */
@Data
public class SysSourceTarget {
    /**
     * dataSourceId
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long dataSourceId;

    /**
     * jobIds
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> jobIds;

    /**
     * lastReleaseTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date lastReleaseTime;
}
