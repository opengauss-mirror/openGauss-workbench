/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseMetaArrayIdSchemaQuery
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseMetaArrayIdSchemaQuery {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String type;
}
