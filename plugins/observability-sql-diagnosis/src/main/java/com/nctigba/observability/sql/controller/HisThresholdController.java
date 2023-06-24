/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;
import com.nctigba.observability.sql.service.history.HisThresholdService;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/historyDiagnosis/api/v1/thresholds")
@RequiredArgsConstructor
public class HisThresholdController {
    private final HisThresholdService hisThresholdService;
    private final LocaleString localeToString;

    @GetMapping("")
    public AppResult select() {
        return AppResult.ok("success").addData(localeToString.trapLanguage(hisThresholdService.select()));
    }

    @PostMapping("")
    public AppResult insertOrUpdate(@RequestBody HisThresholdQuery hisThresholdQuery) {
        hisThresholdService.insertOrUpdate(hisThresholdQuery);
        return AppResult.ok("success");
    }

    @DeleteMapping("/{thresholdId}")
    public AppResult delete(@PathVariable int thresholdId) {
        hisThresholdService.delete(thresholdId);
        return AppResult.ok("success");
    }
}
