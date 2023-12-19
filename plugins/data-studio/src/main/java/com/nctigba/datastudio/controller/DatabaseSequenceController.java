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
 *  DatabaseSequenceController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseSequenceController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;


import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * DatabaseSequenceController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseSequenceController {

    @Resource
    private DatabaseSequenceService databaseSequenceService;

    /**
     * create sequence ddl
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/sequences/action", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSequenceDDL(@RequestBody DatabaseCreateSequenceDTO request) {
        return databaseSequenceService.createSequenceDDL(request);
    }

    /**
     * create sequence
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/sequences", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSequence(@RequestBody DatabaseCreateSequenceDTO request) throws SQLException {
        databaseSequenceService.createSequence(request);
    }

    /**
     * drop sequence
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @DeleteMapping(value = "/sequences", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropSequence(@RequestBody DatabaseDropSequenceDTO request) throws SQLException {
        databaseSequenceService.dropSequence(request);
    }

    /**
     * return sequence ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/sequenceDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public String returnSequenceDDL(@RequestBody DatabaseSequenceDdlDTO request) throws SQLException {
        return databaseSequenceService.returnSequenceDDL(request);
    }
}
