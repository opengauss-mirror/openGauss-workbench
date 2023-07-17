/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.service.history.collection.table.SlowSqlItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestSlowSqlItem
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSlowSqlItem {
    @InjectMocks
    private SlowSqlItem sqlItem;

    @Test
    public void testGetDatabaseSql() {
        String sql = sqlItem.getDatabaseSql();
        assertEquals(sql, SqlCommon.SLOW_SQL);
    }
}
