/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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
