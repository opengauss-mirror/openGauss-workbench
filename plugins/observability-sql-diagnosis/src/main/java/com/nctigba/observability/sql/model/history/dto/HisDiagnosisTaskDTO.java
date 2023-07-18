/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * HisDiagnosisTaskDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
public class HisDiagnosisTaskDTO {
    private String clusterId;
    private String nodeId;
    private Date hisDataStartTime;
    private Date hisDataEndTime;
    List<OptionDTO> configs;
    List<HisThresholdDTO> thresholds;
}
