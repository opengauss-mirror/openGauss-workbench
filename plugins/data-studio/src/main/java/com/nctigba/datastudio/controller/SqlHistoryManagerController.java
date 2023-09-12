/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.service.SqlHistoryManagerService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SqlHistoryManagerController
 *
 * @since 2023-7-17
 */
@Api(tags = {"Schema manager interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class SqlHistoryManagerController {
    @Autowired
    private SqlHistoryManagerService sqlHistoryManagerService;

    /**
     * insert sql history
     *
     * @param sqlHistoryDOList sqlHistoryDOList
     */
    @PostMapping(value = "/sqlHistory/insert", produces = MediaType.APPLICATION_JSON_VALUE)
    public void insertHistory(@RequestBody List<SqlHistoryDO> sqlHistoryDOList) {
        sqlHistoryManagerService.insertHistory(sqlHistoryDOList);
    }

    /**
     * query sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     * @return List
     */
    @PostMapping(value = "/sqlHistory/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SqlHistoryDO> queryHistory(@RequestBody SqlHistoryDO sqlHistoryDO) {
        return sqlHistoryManagerService.queryHistory(sqlHistoryDO);
    }

    /**
     * update sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    @PostMapping(value = "/sqlHistory/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateHistory(@RequestBody SqlHistoryDO sqlHistoryDO) {
        sqlHistoryManagerService.updateHistory(sqlHistoryDO);
    }

    /**
     * delete sql history
     *
     * @param sqlHistoryDO sqlHistoryDO
     */
    @PostMapping(value = "/sqlHistory/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteHistory(@RequestBody SqlHistoryDO sqlHistoryDO) {
        sqlHistoryManagerService.deleteHistory(sqlHistoryDO);
    }
}
