/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;

/**
 * SelectDataQuery
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class SelectDataQuery {
    private String uuid;
    private String oid;
    private String schema;
    private String tableName;
}
