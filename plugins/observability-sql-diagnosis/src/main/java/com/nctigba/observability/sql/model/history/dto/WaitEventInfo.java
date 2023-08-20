/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * WaitEventInfo
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class WaitEventInfo {
    private String eventName;
    private Integer eventCount;
    private String eventDetail;
    private String suggestion;
}
