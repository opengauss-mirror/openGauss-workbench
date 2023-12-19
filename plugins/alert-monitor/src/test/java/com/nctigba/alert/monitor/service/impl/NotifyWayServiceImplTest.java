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
 *  NotifyWayServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/NotifyWayServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.model.dto.NotifyWayDTO;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.service.AlertRuleService;
import com.nctigba.alert.monitor.service.AlertTemplateRuleService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the NotifyWayServiceImpl
 *
 * @since 2023/7/14 19:20
 */
@RunWith(SpringRunner.class)
public class NotifyWayServiceImplTest {
    @InjectMocks
    @Spy
    private NotifyWayServiceImpl notifyWayService;
    @Mock
    private AlertRuleService ruleService;
    @Mock
    private AlertTemplateRuleService templateRuleService;
    @Mock
    private NotifyWayMapper baseMapper;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            NotifyWayDO.class);
    }

    @Test
    public void testGetListPage() {
        Page page = new Page(1, 10);
        Page<NotifyWayDTO> notifyWayDtoPage = new Page<>(1, 10);
        when(baseMapper.selectDtoPage(eq(page), any())).thenReturn(notifyWayDtoPage);

        Page result = notifyWayService.getListPage("", "", page);

        verify(baseMapper, times(1)).selectDtoPage(eq(page), any());
        assertEquals(notifyWayDtoPage, result);
    }

    @Test
    public void testGetList() {
        List<NotifyWayDO> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyWayDO> resultList = notifyWayService.getList(anyString());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, resultList);
    }

    @Test
    public void testSaveNotifyWay1() {
        NotifyWayDO notifyWayDO = new NotifyWayDO();
        when(baseMapper.insert(any(NotifyWayDO.class))).thenReturn(1);
        notifyWayService.saveNotifyWay(notifyWayDO);
        verify(baseMapper, times(1)).insert(any(NotifyWayDO.class));
    }

    @Test
    public void testSaveNotifyWay2() {
        NotifyWayDO notifyWayDO = new NotifyWayDO().setId(1L);
        when(baseMapper.updateById(any(NotifyWayDO.class))).thenReturn(1);
        notifyWayService.saveNotifyWay(notifyWayDO);
        verify(baseMapper, times(1)).insert(any(NotifyWayDO.class));
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException1() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("alertRule");
            List<AlertRuleDO> ruleList = new ArrayList<>();
            AlertRuleDO alertRuleDO = new AlertRuleDO();
            ruleList.add(alertRuleDO);
            List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
            when(ruleService.list(any())).thenReturn(ruleList);
            when(templateRuleService.list(any())).thenReturn(templateRuleList);
            notifyWayService.delById(anyLong());
            verify(ruleService, times(1)).list(any());
            verify(templateRuleService, times(1)).list(any());
        }
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException2() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get(any())).thenReturn("alertRule");
            List<AlertRuleDO> ruleList = new ArrayList<>();
            List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            templateRuleList.add(alertTemplateRuleDO);
            when(ruleService.list(any())).thenReturn(ruleList);
            when(templateRuleService.list(any())).thenReturn(templateRuleList);
            notifyWayService.delById(anyLong());
            verify(ruleService, times(1)).list(any());
            verify(templateRuleService, times(1)).list(any());
        }
    }

    @Test
    public void testDelById() {
        List<AlertRuleDO> ruleList = new ArrayList<>();
        List<AlertTemplateRuleDO> templateRuleList = new ArrayList<>();
        when(ruleService.list(any())).thenReturn(ruleList);
        when(templateRuleService.list(any())).thenReturn(templateRuleList);
        when(baseMapper.update(any(), any())).thenReturn(1);

        notifyWayService.delById(anyLong());
        verify(ruleService, times(1)).list(any());
        verify(templateRuleService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
    }

    @Test(expected = ServiceException.class)
    public void testTestErrNotifyWay() {
        NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType("other");
        notifyWayService.testNotifyWay(notifyWayDO);
    }
}
