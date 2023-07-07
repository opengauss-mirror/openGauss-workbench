/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

public interface SequenceObjectSQLService {
    String type();

    default String splicingSequenceDDL(DatabaseCreateSequenceDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String dropSequenceDDL(DatabaseDropSequenceDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws Exception {
        throw new CustomException(DebugUtils.getMessage());
    }
}
