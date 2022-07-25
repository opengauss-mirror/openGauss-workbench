package com.nctigba.datastudio.controller;


import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.service.DatabaseSequenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseSequenceController {

    @Resource
    DatabaseSequenceService databaseSequenceService;


    @ApiOperation(value = "CREATE SEQUENCE DDL")
    @PostMapping(value = "/sequences/action", params = "action=createSequenceDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSequenceDDL(@RequestBody DatabaseCreateSequenceDTO request) throws Exception {
        return databaseSequenceService.createSequenceDDL(request);
    }


    @ApiOperation(value = "CREATE SEQUENCE")
    @PostMapping(value = "/sequences", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSequence(@RequestBody DatabaseCreateSequenceDTO request) throws Exception {
        databaseSequenceService.createSequence(request);
    }


    @ApiOperation(value = "DROP SEQUENCE")
    @DeleteMapping(value = "/sequences", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropSequence(@RequestBody DatabaseDropSequenceDTO request) throws Exception {
        databaseSequenceService.dropSequence(request);
    }
    @ApiOperation(value = "RETURN SEQUENCE DDL")
    @PostMapping(value = "/sequenceDdls", produces = MediaType.APPLICATION_JSON_VALUE)
    public String returnSequenceDDL(@RequestBody DatabaseSequenceDdlDTO request) throws Exception {
        return databaseSequenceService.returnSequenceDDL(request);
    }
}
