/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;

import java.sql.SQLException;

/**
 * DatabaseSequenceService
 *
 * @since 2023-6-26
 */
public interface DatabaseSequenceService {
    /**
     * create sequence dll
     *
     * @param request request
     * @return String
     */
    String createSequenceDDL(DatabaseCreateSequenceDTO request);

    /**
     * create sequence
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void createSequence(DatabaseCreateSequenceDTO request) throws SQLException;

    /**
     * drop sequence
     *
     * @param request request
     * @throws SQLException SQLException
     */
    void dropSequence(DatabaseDropSequenceDTO request) throws SQLException;

    /**
     * return sequence dll
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws SQLException;
}
