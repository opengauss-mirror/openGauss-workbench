/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TableSpaceObjectSqlService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/TableSpaceObjectSqlService.java
 *
 *  -------------------------------------------------------------------------
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
