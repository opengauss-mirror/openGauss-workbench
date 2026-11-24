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

package com.nctigba.observability.instance.supplier;

import com.gitee.starblues.annotation.Supplier;
import com.nctigba.observability.instance.service.DiagnosisService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TestSupplier
 *
 * @author luomeng
 * @since 2024/4/19
 */
@Supplier("instance-sql-supplier")
public class DiagnosisSupplier {
    @Autowired
    private DiagnosisService diagnosisService;

    /**
     * getInstanceMetric
     *
     * @param nodeId nodeId
     * @return AjaxResult
     */
    public AjaxResult getInstanceMetric(String nodeId) {
        return diagnosisService.getMetricData(nodeId);
    }
}
