/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
