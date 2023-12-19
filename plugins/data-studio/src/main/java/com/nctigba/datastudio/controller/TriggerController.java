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
 *  TriggerController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/TriggerController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.TriggerQuery;
import com.nctigba.datastudio.service.TriggerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TriggerController
 *
 * @since 2023-10-19
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class TriggerController {
    @Resource
    private TriggerService triggerService;

    /**
     * get trigger function list
     *
     * @param query query
     * @return List
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/queryFunction", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> queryFunction(@RequestBody TriggerQuery query) throws SQLException {
        return triggerService.queryFunction(query);
    }

    /**
     * create trigger function
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/createFunction", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createFunction(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.createFunction(query);
    }

    /**
     * create trigger
     *
     * @param query query
     * @return String
     */
    @PostMapping(value = "/trigger/ddlPreview", produces = MediaType.APPLICATION_JSON_VALUE)
    public String ddlPreview(@RequestBody TriggerQuery query) {
        return triggerService.ddlPreview(query);
    }

    /**
     * create trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.create(query);
    }

    /**
     * query trigger
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Map<String, Object>> query(@RequestBody TriggerQuery query) throws SQLException {
        return triggerService.query(query);
    }

    /**
     * rename trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public void rename(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.rename(query);
    }

    /**
     * edit trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public void edit(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.edit(query);
    }

    /**
     * delete trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.delete(query);
    }

    /**
     * enable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    public void enable(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.enable(query);
    }

    /**
     * disable trigger
     *
     * @param query query
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    public void disable(@RequestBody TriggerQuery query) throws SQLException {
        triggerService.disable(query);
    }

    /**
     * show trigger ddl
     *
     * @param query query
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/trigger/showDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> showDdl(@RequestBody TriggerQuery query) throws SQLException {
        return triggerService.showDdl(query);
    }
}
