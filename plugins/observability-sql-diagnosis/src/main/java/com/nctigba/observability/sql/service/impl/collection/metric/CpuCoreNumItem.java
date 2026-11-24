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
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
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
    public String getPrometheusParam(List<DiagnosisThresholdDO> thresholds) {
        return MetricConstants.CPU_CORE_NUM;
    }
}
