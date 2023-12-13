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
 *  TaskRegistrarTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/schedule/TaskRegistrarTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.schedule;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Date;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TaskRegistrarTest
 *
 * @since 2023/8/28 00:35
 */
@RunWith(SpringRunner.class)
public class TaskRegistrarTest {
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private AlertScheduleService scheduleService;

    @InjectMocks
    private TaskRegistrar taskRegistrar;

    private Runnable task = new Runnable() {
        @Override
        public void run() {

        }
    };

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            AlertScheduleDO.class);
    }

    @Test
    public void testGetScheduler() {
        taskRegistrar.getScheduler();
    }

    @Test
    public void testAddFixedRateTaskWithoutDate() {
        taskRegistrar.addFixedRateTask(task, 10L, null);
    }

    @Test
    public void testAddFixedRateTaskWithDate() {
        taskRegistrar.addFixedRateTask(task, 10L, new Date());
    }

    @Test
    public void testAddFixedRateTaskRepeat() {
        taskRegistrar.addFixedRateTask(task, 10L, new Date());
        taskRegistrar.addFixedRateTask(task, 20L, new Date());
    }

    @Test
    public void testAddFixedDelayWithoutDate() {
        taskRegistrar.addFixedDelay(task, 10L, null);
    }

    @Test
    public void testAddFixedDelayWithDate() {
        taskRegistrar.addFixedDelay(task, 10L, new Date());
    }

    @Test
    public void testAddFixedDelayRepeat() {
        taskRegistrar.addFixedDelay(task, 10L, new Date());
        taskRegistrar.addFixedDelay(task, 20L, new Date());
    }

    @Test
    public void testAddCronTask() {
        taskRegistrar.addCronTask(task, "*/10 * * * * ?");
    }

    @Test
    public void testAddCronTaskRepeat() {
        taskRegistrar.addCronTask(task, "*/10 * * * * ?");
        taskRegistrar.addCronTask(task, "*/20 * * * * ?");
    }

    @Test
    public void testRemoveTask() {
        taskRegistrar.removeTask(task);
    }

    @Test
    public void testDestroy() {
        taskRegistrar.addCronTask(task, "*/10 * * * * ?");
        when(scheduleService.update(any())).thenReturn(true);
        taskRegistrar.destroy();
    }
}
