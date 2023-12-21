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
 *  SequenceObjectSQLService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/SequenceObjectSQLService.java
 *
 *  -------------------------------------------------------------------------
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
