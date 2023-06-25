/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AspAnalysisDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class AspAnalysisDTO {
    private String startTime;
    private String endTime;
}
