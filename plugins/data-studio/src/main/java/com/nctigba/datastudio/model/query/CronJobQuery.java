/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CronJobQuery
 *
 * @since 2023-11-08
 */
@NoArgsConstructor
@Data
@Generated
@Slf4j
public class CronJobQuery {
    private String uuid;

    private Integer jobId;

    private String jobContent;

    private String nextRunDate;

    private String interval;

    private Integer pageSize;

    private Integer pageNum;
}
