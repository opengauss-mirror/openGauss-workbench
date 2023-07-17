/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.service.history.collection.metric.ThreadPoolUsageItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * TestThreadPoolUsageItem
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestThreadPoolUsageItem {
    @InjectMocks
    private ThreadPoolUsageItem item;

    @Test
    public void testGetPrometheusParam_hasThreshold() {
        HisDiagnosisThreshold threshold = new HisDiagnosisThreshold();
        threshold.setThreshold(ThresholdCommon.THREAD_POOL_USAGE_RATE);
        threshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> thresholds = new ArrayList<>() {{
            add(threshold);
        }};
        String metric = item.getPrometheusParam(thresholds);
        assertNotEquals(metric, MetricCommon.THREAD_POOL_USAGE_RATE);
    }

    @Test
    public void testGetPrometheusParam_noThreshold() {
        String metric = item.getPrometheusParam(null);
        assertEquals(metric, MetricCommon.THREAD_POOL_USAGE_RATE);
    }
}
