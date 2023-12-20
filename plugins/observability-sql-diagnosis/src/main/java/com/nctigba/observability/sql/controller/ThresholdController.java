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
 *  ThresholdController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/ThresholdController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.model.query.ThresholdQuery;
import com.nctigba.observability.sql.service.ThresholdService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ThresholdController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api")
@RequiredArgsConstructor
public class ThresholdController {
    private final ThresholdService thresholdService;
    private final LocaleStringUtils localeToString;

    @GetMapping("/v2/thresholds")
    public AjaxResult select(String diagnosisType) {
        var thresholds = localeToString.trapLanguage(thresholdService.select(diagnosisType));
        return AjaxResult.success(thresholds);
    }

    @PostMapping("/v1")
    public AjaxResult insertOrUpdate(@RequestBody ThresholdQuery thresholdQuery) {
        thresholdService.insertOrUpdate(thresholdQuery);
        return AjaxResult.success("success");
    }

    @DeleteMapping("/v1/{thresholdId}")
    public AjaxResult delete(@PathVariable int thresholdId) {
        thresholdService.delete(thresholdId);
        return AjaxResult.success("success");
    }
}
