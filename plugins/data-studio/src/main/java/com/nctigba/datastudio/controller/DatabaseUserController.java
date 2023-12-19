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
 *  DatabaseUserController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/DatabaseUserController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseReturnUserDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseUserInfoDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.model.dto.UpdateUserAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateUserPasswordDTO;
import com.nctigba.datastudio.service.DatabaseUserService;
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
public class DatabaseUserController {
    @Resource
    private DatabaseUserService databaseUserService;

    /**
     * create user
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/user/create/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public String createUserPreviewDDL(@RequestBody DatabaseCreateUserDTO request) {
        return databaseUserService.createUserPreviewDDL(request);
    }

    /**
     * create user
     *
     * @param request request
     */
    @PostMapping(value = "/user/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void createUserDDL(@RequestBody DatabaseCreateUserDTO request) {
        databaseUserService.createUserDDL(request);
    }

    /**
     * drop user
     *
     * @param request request
     */
    @DeleteMapping(value = "/user/drop", produces = MediaType.APPLICATION_JSON_VALUE)
    public void dropUserDDL(@RequestBody DatabaseUsserCheckDTO request) {
        databaseUserService.dropUserDDL(request);
    }

    /**
     * user ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/user/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String userReturnDDL(DatabaseReturnUserDdlDTO request) throws SQLException {
        return databaseUserService.userPreviewDDL(request);
    }

    /**
     * user attribute
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    @GetMapping(value = "/user/attribute", produces = MediaType.APPLICATION_JSON_VALUE)
    public DatabaseUserInfoDTO userAttribute(DatabaseReturnUserDdlDTO request) throws SQLException {
        return databaseUserService.userInfo(request);
    }

    /**
     * create user
     *
     * @param request request
     */
    @PostMapping(value = "/user/password/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void userUpdatePassword(@RequestBody UpdateUserPasswordDTO request) {
        databaseUserService.updateUserPassword(request);
    }

    /**
     * create user
     *
     * @param request request
     */
    @PostMapping(value = "/user/attribute/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public void userUpdateAttribute(@RequestBody UpdateUserAttributeDTO request) {
        databaseUserService.userUpdateAttribute(request);
    }

    /**
     * create user
     *
     * @param request request
     * @return String
     */
    @PostMapping(value = "/user/attribute/update/ddl", produces = MediaType.APPLICATION_JSON_VALUE)
    public String userUpdateAttributeDdl(@RequestBody UpdateUserAttributeDTO request) {
        return databaseUserService.userUpdateAttributeDdl(request);
    }
}
