/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseCreateSequenceDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String sequenceName;
    private String start;
    private String increment;
    private String minValue;
    private String maxValue;
    private String cache;
    private String cycle;
    private String tableSchema;
    private String tableName;
    private String tableColumn;
}
