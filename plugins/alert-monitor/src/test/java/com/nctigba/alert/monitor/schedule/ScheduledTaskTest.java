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
 *  ScheduledTaskTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/schedule/ScheduledTaskTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ScheduledFuture;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ScheduledTaskTest
 *
 * @since 2023/8/28 01:16
 */
@RunWith(SpringRunner.class)
public class ScheduledTaskTest {
    @Mock
    private ScheduledFuture<?> future;

    @InjectMocks
    private ScheduledTask scheduledTask;

    @Test
    public void testCancel() {
        when(future.cancel(true)).thenReturn(true);
        scheduledTask.cancel();
        verify(future, times(1)).cancel(true);
    }
}
