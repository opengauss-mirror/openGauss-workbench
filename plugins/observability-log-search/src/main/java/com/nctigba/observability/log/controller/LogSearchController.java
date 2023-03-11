package com.nctigba.observability.log.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.log.model.dto.ContextSearchInfoDTO;
import com.nctigba.observability.log.model.dto.LogDistroMapDTO;
import com.nctigba.observability.log.model.dto.LogInfoDTO;
import com.nctigba.observability.log.model.dto.LogTypeTreeDTO;
import com.nctigba.observability.log.model.query.ContextSearchQuery;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import com.nctigba.observability.log.model.query.LogDistroMapQuery;
import com.nctigba.observability.log.model.query.LogSearchQuery;
import com.nctigba.observability.log.service.LogSearchService;


/**
 * Log-Search
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/11/17 09:00
 */
@RestController
@RequestMapping("/logSearch/api/v1")
public class LogSearchController {

    @Autowired
    private LogSearchService logSearchService;

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), true));
    }

    @GetMapping(value = "/logDistributionMap")
    public List<LogDistroMapDTO> logDistroMap(LogDistroMapQuery queryParam) throws Exception {
        EsSearchQuery esSearchQuery = new EsSearchQuery();
        if (queryParam != null) {
            esSearchQuery.setNodeId(queryParam.getNodeId());
            esSearchQuery.setStartDate(queryParam.getStartDate());
            esSearchQuery.setEndDate(queryParam.getEndDate());
            esSearchQuery.setSearchPhrase(queryParam.getSearchPhrase());
            esSearchQuery.setLogLevel(queryParam.getLogLevel());
            esSearchQuery.setLogType(queryParam.getLogType());
        }
        return logSearchService.getLogDistroMap(esSearchQuery);
    }

    @GetMapping(value = "/logs")
    public LogInfoDTO logInfo(@Valid LogSearchQuery queryParam) throws Exception {
        EsSearchQuery esSearchQuery = new EsSearchQuery();
        if (queryParam != null) {
            esSearchQuery.setNodeId(queryParam.getNodeId());
            esSearchQuery.setStartDate(queryParam.getStartDate());
            esSearchQuery.setEndDate(queryParam.getEndDate());
            esSearchQuery.setSearchPhrase(queryParam.getSearchPhrase());
            esSearchQuery.setLogLevel(queryParam.getLogLevel());
            esSearchQuery.setLogType(queryParam.getLogType());
            esSearchQuery.setScrollId(queryParam.getScrollId());
            esSearchQuery.setRowCount(queryParam.getRowCount());
        }
        return logSearchService.getLogByQuery(esSearchQuery);
    }

    @GetMapping(value = "/logTypes")
    public List<LogTypeTreeDTO> logTypeInfo() throws Exception {
        return logSearchService.getLogType();
    }

    @GetMapping(value = "/logLevels")
    public List<String> logLevelInfo() throws Exception {
        return logSearchService.getLogLevel();
    }

    @GetMapping(value = "/logContextSearch")
    public ContextSearchInfoDTO logContextSearch(ContextSearchQuery queryParam) throws Exception {
        return logSearchService.getContextSearch(queryParam);
    }

}
