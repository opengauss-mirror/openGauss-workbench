/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.SynonymObjectSQLService;
import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_REPLACE_SYNONYM_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_SYNONYM_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DROP_SYNONYM_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SYNONYM_ATTRIBUTE_SQL;

@Slf4j
@Service
public class SynonymObjectSQLServiceImpl implements SynonymObjectSQLService {
    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String splicingSequenceDDL(DatabaseCreateSynonymDTO request) {
        log.info("splicingSequenceDDL request is: " + request);
        String ddl;
        if (request.isReplace()) {
            ddl = String.format(CREATE_REPLACE_SYNONYM_SQL, request.getSchema(),
                    request.getSynonymName(), request.getSchema(), request.getObjectName());
        } else {
            ddl = String.format(CREATE_SYNONYM_SQL, request.getSchema(),
                    request.getSynonymName(), request.getSchema(), request.getObjectName());
        }
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        log.info("synonymAttribute request is: " + request);
        return String.format(SYNONYM_ATTRIBUTE_SQL, request.getSynonymName());
    }

    @Override
    public String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        log.info("dropSynonym request is: " + request);
        return String.format(DROP_SYNONYM_SQL, request.getSchema(), request.getSynonymName());
    }
}
