/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;

public interface SynonymObjectSQLService {
    String type();

    default String splicingSequenceDDL(DatabaseCreateSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }


    default String synonymAttributeSQL(DatabaseSynonymAttributeDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String dropSynonymSQL(DatabaseDropSynonymDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
