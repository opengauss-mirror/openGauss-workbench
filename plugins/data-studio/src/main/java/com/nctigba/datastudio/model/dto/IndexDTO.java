/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * IndexDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class IndexDTO {
    private String attName;
    private String indexName;
    private String oldIndexName;
    private String amName;
    private String description;
    private String expression;
    private Boolean unique;
    private int type;
}
