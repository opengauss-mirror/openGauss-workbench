/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class SelectDataQuery {
    private String uuid;
    private String oid;
    private String schema;
    private String tableName;
}
