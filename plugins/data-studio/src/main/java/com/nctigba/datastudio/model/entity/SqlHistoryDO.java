/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * SqlHistoryDO
 *
 * @since 2023-7-17
 */
@NoArgsConstructor
@Data
@Generated
public class SqlHistoryDO {
    private Integer id;

    private String startTime;

    @JsonProperty("success")
    private boolean isSuccess;

    private String sql;

    private String executeTime;

    @JsonProperty("lock")
    private boolean isLock;

    private String webUser;
}
