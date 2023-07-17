/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * CreateDatabaseDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class CreateDatabaseDTO {
    private String uuid;
    private String databaseName;
    private String databaseCode;
    private String compatibleType;
    private String collation;
    private String characterType;
    private String conRestrictions;
}
