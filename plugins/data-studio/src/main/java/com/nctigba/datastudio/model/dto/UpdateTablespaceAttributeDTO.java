/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * UpdateTablespaceAttributeDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class UpdateTablespaceAttributeDTO {
    private String uuid;
    private String newTablespaceName;
    private String oldTablespaceName;
    private String owner;
    private String maxStorage;
    private String sequentialOverhead;
    private String nonSequentialOverhead;
    private String comment;
}
