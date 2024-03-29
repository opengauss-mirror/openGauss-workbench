/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseSequenceDdlDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseSequenceDdlDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String sequenceName;
    private String schema;
}
