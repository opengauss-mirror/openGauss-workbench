/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/dataStudio/web/v1/functionality")
public class FunctionalityConrroller {
    @GetMapping(value = "/heartbeat", produces = MediaType.APPLICATION_JSON_VALUE)
    public String heartbeat() {
        return "";
    }
}
