/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;
import lombok.Generated;

/**
 * SchemaManagerRequest
 *
 * @since 2023-6-26
 */
@Data
@Generated
public class SchemaManagerQuery {
    private String uuid;

    private String oid;

    private String schemaName;

    private String owner;

    private String description;
}
