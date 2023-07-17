/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.ElasticSearchCommon;
import com.nctigba.observability.sql.service.history.collection.elastic.DeadlockItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestDeadlockItem
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDeadlockItem {
    @InjectMocks
    private DeadlockItem item;

    @Test
    public void testGetQueryParam() {
        String sql = item.getQueryParam();
        assertEquals(sql, ElasticSearchCommon.DEADLOCK);
    }
}
