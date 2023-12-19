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
 *  DatabaseFunctionSPController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseFunctionSPController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;


import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageQuery;
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
    public void dropPackage(@RequestBody PackageQuery request) {
        databaseFunctionSPService.dropPackage(request);
    }
}
