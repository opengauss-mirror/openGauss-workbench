/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.Generated;
import lombok.experimental.Accessors;

/**
 * ConnCountDTO
 *
 * @author luomeng
 * @since 2023/6/29
 */
@Data
@Accessors(chain = true)
@Generated
public class ConnCountDTO {
    private String beforeStartTime;
    private String beforeEndTime;
    private String beforeSessionCount;
    private String nowStartTime;
    private String nowEndTime;
    private String nowSessionCount;
}
