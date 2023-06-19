/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseFunctionSPDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String functionSPName;
    private String oid;
}
