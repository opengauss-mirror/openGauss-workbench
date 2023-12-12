/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseReturnUserDdlDTO;
import com.nctigba.datastudio.model.dto.DatabaseUserInfoDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.model.dto.UpdateUserAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateUserPasswordDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;

/**
 * UserObjectSQLService
 *
 * @since 2023-06-25
 */
public interface UserObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * create user preview ddl
     *
     * @param request      request
     * @param passwordType passwordType
     * @return String
     */
    default String createUserPreviewDDL(DatabaseCreateUserDTO request, String passwordType) {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * create user preview ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String userPreviewDDL(DatabaseReturnUserDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create user preview ddl
     *
     * @param request request
     * @return DatabaseUserInfoDTO
     * @throws SQLException SQLException
     */
    default DatabaseUserInfoDTO userInfo(DatabaseReturnUserDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop user preview ddl
     *
     * @param request request
     * @return String
     */
    default String dropUserDDL(DatabaseUsserCheckDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update user password
     *
     * @param request request
     * @return String
     */
    default String updateUserPassword(UpdateUserPasswordDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update user attribute
     *
     * @param request request
     * @return String
     */
    default String userUpdateAttribute(UpdateUserAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
