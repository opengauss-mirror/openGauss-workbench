/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.RequestTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateTablespaceAttributeDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

/**
 * TablespaceObjectSQLService
 *
 * @since 2023-06-25
 */
public interface TableSpaceObjectSqlService {
    /**
     * type
     *
     * @return String
     */
    String type();

    /**
     * create tablespace ddl
     *
     * @param request request
     * @return String
     */
    default String createTablespaceDDL(DatabaseCreateTablespaceDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * drop tablespace ddl
     *
     * @param name name
     * @return String
     */
    default String dropTablespaceDDL(String name) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * create tablespace ddl
     *
     * @param request request
     * @return String
     */
    default DatabaseTablespaceAttributeDTO tablespaceAttribute(RequestTablespaceAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    /**
     * update tablespace ddl
     *
     * @param request request
     * @return String
     */
    default String tablespaceUpdateAttributeDdl(UpdateTablespaceAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
