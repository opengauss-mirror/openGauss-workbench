/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.service.history.collection.table.ThreadWaitEventItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestThreadWaitEventItem
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestThreadWaitEventItem {
    @InjectMocks
    private ThreadWaitEventItem sqlItem;

    @Test
    public void testGetDatabaseSql() {
        String sql = sqlItem.getDatabaseSql();
        assertEquals(sql, SqlCommon.WAIT_EVENT);
    }
}
