package com.nctigba.datastudio.controller;


import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = {"Metadata query interface"})
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseFunctionSPController {

    @Resource
    DatabaseFunctionSPService databaseFunctionSPService;

    @ApiOperation(value = "DROP FunctionSP")
    @DeleteMapping(value = "/functionSP", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropFunctionSP(@RequestBody DatabaseFunctionSPDTO request) throws Exception {
        databaseFunctionSPService.dropFunctionSP(request);
    }
}
