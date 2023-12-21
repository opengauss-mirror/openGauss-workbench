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
 *  SynonymObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/SynonymObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_OR_REPLACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SYNONYM_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SYNONYM_ATTRIBUTE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SYNONYM_SQL;

/**
 * SynonymObjectSQLService achieve
 *
 * @since 2023-09-25
 */
@Slf4j
@Service
public class SynonymObjectSQLServiceImpl implements SynonymObjectSQLService {
    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String splicingSynonymDDL(DatabaseCreateSynonymDTO request) {
        log.info("splicingSynonymDDL request is: " + request);
        StringBuilder ddl = new StringBuilder();
        if (request.isReplace()) {
            ddl.append(CREATE_OR_REPLACE_SQL);
        } else {
            ddl.append(CREATE_SQL);
        }
        ddl.append(String.format(SYNONYM_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getSynonymName()), DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getObjectName())));
        log.info("splicingSynonymDDL response is: " + ddl);
        return ddl.toString();
    }

    @Override
    public String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        log.info("synonymAttribute request is: " + request);
        return String.format(SYNONYM_ATTRIBUTE_SQL, request.getSynonymName());
    }

    @Override
    public String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        return String.format(DROP_SYNONYM_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getSynonymName()));
    }
}
