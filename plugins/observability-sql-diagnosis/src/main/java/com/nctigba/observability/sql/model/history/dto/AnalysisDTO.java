/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import lombok.Data;
import lombok.Generated;

/**
 * Diagnosis analysis dto
 *
 * @author luomeng
 * @since 2023/6/15
 */
@Data
@Generated
public class AnalysisDTO {
    private HisDiagnosisResult.PointType pointType;
    private Object pointData;
    private HisDiagnosisResult.ResultState isHint;
    private String pointState;
}
