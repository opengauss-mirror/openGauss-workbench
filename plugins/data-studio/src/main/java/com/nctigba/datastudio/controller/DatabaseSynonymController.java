package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.service.DatabaseSynonymService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseSynonymController {
    @Resource
    DatabaseSynonymService databaseSynonymService;


    @ApiOperation(value = "CREATE SYNONYM DDL")
    @PostMapping(value = "/synonyms/action", params = "action=createSynonymDdl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createSequenceDDL(@RequestBody DatabaseCreateSynonymDTO request) throws Exception {
        return databaseSynonymService.createSynonymDDL(request);
    }


    @ApiOperation(value = "CREATE SYNONYM")
    @PostMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createSequence(@RequestBody DatabaseCreateSynonymDTO request) throws Exception {
        databaseSynonymService.createSynonym(request);
    }

    @ApiOperation(value = "CREATE SYNONYM")
    @GetMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> sequenceAttribute(DatabaseSynonymAttributeDTO request) throws Exception {
        return databaseSynonymService.synonymAttribute(request);
    }

    @ApiOperation(value = "DROP SYNONYM")
    @DeleteMapping(value = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropSequence(@RequestBody DatabaseDropSynonymDTO request) throws Exception {
        databaseSynonymService.dropSynonym(request);
    }
}
