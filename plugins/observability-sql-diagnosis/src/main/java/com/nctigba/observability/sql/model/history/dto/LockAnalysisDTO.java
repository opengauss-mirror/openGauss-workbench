/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * lock
 *
 * @author luomeng
 * @since 2023/6/12
 */
@Data
@Accessors(chain = true)
public class LockAnalysisDTO {
    private String logTime;
    private String processId;
    private String transactionId;
    private String logData;
}
