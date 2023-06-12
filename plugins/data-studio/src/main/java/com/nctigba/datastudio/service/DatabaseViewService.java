/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;

import java.util.Map;

public interface DatabaseViewService {
    String createViewDDL(DatabaseCreateViewDTO request) throws Exception;

    String returnViewDDL(DatabaseViewDdlDTO request) throws Exception;

    void createView(DatabaseCreateViewDTO request) throws Exception;

    void dropView(DatabaseViewDdlDTO request) throws Exception;

    Map<String, Object> selectView(DatabaseSelectViewDTO request) throws Exception;

}
