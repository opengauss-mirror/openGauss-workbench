/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.service.history.collection.ebpf.BioLatencyItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestBioLatencyItem
 *
 * @author luomeng
 * @since 2023/8/21
 */
@RunWith(MockitoJUnitRunner.class)
public class TestBioLatencyItem {
    @InjectMocks
    private BioLatencyItem item;

    @Test
    public void testGetHttpParam() {
        String param = item.getHttpParam();
        assertEquals(param, AgentParamCommon.BIOLATENCY);
    }

    @Test
    public void testGetCollectionType() {
        String type = item.getCollectionType();
        assertEquals(type, CollectionTypeCommon.AFTER);
    }
}
