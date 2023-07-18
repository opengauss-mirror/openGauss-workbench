/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * PrometheusDataDTO
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Data
@Accessors(chain = true)
public class PrometheusDataDTO {
    private String chartName;
    private String unit;
    private List<String> time;
    private List<MetricDataDTO> datas;
    private List<AspAnalysisDTO> data;
}
