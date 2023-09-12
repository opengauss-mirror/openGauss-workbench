/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.query.TaskQuery;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.util.LocaleString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/historyDiagnosis/api")
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

    @PostMapping("/v2/tasks")
    public AjaxResult start(@RequestBody HisDiagnosisTaskDTO hisDiagnosisTaskDTO) {
        int taskId = taskService.add(hisDiagnosisTaskDTO);
        if (DiagnosisTypeCommon.HISTORY.equals(hisDiagnosisTaskDTO.getDiagnosisType())) {
            taskService.start(taskId);
        }
        return AjaxResult.success(taskId);
    }

    @GetMapping("/v2/options")
    public AjaxResult option(String diagnosisType) {
        var options = localeToString.trapLanguage(taskService.getOption(diagnosisType));
        return AjaxResult.success(options);
    }

    /**
     * Query all tasks by page
     *
     * @param query task query info
     * @return AjaxResult
     */
    @GetMapping("/v1/pageQuery")
    public AjaxResult selectByPage(TaskQuery query) {
        var result = localeToString.trapLanguage(taskService.selectByPage(query), ObjectNode.class);
        return AjaxResult.success(result);
    }

    /**
     * Delete task by id
     *
     * @param taskId task idd
     * @return AjaxResult
     */
    @DeleteMapping("/v1/tasks/{taskId}")
    public AjaxResult delete(@PathVariable int taskId) {
        taskService.delete(taskId);
        return AjaxResult.success();
    }

    /**
     * Query task by id
     *
     * @param taskId task id
     * @return AjaxResult
     */
    @GetMapping("/v1/tasks/{taskId}")
    public AjaxResult selectById(@PathVariable int taskId) {
        var result = localeToString.trapLanguage(taskService.selectById(taskId), ObjectNode.class);
        return AjaxResult.success(result);
    }

    /**
     * Query sql plan
     *
     * @param nodeId node id
     * @param sqlId sql id
     * @return AjaxResult
     */
    @GetMapping(value = "/v1/plan/{nodeId}/{sqlId}")
    public AjaxResult plan(@PathVariable String nodeId, @PathVariable String sqlId) {
        var result = taskService.plan(nodeId, sqlId);
        return AjaxResult.success(result);
    }
}
