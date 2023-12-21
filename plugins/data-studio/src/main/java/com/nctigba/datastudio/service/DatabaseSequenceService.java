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
 *  DatabaseSequenceService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/DatabaseSequenceService.java
 *
 *  -------------------------------------------------------------------------
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
