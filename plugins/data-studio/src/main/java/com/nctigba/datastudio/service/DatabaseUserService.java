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
 *  DatabaseUserService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/DatabaseUserService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseReturnUserDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseUserInfoDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.model.dto.UpdateUserAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateUserPasswordDTO;

import java.sql.SQLException;

/**
 * DatabaseUserService
 *
 * @since 2023-6-26
 */
public interface DatabaseUserService {
    /**
     * create user preview ddl
     *
     * @param request request
     * @return String
     */
    String createUserPreviewDDL(DatabaseCreateUserDTO request);

    /**
     * user preview ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    String userPreviewDDL(DatabaseReturnUserDdlDTO request) throws SQLException;

    /**
     * user info
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    DatabaseUserInfoDTO userInfo(DatabaseReturnUserDdlDTO request) throws SQLException;

    /**
     * create user ddl
     *
     * @param request request
     */
    void createUserDDL(DatabaseCreateUserDTO request);

    /**
     * drop user ddl
     *
     * @param request request
     */
    void dropUserDDL(DatabaseUsserCheckDTO request);

    /**
     * update user password
     *
     * @param request request
     */
    void updateUserPassword(UpdateUserPasswordDTO request);

    /**
     * update user attribute
     *
     * @param request request
     */
    void userUpdateAttribute(UpdateUserAttributeDTO request);

    /**
     * update user attribute ddl
     *
     * @param request request
     */
    String userUpdateAttributeDdl(UpdateUserAttributeDTO request);
}
