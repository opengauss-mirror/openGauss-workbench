/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseViewDTO
 *
 * @since 2023-8-15
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseViewDTO {
    private String uuid;
    private String schema;
    private String viewName;
    private String newViewName;
    private String newSchema;
}
