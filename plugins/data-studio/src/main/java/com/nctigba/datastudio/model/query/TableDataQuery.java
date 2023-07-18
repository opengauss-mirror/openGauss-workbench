/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;

/**
 * TableDataQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class TableDataQuery {
    private String winId;
    private String uuid;
    private String schema;
    private String tableName;
    private Integer pageNum;
    private Integer pageSize;
}
