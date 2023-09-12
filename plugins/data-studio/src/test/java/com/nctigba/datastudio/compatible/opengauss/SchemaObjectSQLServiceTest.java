/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.compatible.opengauss;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * SchemaObjectSQLServiceTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaObjectSQLServiceTest {
    @InjectMocks
    private SchemaObjectSQLServiceImpl schemaObjectSQLService;


    @Test
    public void testUpdateSchemaCommentSQL() {
        schemaObjectSQLService.updateSchemaCommentSQL("", null);
    }
}
