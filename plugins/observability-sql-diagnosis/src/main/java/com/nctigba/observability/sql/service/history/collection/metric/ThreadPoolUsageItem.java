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
 * ThreadPoolUsageItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class ThreadPoolUsageItem extends PrometheusCollectionItem {
    @Override
    public String getPrometheusParam(List<HisDiagnosisThreshold> thresholds) {
        if (CollectionUtils.isEmpty(thresholds)) {
            return MetricCommon.THREAD_POOL_USAGE_RATE;
        } else {
            StringBuilder sb = new StringBuilder(MetricCommon.THREAD_POOL_USAGE_RATE);
            for (HisDiagnosisThreshold threshold : thresholds) {
                if (ThresholdCommon.THREAD_POOL_USAGE_RATE.equals(threshold.getThreshold())) {
                    sb.append(">=");
                    sb.append(threshold.getThresholdValue());
                }
            }
            return sb.toString();
        }
    }
}
