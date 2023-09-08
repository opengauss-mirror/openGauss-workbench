/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateUserDTO;
import com.nctigba.datastudio.model.dto.DatabaseUsserCheckDTO;

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
}
