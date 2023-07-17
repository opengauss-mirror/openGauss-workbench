/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;

import java.sql.SQLException;
import java.util.Map;

/**
 * DatabaseSynonymService
 *
 * @since 2023-6-26
 */
public interface DatabaseSynonymService {
    /**
     * create synonym ddl
     *
     * @param request request
     * @return String
     */
    String createSynonymDDL(DatabaseCreateSynonymDTO request);

    /**
     * create synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void createSynonym(DatabaseCreateSynonymDTO request) throws SQLException;

    /**
     * synonym attribute
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) throws SQLException;

    /**
     * drop synonym
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void dropSynonym(DatabaseDropSynonymDTO request) throws SQLException;
}
