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
 *  NotifyTemplateServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/NotifyTemplateServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.service.NotifyWayService;
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
 * test the function of the NotifyTemplateServiceImpl
 *
 * @since 2023/7/14 18:54
 */
@RunWith(SpringRunner.class)
public class NotifyTemplateServiceImplTest {
    @InjectMocks
    @Spy
    private NotifyTemplateServiceImpl notifyTemplateService;
    @Mock
    private NotifyTemplateMapper baseMapper;

    @Mock
    private NotifyWayService notifyWayService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            NotifyTemplateDO.class);
    }

    @Test
    public void testGetListPage() {
        Page page = new Page(1, 10);
        Page<NotifyTemplateDO> templatePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(templatePage);

        Page result = notifyTemplateService.getListPage("", "", page);

        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(templatePage, result);
    }

    @Test
    public void testSaveTemplate1() {
        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO();
        when(baseMapper.insert(any(NotifyTemplateDO.class))).thenReturn(1);
        notifyTemplateService.saveTemplate(notifyTemplateDO);
        verify(baseMapper, times(1)).insert(any(NotifyTemplateDO.class));
    }

    @Test
    public void testSaveTemplate2() {
        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO().setId(1L);
        when(baseMapper.updateById(any(NotifyTemplateDO.class))).thenReturn(1);
        notifyTemplateService.saveTemplate(notifyTemplateDO);
        verify(baseMapper, times(1)).insert(any(NotifyTemplateDO.class));
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            List<NotifyWayDO> list = new ArrayList<>();
            NotifyWayDO notifyWayDO = new NotifyWayDO();
            list.add(notifyWayDO);
            when(notifyWayService.list(any())).thenReturn(list);
            notifyTemplateService.delById(anyLong());
            verify(notifyWayService, times(1)).list(any());
        }
    }

    @Test
    public void testDelById() {
        List<NotifyWayDO> list = new ArrayList<>();
        when(notifyWayService.list(any())).thenReturn(list);
        when(baseMapper.update(any(), any())).thenReturn(1);

        notifyTemplateService.delById(anyLong());

        verify(notifyWayService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
    }

    @Test
    public void testGetList() {
        List<NotifyTemplateDO> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyTemplateDO> resultList = notifyTemplateService.getList(anyString());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, resultList);
    }
}
