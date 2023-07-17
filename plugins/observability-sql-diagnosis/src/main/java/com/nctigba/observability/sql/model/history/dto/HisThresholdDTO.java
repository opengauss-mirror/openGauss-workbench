/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * HisThresholdDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
@Generated
public class HisThresholdDTO {
    private String threshold;
    private String thresholdValue;
}
