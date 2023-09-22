/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.service.history.collection.table.ExplainItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestExplainItem
 *
 * @author luomeng
 * @since 2023/9/17
 */
@RunWith(MockitoJUnitRunner.class)
public class TestExplainItem {
    @InjectMocks
    private ExplainItem sqlItem;

    @Test
    public void testGetDatabaseSql() {
        String sql = sqlItem.getDatabaseSql();
        assertEquals(sql, SqlCommon.DEFAULT);
    }

    @Test
    public void testGetCollectionType() {
        String type = sqlItem.getCollectionType();
        assertEquals(type, CollectionTypeCommon.AFTER);
    }
}
