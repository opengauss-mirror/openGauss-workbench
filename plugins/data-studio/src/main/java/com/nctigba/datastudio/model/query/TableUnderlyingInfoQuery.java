/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class TableUnderlyingInfoQuery {
    private String tableName;
    private Boolean exists;
    private String tableType;
    private Boolean oids;
    private String tableSpace;
    private Integer fillingFactor;
    private String storage;
    private String comment;
}
