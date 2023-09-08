/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;
import com.nctigba.observability.sql.service.history.HisThresholdService;
import com.nctigba.observability.sql.util.LocaleString;
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
 * HisThresholdController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api")
@RequiredArgsConstructor
public class HisThresholdController {
    private final HisThresholdService hisThresholdService;
    private final LocaleString localeToString;

    @GetMapping("/v2/thresholds")
    public AjaxResult select(String diagnosisType) {
        var thresholds = localeToString.trapLanguage(hisThresholdService.select(diagnosisType));
        return AjaxResult.success(thresholds);
    }

    @PostMapping("/v1")
    public AjaxResult insertOrUpdate(@RequestBody HisThresholdQuery hisThresholdQuery) {
        hisThresholdService.insertOrUpdate(hisThresholdQuery);
        return AjaxResult.success("success");
    }

    @DeleteMapping("/v1/{thresholdId}")
    public AjaxResult delete(@PathVariable int thresholdId) {
        hisThresholdService.delete(thresholdId);
        return AjaxResult.success("success");
    }
}
