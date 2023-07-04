/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model.history.point;

import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import lombok.Data;
import lombok.experimental.Accessors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    /**
     *
     * AspAnalysisDTO
     * @since 2023-07-04
     */
    public AspAnalysisDTO(int startTime, int endTime) {
        super();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        this.startTime = simpleDateFormat.format(new Date(startTime * PrometheusConstants.MS));
        this.endTime = simpleDateFormat.format(new Date(endTime * PrometheusConstants.MS));
    }
}
