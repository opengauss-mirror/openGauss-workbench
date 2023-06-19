/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class TableDataQuery {
    private String winId;
    private String uuid;
    private String schema;
    private String tableName;
    private Integer pageNum;
    private Integer pageSize;
}
