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
 *  DatabaseSynonymService.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/DatabaseSynonymService.java
 *
 *  -------------------------------------------------------------------------
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
