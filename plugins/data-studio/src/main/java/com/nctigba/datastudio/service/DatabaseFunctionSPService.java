/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;

public interface DatabaseFunctionSPService {
    String functionDdl(DatabaseFunctionSPDTO request) throws Exception;

    void dropFunctionSP(DatabaseFunctionSPDTO request) throws Exception;
}
