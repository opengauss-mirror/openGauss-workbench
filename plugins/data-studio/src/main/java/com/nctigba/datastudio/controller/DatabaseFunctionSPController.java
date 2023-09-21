/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;


import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageRequest;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * DatabaseFunctionSPController
 *
 * @since 2023-6-26
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseFunctionSPController {

    @Resource
    private DatabaseFunctionSPService databaseFunctionSPService;

    /**
     * drop function
     *
     * @param request request
     */
    @DeleteMapping(value = "/functionSP", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropFunctionSP(@RequestBody DatabaseFunctionSPDTO request) {
        databaseFunctionSPService.dropFunctionSP(request);
    }

    /**
     * drop package
     *
     * @param request request
     */
    @DeleteMapping(value = "/drop/package", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropPackage(@RequestBody PackageRequest request) {
        databaseFunctionSPService.dropPackage(request);
    }
}
