/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

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
     * @param request request
     * @param passwordType passwordType
     * @return String
     */
    default String createUserPreviewDDL(DatabaseCreateUserDTO request, String passwordType) {
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
}
