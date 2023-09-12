/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Snapshot.java
 *
 * @since 2023-08-25
 */
@Data
@TableName("snapshot.snapshot")
public class Snapshot {
    private Long snapshotId;
    private Date startTs;
    private Date endTs;
}