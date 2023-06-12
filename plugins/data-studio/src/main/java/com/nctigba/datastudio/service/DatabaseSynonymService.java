/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSynonymDTO;
import com.nctigba.datastudio.model.dto.DatabaseSynonymAttributeDTO;

import java.util.Map;

public interface DatabaseSynonymService {
    String createSynonymDDL(DatabaseCreateSynonymDTO request) throws Exception;

    void createSynonym(DatabaseCreateSynonymDTO request) throws Exception;

    Map<String, Object> synonymAttribute(DatabaseSynonymAttributeDTO request) throws Exception;

    void dropSynonym(DatabaseDropSynonymDTO request) throws Exception;
}
