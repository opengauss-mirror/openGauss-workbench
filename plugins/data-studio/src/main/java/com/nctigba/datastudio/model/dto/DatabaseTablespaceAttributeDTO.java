/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseTablespaceAttributeDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseTablespaceAttributeDTO {
    private String tablespaceName;
    private String owner;
    private String path;
    private boolean isRelativePath;
    private String maxStorage;
    private String sequentialOverhead;
    private String nonSequentialOverhead;
    private String comment;
}
