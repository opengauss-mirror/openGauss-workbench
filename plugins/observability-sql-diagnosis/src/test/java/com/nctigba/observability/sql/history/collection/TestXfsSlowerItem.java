/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.service.history.collection.ebpf.XfsSlowerItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestXfsSlowerItem
 *
 * @author luomeng
 * @since 2023/8/21
 */
@RunWith(MockitoJUnitRunner.class)
public class TestXfsSlowerItem {
    @InjectMocks
    private XfsSlowerItem item;

    @Test
    public void testGetHttpParam() {
        String param = item.getHttpParam();
        assertEquals(param, AgentParamCommon.XFSSLOWER);
    }

    @Test
    public void testGetCollectionType() {
        String type = item.getCollectionType();
        assertEquals(type, CollectionTypeCommon.AFTER);
    }
}
