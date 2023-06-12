/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RenameDatabaseDTO {

    private String uuid;
    private String databaseName;
    private String oldDatabaseName;
    private String conRestrictions;
}
