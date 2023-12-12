/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 *  SynonymObjectSQLService
 *
 * @since 2023-06-25
 */
public interface SynonymObjectSQLService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * splicing synonym ddl
     *
     * @param request request
     * @return String
     */
    default String splicingSynonymDDL(DatabaseCreateSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }


    /**
     * synonym attribute sql
     *
     * @param request request
     * @return String
     */
    default String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop synonym sql
     *
     * @param request request
     * @return String
     */
    default String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
