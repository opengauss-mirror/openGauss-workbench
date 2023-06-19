/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible;

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.util.DebugUtils;
import org.opengauss.admin.common.exception.CustomException;

public interface FunctionSPObjectSQLService {
    String type();

    default String functionDdl(DatabaseFunctionSPDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }

    default String dropFunctionSP(DatabaseFunctionSPDTO request) {
        throw new CustomException(DebugUtils.getMessage());
    }
}
