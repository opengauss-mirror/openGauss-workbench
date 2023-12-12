/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseCreateTablespaceDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class DatabaseCreateTablespaceDTO {
    private String uuid;
    private String tablespaceName;
    private String owner;
    private String path;
    @JsonProperty("relativePath")
    private boolean isRelativePath;
    private String maxStorage;
    private String sequentialOverhead;
    private String nonSequentialOverhead;
    private String comment;
}
