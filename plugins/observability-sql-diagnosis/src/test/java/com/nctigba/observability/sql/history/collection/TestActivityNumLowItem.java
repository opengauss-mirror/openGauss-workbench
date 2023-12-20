/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TestActivityNumLowItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/collection/TestActivityNumLowItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.service.impl.collection.metric.ActivityNumLowItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * TestActivityNumItem2
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestActivityNumLowItem {
    @InjectMocks
    private ActivityNumLowItem item;

    @Test
    public void testGetPrometheusParam_hasThreshold() {
        DiagnosisThresholdDO threshold = new DiagnosisThresholdDO();
        threshold.setThreshold(ThresholdConstants.ACTIVITY_NUM);
        threshold.setThresholdValue("20");
        List<DiagnosisThresholdDO> thresholds = new ArrayList<>() {{
            add(threshold);
        }};
        String metric = item.getPrometheusParam(thresholds);
        assertNotEquals(metric, MetricConstants.ACTIVITY_NUM);
    }

    @Test
    public void testGetPrometheusParam_noThreshold() {
        String metric = item.getPrometheusParam(null);
        assertEquals(metric, MetricConstants.ACTIVITY_NUM);
    }
}
