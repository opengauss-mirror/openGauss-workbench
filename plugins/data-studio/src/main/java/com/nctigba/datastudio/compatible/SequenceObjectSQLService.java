/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

import java.sql.SQLException;

/**
 * SequenceObjectSQLService
 *
 * @since 2023-6-26
 */
public interface SequenceObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * splicing sequence ddl
     *
     * @param request request
     * @return String
     */
    default String splicingSequenceDDL(DatabaseCreateSequenceDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop sequence ddl
     *
     * @param request request
     * @return String
     */
    default String dropSequenceDDL(DatabaseDropSequenceDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * return sequence ddl
     *
     * @param request request
     * @return String
     * @throws SQLException SQLException
     */
    default String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws SQLException {
        throw new CustomException(DebugUtils.getMessage());
    }
}
