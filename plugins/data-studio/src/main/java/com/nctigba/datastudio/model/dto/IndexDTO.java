/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IndexDTO {
    private String attname;
    private String indexName;
    private String oldIndexName;
    private String amname;
    private String description;
    private String expression;
    private Boolean unique;
    private int type;
}
