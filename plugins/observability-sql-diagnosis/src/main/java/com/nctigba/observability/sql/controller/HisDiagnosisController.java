/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.sql.service.history.HisDiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HisDiagnosisController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api/v1/tasks")
@RequiredArgsConstructor
public class HisDiagnosisController {
    private final HisDiagnosisService hisDiagnosisService;
    private final LocaleString localeToString;

    @GetMapping("/{taskId}/points/{nodeName}")
    public AppResult getNodeDetail(@PathVariable int taskId, @PathVariable("nodeName") String nodeName) {
        return AppResult.ok("success").addData(hisDiagnosisService.getNodeDetail(taskId, nodeName));
    }

    @GetMapping("/{taskId}/point/all")
    public AppResult getAllPoint(@PathVariable int taskId) {
        return AppResult.ok("success").addData(localeToString.trapLanguage(hisDiagnosisService.getAllPoint(taskId)));
    }

    @GetMapping("/{taskId}/suggestPoints/{type}")
    public AppResult getTopologyMap(@PathVariable int taskId, @RequestParam(defaultValue = "true") boolean isAll) {
        return AppResult.ok("success").addData(
                localeToString.trapLanguage(hisDiagnosisService.getTopologyMap(taskId, isAll)));
    }
}
