/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service;

import com.nctigba.datastudio.model.dto.DatabaseCreateSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDropSequenceDTO;
import com.nctigba.datastudio.model.dto.DatabaseSequenceDdlDTO;

public interface DatabaseSequenceService {
    String createSequenceDDL(DatabaseCreateSequenceDTO request) throws Exception;

    void createSequence(DatabaseCreateSequenceDTO request) throws Exception;

    void dropSequence(DatabaseDropSequenceDTO request) throws Exception;

    String returnSequenceDDL(DatabaseSequenceDdlDTO request) throws Exception;
}
