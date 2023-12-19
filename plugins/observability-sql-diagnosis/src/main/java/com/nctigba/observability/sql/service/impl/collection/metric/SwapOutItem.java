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
 *  SwapOutItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/metric/SwapOutItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.metric;

import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * SwapOutItem
 *
 * @author luomeng
 * @since 2023/11/12
 */
@Service
public class SwapOutItem extends PrometheusCollectionItem {
    @Override
    public String getPrometheusParam(List<DiagnosisThresholdDO> thresholds) {
        if (CollectionUtils.isEmpty(thresholds)) {
            return MetricConstants.SWAP_OUT;
        } else {
            StringBuilder sb = new StringBuilder(MetricConstants.SWAP_OUT);
            for (DiagnosisThresholdDO threshold : thresholds) {
                if (ThresholdConstants.SWAP_NUM.equals(threshold.getThreshold())) {
                    sb.append(">");
                    sb.append(threshold.getThresholdValue());
                }
            }
            return sb.toString();
        }
    }
}