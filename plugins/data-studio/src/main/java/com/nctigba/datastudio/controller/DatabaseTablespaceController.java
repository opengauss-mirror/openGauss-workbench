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
 *  DatabaseTablespaceController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseTablespaceController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDorpTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.RequestTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateTablespaceAttributeDTO;
import com.nctigba.datastudio.service.DatabaseTablespaceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;

/**
 * DatabaseUserController
 *
 * @since 2023-8-9
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class DatabaseTablespaceController {
    @Resource
    private DatabaseTablespaceService databaseTablespaceService;

    /**
     * create tablespace ddl
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/tablespace/create/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createUserPreviewDDL(@RequestBody DatabaseCreateTablespaceDTO request) {
        return databaseTablespaceService.createTablespaceDDL(request);
    }

    /**
     * create tablespace
     *
     * @param request request
     */
    @PostMapping(value = "/tablespace/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createUserDDL(@RequestBody DatabaseCreateTablespaceDTO request) {
        databaseTablespaceService.createTablespace(request);
    }

    /**
     * drop tablespace
     *
     * @param request request
     */
    @DeleteMapping(value = "/tablespace/drop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropTablespaceDDL(@RequestBody DatabaseDorpTablespaceDTO request) {
        databaseTablespaceService.dropTablespaceDDL(request);
    }

    /**
     * tablespace attribute
     *
     * @param request request
     * @return DatabaseTablespaceAttributeDTO
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/tablespace/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseTablespaceAttributeDTO tablespaceAttribute(
            RequestTablespaceAttributeDTO request) throws SQLException {
        return databaseTablespaceService.tablespaceAttribute(request);
    }

    /**
     * update tablespace
     *
     * @param request request
     */
    @PostMapping(value = "/tablespace/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void tablespaceUpdate(@RequestBody UpdateTablespaceAttributeDTO request) {
        databaseTablespaceService.tablespaceUpdate(request);
    }

    /**
     * update tablespace ddl
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/tablespace/update/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String tablespaceUpdateAttributeDdl(@RequestBody UpdateTablespaceAttributeDTO request) {
        return databaseTablespaceService.tablespaceUpdateAttributeDdl(request);
    }
}
