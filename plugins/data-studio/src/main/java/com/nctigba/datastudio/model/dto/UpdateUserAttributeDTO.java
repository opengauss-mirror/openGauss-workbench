/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * UpdateUserAttributeDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class UpdateUserAttributeDTO {
    private String uuid;
    private String webUser;
    private String oldName;
    private String newName;
    private String beginDate;
    private String endDate;
    private String connectionLimit;
    private String resourcePool;
    private Map<String, Boolean> changePower;
    private Map<String, Boolean> changeBelong;
    private String comment;
    private String type;
}
