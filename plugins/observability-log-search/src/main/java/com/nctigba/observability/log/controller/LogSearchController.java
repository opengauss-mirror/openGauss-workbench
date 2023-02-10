package com.nctigba.observability.log.controller;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.nctigba.observability.log.model.dto.LogDistroMapDTO;
import com.nctigba.observability.log.model.dto.LogInfoDTO;
import com.nctigba.observability.log.model.query.EsSearchQuery;
import com.nctigba.observability.log.model.dto.LogTypeTreeDTO;
import com.nctigba.observability.log.model.query.LogDistroMapQuery;
import com.nctigba.observability.log.model.query.LogSearchQuery;
import com.nctigba.observability.log.service.LogSearchService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;


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
        webDataBinder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"), true));
    }

    @ApiOperation("Log Distribution Map")
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

    @ApiOperation("Log Info")
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

    @ApiOperation("LogType Info")
    @GetMapping(value = "/logTypes")
    public List<LogTypeTreeDTO> logTypeInfo() throws Exception {
        return logSearchService.getLogType();
    }

    @ApiOperation("LogLevel Info")
    @GetMapping(value = "/logLevels")
    public List<String> logLevelInfo() throws Exception {
        return logSearchService.getLogLevel();
    }

}
