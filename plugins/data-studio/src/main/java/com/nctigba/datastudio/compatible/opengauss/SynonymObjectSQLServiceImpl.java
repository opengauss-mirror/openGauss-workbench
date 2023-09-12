/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.util.DebugUtils;
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
 * @since 2023-06-26
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
        log.info("splicingSequenceDDL request is: " + request);
        StringBuilder ddl = new StringBuilder();
        if (request.isReplace()) {
            ddl.append(CREATE_OR_REPLACE_SQL);
        } else {
            ddl.append(CREATE_SQL);
        }
        ddl.append(String.format(SYNONYM_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getSynonymName()), DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getObjectName())));
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl.toString();
    }

    @Override
    public String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        log.info("synonymAttribute request is: " + request);
        return String.format(SYNONYM_ATTRIBUTE_SQL, DebugUtils.needQuoteName(request.getSynonymName()));
    }

    @Override
    public String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        return String.format(DROP_SYNONYM_SQL, DebugUtils.needQuoteName(request.getSchema()),
                DebugUtils.needQuoteName(request.getSynonymName()));
    }
}
