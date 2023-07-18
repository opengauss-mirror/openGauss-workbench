/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

/**
 * TablePKDataQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class TablePKDataQuery {
    private String msg;
    @JsonProperty("PKCreate")
    private Boolean isPKCreate;
}
