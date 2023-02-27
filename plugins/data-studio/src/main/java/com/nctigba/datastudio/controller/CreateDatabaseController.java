package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.CreateDatabaseDTO;
import com.nctigba.datastudio.model.dto.DatabaseNameDTO;
import com.nctigba.datastudio.model.dto.RenameDatabaseDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.service.CreateDatabaseService;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class CreateDatabaseController {

    @Resource
    private CreateDatabaseService creaeteDatabaseService;


    @PostMapping(value = "/database/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createDatabase(@RequestBody CreateDatabaseDTO database) throws Exception {
        creaeteDatabaseService.createDatabase(database);
    }

    @PostMapping(value = "/database/connection", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseConnectionDO connectionDatabase(@RequestBody DatabaseConnectionDO database) throws Exception {
        return creaeteDatabaseService.connectionDatabase(database);
    }

    @DeleteMapping(value = "/database/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteDatabase(@RequestBody DatabaseNameDTO database) throws Exception {
        creaeteDatabaseService.deleteDatabase(database);
    }

    @PostMapping(value = "/database/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public void renameDatabase(@RequestBody RenameDatabaseDTO database) throws Exception {
        creaeteDatabaseService.renameDatabase(database);
    }

    @GetMapping(value = "/database/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> databaseAttribute(DatabaseNameDTO request) throws Exception {
        return creaeteDatabaseService.databaseAttribute(request);
    }


}
