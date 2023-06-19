/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseConstraintPkDTO {
    private String uuid;
    private String schema;
    private String tableName;
    private String column;
}

