/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TestComparatorUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestComparatorUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.nctigba.observability.sql.model.vo.point.WaitEventInfoVO;
import com.nctigba.observability.sql.util.ComparatorUtils;
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
public class TestComparatorUtils {
    @InjectMocks
    private ComparatorUtils util;

    @Test
    public void testCompare() {
        WaitEventInfoVO event1 = new WaitEventInfoVO();
        event1.setEventCount(1);
        event1.setEventName("name");
        event1.setEventDetail("detail");
        WaitEventInfoVO event2 = new WaitEventInfoVO();
        event2.setEventCount(1);
        event2.setEventName("name");
        event2.setEventDetail("detail");
        int result = util.compare(event1, event2);
        assertEquals(0, result);
    }
}
