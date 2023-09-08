/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.model.dto.DatabaseCreateSynonymDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * SynonymObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class SynonymObjectSQLServiceTest {
    @InjectMocks
    private SynonymObjectSQLServiceImpl synonymObjectSQLService;

    @Test
    public void testUpdateSchemaCommentSQL() {
        DatabaseCreateSynonymDTO databaseCreateSynonymDTO = new DatabaseCreateSynonymDTO();
        databaseCreateSynonymDTO.setReplace(false);
        databaseCreateSynonymDTO.setSynonymName("ss");
        databaseCreateSynonymDTO.setSchema("s1s");
        databaseCreateSynonymDTO.setObjectName("s1s1");
        synonymObjectSQLService.splicingSynonymDDL(databaseCreateSynonymDTO);
    }
}
