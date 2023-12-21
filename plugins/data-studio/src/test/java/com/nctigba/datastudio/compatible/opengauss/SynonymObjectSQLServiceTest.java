/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SynonymObjectSQLServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/compatible/opengauss/SynonymObjectSQLServiceTest.java
 *
 *  -------------------------------------------------------------------------
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
