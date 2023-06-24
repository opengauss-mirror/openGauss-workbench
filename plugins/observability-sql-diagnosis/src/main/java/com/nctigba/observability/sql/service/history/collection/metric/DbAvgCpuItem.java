/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.metric;

import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * DbAvgCpuItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DbAvgCpuItem extends PrometheusCollectionItem {
    @Override
    String getPrometheusParam(List<HisDiagnosisThreshold> thresholds) {
        if (CollectionUtils.isEmpty(thresholds)) {
            return MetricCommon.DB_AVG_CPU_USAGE_RATE;
        } else {
            StringBuilder sb = new StringBuilder(MetricCommon.DB_AVG_CPU_USAGE_RATE);
            for (HisDiagnosisThreshold threshold : thresholds) {
                if (ThresholdCommon.DB_CPU_USAGE_RATE.equals(threshold.getThreshold())) {
                    sb.append(">=");
                    sb.append(threshold.getThresholdValue());
                }
            }
            return sb.toString();
        }
    }
}
