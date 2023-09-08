/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.metric;

import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CpuCoreNumItem
 *
 * @author luomeng
 * @since 2023/8/22
 */
@Service
public class CpuCoreNumItem extends PrometheusCollectionItem {
    @Override
    public String getPrometheusParam(List<HisDiagnosisThreshold> thresholds) {
        return MetricCommon.CPU_CORE_NUM;
    }
}
