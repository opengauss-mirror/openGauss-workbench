/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class SchemaManagerRequest {
    private String uuid;

    private String oid;

    private String schemaName;

    private String owner;

    private String description;
}
