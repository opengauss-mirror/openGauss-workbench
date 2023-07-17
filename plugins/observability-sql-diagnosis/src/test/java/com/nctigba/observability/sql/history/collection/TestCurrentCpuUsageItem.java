/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.service.history.collection.agent.CurrentCpuUsageItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestCurrentCpuUsageItem
 *
 * @author luomeng
 * @since 2023/7/14
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCurrentCpuUsageItem {
    @InjectMocks
    private CurrentCpuUsageItem item;

    @Test
    public void testGetHttpParam() {
        String sql = item.getHttpParam();
        assertEquals(sql, AgentParamCommon.TOP);
    }
}
