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
 *  PrivilegeController.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/controller/PrivilegeController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.dto.PrivilegeHistoryDTO;
import com.nctigba.datastudio.model.query.PrivilegeHistoryQuery;
import com.nctigba.datastudio.model.query.PrivilegeSetQuery;
import com.nctigba.datastudio.service.PrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

/**
 * PrivilegeController
 *
 * @author liupengfei
 * @since 2024/10/25
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1/privilege")
public class PrivilegeController {
    @Autowired
    private PrivilegeService privilegeService;

    /**
     * getPrivilegeSql
     *
     * @param request PrivilegeSetQuery
     * @return privilege sql
     */
    @PostMapping(value = "/sql", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPrivilegeSql(@RequestBody PrivilegeSetQuery request) {
        return privilegeService.getPrivilegeSql(request);
    }

    /**
     * setPrivilege
     *
     * @param request PrivilegeSetQuery
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/set", produces = MediaType.APPLICATION_JSON_VALUE)
    public void setPrivilege(@RequestBody PrivilegeSetQuery request) throws SQLException {
        privilegeService.setPrivilege(request);
    }

    /**
     * getPrivilegeHistory
     *
     * @param request PrivilegeHistoryQuery
     * @return history
     */
    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public PrivilegeHistoryDTO getPrivilegeHistory(PrivilegeHistoryQuery request) {
        return privilegeService.getPrivilegeHistory(request);
    }
}
