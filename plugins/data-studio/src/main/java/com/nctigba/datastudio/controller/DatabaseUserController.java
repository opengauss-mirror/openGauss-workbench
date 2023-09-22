/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.service.DatabaseUserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * DatabaseUserController
 *
 * @since 2023-8-9
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1/user")
public class DatabaseUserController {
    @Resource
    private DatabaseUserService databaseUserService;

    /**
     * create user
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/creat/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createUserPreviewDDL(@RequestBody DatabaseCreateUserDTO request) {
        return databaseUserService.createUserPreviewDDL(request);
    }

    /**
     * create user
     *
     * @param request request
     */
    @PostMapping(value = "/creat", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createUserDDL(@RequestBody DatabaseCreateUserDTO request) {
        databaseUserService.createUserDDL(request);
    }

    /**
     * drop user
     *
     * @param request request
     */
    @DeleteMapping(value = "/drop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropUserDDL(@RequestBody DatabaseUsserCheckDTO request) {
        databaseUserService.dropUserDDL(request);
    }
}
