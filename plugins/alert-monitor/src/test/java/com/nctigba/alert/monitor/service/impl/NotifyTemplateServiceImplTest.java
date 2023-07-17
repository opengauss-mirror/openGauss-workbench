/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
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
            NotifyTemplate.class);
    }

    @Test
    public void testGetListPage() {
        Page page = new Page(1, 10);
        Page<NotifyTemplate> templatePage = new Page<>(1, 10);
        when(baseMapper.selectPage(eq(page), any())).thenReturn(templatePage);

        Page result = notifyTemplateService.getListPage("", "", page);

        verify(baseMapper, times(1)).selectPage(eq(page), any());
        assertEquals(templatePage, result);
    }

    @Test
    public void testSaveTemplate1() {
        NotifyTemplate notifyTemplate = new NotifyTemplate();
        when(baseMapper.insert(any(NotifyTemplate.class))).thenReturn(1);
        notifyTemplateService.saveTemplate(notifyTemplate);
        verify(baseMapper, times(1)).insert(any(NotifyTemplate.class));
    }

    @Test
    public void testSaveTemplate2() {
        NotifyTemplate notifyTemplate = new NotifyTemplate().setId(1L);
        when(baseMapper.updateById(any(NotifyTemplate.class))).thenReturn(1);
        notifyTemplateService.saveTemplate(notifyTemplate);
        verify(baseMapper, times(1)).insert(any(NotifyTemplate.class));
    }

    @Test(expected = ServiceException.class)
    public void testDelByIdThrowException() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            List<NotifyWay> list = new ArrayList<>();
            NotifyWay notifyWay = new NotifyWay();
            list.add(notifyWay);
            when(notifyWayService.list(any())).thenReturn(list);
            notifyTemplateService.delById(anyLong());
            verify(notifyWayService, times(1)).list(any());
        }
    }

    @Test
    public void testDelById() {
        List<NotifyWay> list = new ArrayList<>();
        when(notifyWayService.list(any())).thenReturn(list);
        when(baseMapper.update(any(), any())).thenReturn(1);

        notifyTemplateService.delById(anyLong());

        verify(notifyWayService, times(1)).list(any());
        verify(baseMapper, times(1)).update(any(), any());
    }

    @Test
    public void testGetList() {
        List<NotifyTemplate> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyTemplate> resultList = notifyTemplateService.getList(anyString());
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, resultList);
    }
}
