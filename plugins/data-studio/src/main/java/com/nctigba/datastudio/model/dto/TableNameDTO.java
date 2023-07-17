/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * TableNameDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class TableNameDTO {
    private String uuid;
    private String oid;
    private String schema;
    private String tableName;
}
