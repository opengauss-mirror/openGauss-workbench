/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * HisDiagnosisTaskController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api/v1")
@RequiredArgsConstructor
public class HisDiagnosisTaskController {
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(
                Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), true));
    }

    private final TaskService taskService;
    private final LocaleString localeToString;

    @PostMapping("/tasks")
    public AjaxResult start(@RequestBody HisDiagnosisTaskDTO hisDiagnosisTaskDTO) {
        int taskId = taskService.add(hisDiagnosisTaskDTO);
        taskService.start(taskId, hisDiagnosisTaskDTO);
        return AjaxResult.success(taskId);
    }

    @GetMapping("/options")
    public AjaxResult option() {
        var options = localeToString.trapLanguage(taskService.getOption());
        return AjaxResult.success(options);
    }
}
