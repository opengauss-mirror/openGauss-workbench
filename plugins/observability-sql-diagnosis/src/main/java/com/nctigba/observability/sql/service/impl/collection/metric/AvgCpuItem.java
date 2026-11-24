/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.service.impl.collection.metric;

import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AvgCpuItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class AvgCpuItem extends PrometheusCollectionItem {
    @Override
    public String getPrometheusParam(List<DiagnosisThresholdDO> thresholds) {
        if (CollectionUtils.isEmpty(thresholds)) {
            return MetricConstants.AVG_CPU_USAGE_RATE;
        } else {
            StringBuilder sb = new StringBuilder(MetricConstants.AVG_CPU_USAGE_RATE);
            for (DiagnosisThresholdDO threshold : thresholds) {
                if (ThresholdConstants.CPU_USAGE_RATE.equals(threshold.getThreshold())) {
                    sb.append(">=");
                    sb.append(threshold.getThresholdValue());
                }
            }
            return sb.toString();
        }
    }
}
