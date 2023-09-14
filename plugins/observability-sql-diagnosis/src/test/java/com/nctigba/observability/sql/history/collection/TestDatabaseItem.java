/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.service.history.collection.table.DatabaseItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestDatabaseItem
 *
 * @author luomeng
 * @since 2023/8/21
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseItem {
    @InjectMocks
    private DatabaseItem sqlItem;

    @Test
    public void testGetDatabaseSql() {
        String sql = sqlItem.getDatabaseSql();
        assertEquals(sql, SqlCommon.DEFAULT);
    }
}
