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
 *  DiagnosisTaskController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/DiagnosisTaskController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * DiagnosisTaskController
 *
 * @author luomeng
 * @since 2023/6/11
 */
@Slf4j
@RestController
@RequestMapping("/historyDiagnosis/api")
@RequiredArgsConstructor
public class DiagnosisTaskController {
    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(
                Date.class,
                new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"), true));
    }

    private final TaskService taskService;
    private final LocaleStringUtils localeToString;

    @PostMapping("/v2/tasks")
    public AjaxResult start(@RequestBody DiagnosisTaskDTO diagnosisTaskDTO) {
        int taskId = taskService.add(diagnosisTaskDTO);
        if (DiagnosisTypeConstants.HISTORY.equals(diagnosisTaskDTO.getDiagnosisType())) {
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
        if (query.getStartTime() != null && query.getEndTime() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            format.setTimeZone(TimeZone.getDefault());
            try {
                String startDate = format.format(query.getStartTime());
                String endDate = format.format(query.getEndTime());
                query.setStartTime(dateFormat.parse(startDate));
                query.setEndTime(dateFormat.parse(endDate));
            } catch (ParseException e) {
                throw new HisDiagnosisException("Date parse error!");
            }
        }
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
}