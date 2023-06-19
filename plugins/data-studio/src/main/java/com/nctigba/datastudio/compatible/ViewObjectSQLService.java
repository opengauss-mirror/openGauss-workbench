/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import org.opengauss.admin.common.exception.CustomException;

public interface ViewObjectSQLService {
    String type();

    default String splicingViewDDL(DatabaseCreateViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String returnDatabaseViewDDL(DatabaseViewDdlDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String returnDropViewSQL(DatabaseViewDdlDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String returnSelectViewSQL(DatabaseSelectViewDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
