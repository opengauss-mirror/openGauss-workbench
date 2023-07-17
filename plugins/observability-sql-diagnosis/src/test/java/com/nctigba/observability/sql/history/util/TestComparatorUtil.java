/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.model.history.dto.WaitEventInfo;
import com.nctigba.observability.sql.util.ComparatorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TestComparatorUtil
 *
 * @author luomeng
 * @since 2023/7/14
 */
@RunWith(MockitoJUnitRunner.class)
public class TestComparatorUtil {
    @InjectMocks
    private ComparatorUtil util;

    @Test
    public void testCompare() {
        WaitEventInfo event1 = new WaitEventInfo();
        event1.setEventCount(1);
        event1.setEventName("name");
        event1.setEventDetail("detail");
        WaitEventInfo event2 = new WaitEventInfo();
        event2.setEventCount(1);
        event2.setEventName("name");
        event2.setEventDetail("detail");
        int result = util.compare(event1, event2);
        assertEquals(0, result);
    }
}
