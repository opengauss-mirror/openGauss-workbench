/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  TestSlowLogController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/controller/
 *  TestScheduleTaskController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.controller.ScheduleTaskController;
import com.nctigba.observability.sql.service.impl.TimeConfigServiceImpl;
import com.nctigba.observability.sql.scheduler.DynamicTaskScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestScheduleTaskController
 *
 * @author jianghongbo
 * @since 2025-09-15
 */
@ExtendWith(MockitoExtension.class)
public class TestScheduleTaskController {
    @Mock
    private DynamicTaskScheduler dynamicTaskScheduler;

    @Mock
    private TimeConfigServiceImpl timeConfigService;

    @InjectMocks
    private ScheduleTaskController scheduleTaskController;

    @Test
    void testChangeFrequencyWithSeconds() {
        // Arrange
        String frequency = "60s";
        doNothing().when(dynamicTaskScheduler).setFrequency(60);
        doNothing().when(timeConfigService).persistTimeConfig(isNull(), anyInt());

        // Act
        String result = scheduleTaskController.changeFrequency(frequency);

        // Assert
        assertEquals("shcedule task frequency changed to 60 s", result);
        verify(dynamicTaskScheduler).setFrequency(60);
        verify(timeConfigService).persistTimeConfig(null, 60);
    }

    @Test
    void testChangeFrequencyWithMinutes() {
        // Arrange
        String frequency = "5m";
        doNothing().when(dynamicTaskScheduler).setFrequency(300);
        doNothing().when(timeConfigService).persistTimeConfig(isNull(), anyInt());
        // Act
        String result = scheduleTaskController.changeFrequency(frequency);
        // Assert
        assertEquals("shcedule task frequency changed to 300 s", result);
        verify(dynamicTaskScheduler).setFrequency(300);
        verify(timeConfigService).persistTimeConfig(null, 300);
    }

    @Test
    void testChangeFrequencyWithHours() {
        // Arrange
        String frequency = "2h";
        doNothing().when(dynamicTaskScheduler).setFrequency(7200);
        doNothing().when(timeConfigService).persistTimeConfig(isNull(), anyInt());
        // Act
        String result = scheduleTaskController.changeFrequency(frequency);
        // Assert
        assertEquals("shcedule task frequency changed to 7200 s", result);
        verify(dynamicTaskScheduler).setFrequency(7200);
        verify(timeConfigService).persistTimeConfig(null, 7200);
    }

    @Test
    void testChangeFrequencyWithInvalidFormat() {
        // Arrange
        String frequency = "invalid";
        doNothing().when(dynamicTaskScheduler).setFrequency(300); // 默认值
        doNothing().when(timeConfigService).persistTimeConfig(isNull(), anyInt());
        // Act
        String result = scheduleTaskController.changeFrequency(frequency);
        // Assert
        assertEquals("shcedule task frequency changed to 300 s", result);
        verify(dynamicTaskScheduler).setFrequency(300);
        verify(timeConfigService).persistTimeConfig(null, 300);
    }

    @Test
    void testChangePeroidWithDays() {
        // Arrange
        String peroid = "30d";
        doNothing().when(timeConfigService).persistTimeConfig(anyInt(), isNull());
        // Act
        String result = scheduleTaskController.changePeroid(peroid);
        // Assert
        assertEquals("slow sql save days changed to 30", result);
        verify(timeConfigService).persistTimeConfig(30, null);
    }

    @Test
    void testChangePeroidWithMonths() {
        // Arrange
        String peroid = "2m";
        doNothing().when(timeConfigService).persistTimeConfig(anyInt(), isNull());

        // Act
        String result = scheduleTaskController.changePeroid(peroid);

        // Assert
        assertEquals("slow sql save days changed to 60", result);
        verify(timeConfigService).persistTimeConfig(60, null);
    }

    @Test
    void testChangePeroidWithInvalidFormat() {
        // Arrange
        String peroid = "invalid";
        doNothing().when(timeConfigService).persistTimeConfig(anyInt(), isNull());
        // Act
        String result = scheduleTaskController.changePeroid(peroid);
        // Assert
        assertEquals("slow sql save days changed to 30", result);
        verify(timeConfigService).persistTimeConfig(30, null);
    }

    @Test
    void testGetDefaultTime() {
        // Arrange
        Map<String, String> expectedConfig = new HashMap<>();
        expectedConfig.put("peroid", "1month");
        expectedConfig.put("frequency", "5m");
        when(timeConfigService.getTimeConfig()).thenReturn(expectedConfig);
        // Act
        Map<String, String> result = scheduleTaskController.getDefaultTime();
        // Assert
        assertEquals(expectedConfig, result);
        verify(timeConfigService).getTimeConfig();
    }
}
