/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.PrivilegeHistoryDTO;
import com.nctigba.datastudio.model.query.PrivilegeHistoryQuery;
import com.nctigba.datastudio.model.query.PrivilegeSetQuery;

import java.sql.SQLException;

/**
 * PrivilegeService
 *
 * @author liupengfei
 * @since 2024/10/28
 */
public interface PrivilegeService {
    /**
     * getPrivilegeSql
     *
     * @param request PrivilegeSetQuery
     * @return sql
     */
    String getPrivilegeSql(PrivilegeSetQuery request);

    /**
     * setPrivilege
     *
     * @param request PrivilegeSetQuery
     * @throws SQLException SQLException
     */
    void setPrivilege(PrivilegeSetQuery request) throws SQLException;

    /**
     * getPrivilegeHistory
     *
     * @param request PrivilegeHistoryQuery
     * @return PrivilegeHistoryDTO
     */
    PrivilegeHistoryDTO getPrivilegeHistory(PrivilegeHistoryQuery request);
}
