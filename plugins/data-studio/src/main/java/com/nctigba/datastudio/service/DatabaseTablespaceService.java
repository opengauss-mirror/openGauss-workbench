/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDorpTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.RequestTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateTablespaceAttributeDTO;

/**
 * DatabaseUserService
 *
 * @since 2023-6-26
 */
public interface DatabaseTablespaceService {
    /**
     * create tablespace ddl
     *
     * @param request request
     * @return String
     */
    String createTablespaceDDL(DatabaseCreateTablespaceDTO request);

    /**
     * create tablespace
     *
     * @param request request
     */
    void createTablespace(DatabaseCreateTablespaceDTO request);

    /**
     * drop tablespace ddl
     *
     * @param request request
     */
    void dropTablespaceDDL(DatabaseDorpTablespaceDTO request);

    /**
     * tablespace attribute
     *
     * @param request request
     */
    DatabaseTablespaceAttributeDTO tablespaceAttribute(RequestTablespaceAttributeDTO request);

    /**
     * update tablespace attribute
     *
     * @param request request
     */
    void tablespaceUpdate(UpdateTablespaceAttributeDTO request);

    /**
     * update tablespace attribute ddl
     *
     * @param request request
     * @return String
     */
    String tablespaceUpdateAttributeDdl(UpdateTablespaceAttributeDTO request);
}
