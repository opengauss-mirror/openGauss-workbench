/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * ConstraintDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class ConstraintDTO {
    private String attName;
    private String conName;
    private String oldConName;
    private String conType;
    private String constraintDef;
    @JsonProperty("conDeferrable")
    private Boolean conDeferrable;
    private String nspName;
    private String tbName;
    private String colName;
    private String description;
    private int type;
}
