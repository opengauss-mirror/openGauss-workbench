/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.service.history.HisDiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HisDiagnosisController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api")
@RequiredArgsConstructor
public class HisDiagnosisController {
    private final HisDiagnosisService hisDiagnosisService;
    private final LocaleString localeToString;
    private final DiagnosisResourceMapper resourceMapper;

    @GetMapping("/v2/tasks/{taskId}/points/{pointName}")
    public AjaxResult getNodeDetail(@PathVariable int taskId, @PathVariable("pointName") String pointName,
            String diagnosisType) {
        var nodeDetail = hisDiagnosisService.getNodeDetail(taskId, pointName, diagnosisType);
        return AjaxResult.success(nodeDetail);
    }

    @GetMapping("/v2/tasks/{taskId}/suggestPoints/{type}")
    public AjaxResult getTopologyMap(@PathVariable int taskId, @RequestParam(defaultValue = "true") boolean isAll,
            String diagnosisType) {
        var topologyMap = localeToString.trapLanguage(hisDiagnosisService.getTopologyMap(taskId, isAll, diagnosisType));
        return AjaxResult.success(topologyMap);
    }

    /**
     * Query onCpu/offCpu svg
     *
     * @param id resource id
     * @param type diagnosis type
     * @param resp http response
     */
    @GetMapping(value = "/v1/res/{id}.{type}")
    public void res(@PathVariable String id, @PathVariable String type,
            HttpServletResponse resp) {
        switch (type) {
            case "svg":
                resp.setContentType("image/svg+xml");
                break;
            case "png":
                resp.setContentType("image/png");
                break;
            default:
                log.error("fail data:{}", type);
                break;
        }
        try {
            resourceMapper.selectById(id).to(resp.getOutputStream());
        } catch (IOException e) {
            log.error("fail read data:{}", type);
        }
    }
}
