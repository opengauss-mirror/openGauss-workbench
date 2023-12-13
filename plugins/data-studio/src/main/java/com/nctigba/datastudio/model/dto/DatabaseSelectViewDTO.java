/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseSelectViewDTO
 *
 * @since 2023-9-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseSelectViewDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String viewName;
    private String schema;
    private Integer pageNum;
    private Integer pageSize;
}
