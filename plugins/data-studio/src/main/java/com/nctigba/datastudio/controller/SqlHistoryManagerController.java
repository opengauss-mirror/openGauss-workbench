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
 *  SqlHistoryManagerController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/SqlHistoryManagerController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.service.SqlHistoryManagerService;
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
