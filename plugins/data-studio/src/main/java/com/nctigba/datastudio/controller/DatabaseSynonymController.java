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
 *  DatabaseSynonymController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseSynonymController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

/**
 * DatabaseSynonymController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseSynonymController {
    @Resource
    private DatabaseSynonymService databaseSynonymService;

    /**
     * create
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/synonyms/action", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSynonymDDL(@RequestBody DatabaseCreateSynonymDTO request) {
        return databaseSynonymService.createSynonymDDL(request);
    }

    /**
     * create synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSynonym(@RequestBody DatabaseCreateSynonymDTO request) throws SQLException {
        databaseSynonymService.createSynonym(request);
    }

    /**
     * synonym attribute
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) throws SQLException {
        return databaseSynonymService.synonymAttribute(request);
    }

    /**
     * drop synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @DeleteMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropSynonym(@RequestBody DatabaseDropSynonymDTO request) throws SQLException {
        databaseSynonymService.dropSynonym(request);
    }
}
