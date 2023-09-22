/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
    @PostMapping(value = "/sequences/action", params = "action=createSequenceDdl", produces = MediaType.APPLICATION_JSON_VALUE)
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
