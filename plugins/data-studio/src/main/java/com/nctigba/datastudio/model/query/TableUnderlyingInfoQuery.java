/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

/**
 * TableUnderlyingInfoQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class TableUnderlyingInfoQuery {
    private String tableName;
    @JsonProperty("exists")
    private Boolean isExists;
    private String tableType;
    @JsonProperty("oids")
    private Boolean isOids;
    private String tableSpace;
    private Integer fillingFactor;
    private String storage;
    private String comment;
}
