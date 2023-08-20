/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OptionDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class OptionDTO {
    private String option;
    private Boolean isCheck;
}
