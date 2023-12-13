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
 *  AlertScheduleServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/AlertScheduleServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertScheduleMapper;
import com.nctigba.alert.monitor.schedule.TaskRegistrar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AlertScheduleServiceImplTest
 *
 * @since 2023/8/22 10:35
 */
@RunWith(SpringRunner.class)
public class AlertScheduleServiceImplTest {
    @Mock
    private AlertRuleMapper ruleMapper;
    @Mock
    private TaskRegistrar taskRegistrar;
    @Mock
    private AlertScheduleMapper baseMapper;
    @InjectMocks
    private AlertScheduleServiceImpl alertScheduleService;

    @Test
    public void testAddTasksWithEmptyRuleIds() {
        Collection<Long> ruleIds = new ArrayList<>();
        alertScheduleService.addTasks(ruleIds);
    }

    @Test
    public void testAddTasksWithEmptySchedules() {
        when(baseMapper.selectCount(any())).thenReturn(1L);
        Collection<Long> ruleIds = new ArrayList<>();
        ruleIds.add(1L);
        alertScheduleService.addTasks(ruleIds);
        verify(baseMapper, times(1)).selectCount(any());
    }

    @Test
    public void testAddTasks() {
        try (MockedStatic<SqlHelper> mockedStatic = mockStatic(SqlHelper.class)) {
            Collection<Long> ruleIds = new ArrayList<>();
            ruleIds.add(1L);
            ruleIds.add(2L);
            ruleIds.add(3L);
            ruleIds.add(4L);
            when(baseMapper.selectCount(any())).thenReturn(0L);
            AlertRuleDO rule1 = new AlertRuleDO().setId(1L).setCheckFrequency(1).setCheckFrequencyUnit(
                CommonConstants.SECOND);
            when(ruleMapper.selectById(1L)).thenReturn(rule1);
            AlertRuleDO rule2 = new AlertRuleDO().setId(2L).setCheckFrequency(1).setCheckFrequencyUnit(
                CommonConstants.MINUTE);
            when(ruleMapper.selectById(2L)).thenReturn(rule2);
            AlertRuleDO rule3 = new AlertRuleDO().setId(3L).setCheckFrequency(1).setCheckFrequencyUnit(
                CommonConstants.HOUR);
            when(ruleMapper.selectById(3L)).thenReturn(rule3);
            AlertRuleDO rule4 =
                new AlertRuleDO().setId(4L).setCheckFrequency(1).setCheckFrequencyUnit(CommonConstants.DAY);
            when(ruleMapper.selectById(4L)).thenReturn(rule4);

            doNothing().when(taskRegistrar).addFixedDelay(any(Runnable.class), anyLong(), any(Date.class));

            alertScheduleService.addTasks(ruleIds);

            verify(baseMapper, times(4)).selectCount(any());
            verify(ruleMapper, times(4)).selectById(anyLong());
            verify(taskRegistrar, times(4))
                .addFixedDelay(any(Runnable.class), anyLong(), any(Date.class));
        }
    }

    @Test
    public void testRemoveTasksWithEmptyRuleIds() {
        Collection<Long> ruleIds = new ArrayList<>();
        alertScheduleService.removeTasks(ruleIds);
    }

    @Test
    public void testRemoveTasksWithEmptySchedules() {
        List<AlertScheduleDO> scheduleList = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(scheduleList);

        Collection<Long> ruleIds = new ArrayList<>();
        ruleIds.add(1L);
        alertScheduleService.removeTasks(ruleIds);

        verify(baseMapper, times(1)).selectList(any());
    }

    @Test
    public void testRemoveTasks() {
        try (MockedStatic<SqlHelper> mockedStatic = mockStatic(SqlHelper.class);
             MockedStatic<TableInfoHelper> mockedStatic2 = mockStatic(TableInfoHelper.class)
        ) {
            AlertScheduleDO alertScheduleDO = new AlertScheduleDO();
            List<AlertScheduleDO> scheduleList = new ArrayList<>();
            scheduleList.add(alertScheduleDO);
            when(baseMapper.selectList(any())).thenReturn(scheduleList);
            doNothing().when(taskRegistrar).removeTask(any(Runnable.class));
            TableInfo tableInfo = mock(TableInfo.class);
            mockedStatic2.when(() -> TableInfoHelper.getTableInfo(AlertScheduleDO.class)).thenReturn(tableInfo);

            Collection<Long> ruleIds = new ArrayList<>();
            ruleIds.add(1L);
            alertScheduleService.removeTasks(ruleIds);

            verify(baseMapper, times(1)).selectList(any());
            verify(taskRegistrar, times(1)).removeTask(any(Runnable.class));
            mockedStatic2.verify(() -> TableInfoHelper.getTableInfo(AlertScheduleDO.class));
        }
    }
}
