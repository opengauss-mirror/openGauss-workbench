/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.NotifyConfigReq;
import com.nctigba.alert.monitor.service.DingTalkService;
import com.nctigba.alert.monitor.service.EmailService;
import com.nctigba.alert.monitor.service.WeComService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.SmartValidator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the function of the NotifyConfigServiceImpl
 *
 * @since 2023/7/14 14:13
 */
@RunWith(SpringRunner.class)
public class NotifyConfigServiceImplTest {
    @InjectMocks
    @Spy
    private NotifyConfigServiceImpl notifyConfigService;
    @Mock
    private NotifyConfigMapper baseMapper;
    @Mock
    private EmailService emailService;
    @Mock
    private NotifyWayMapper notifyWayMapper;
    @Mock
    private NotifyTemplateMapper notifyTemplateMapper;
    @Mock
    private SmartValidator smartValidator;
    @Mock
    private WeComService weComService;
    @Mock
    private DingTalkService dingTalkService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            NotifyConfig.class);
    }

    @Test
    public void testGetAllListNull() {
        List<NotifyConfig> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyConfig> result = notifyConfigService.getAllList();
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list.size(), result.size());
    }

    @Test
    public void testGetAllList() {
        NotifyConfig notifyConfig = new NotifyConfig();
        notifyConfig.setType("email");
        List<NotifyConfig> list = new ArrayList<>();
        list.add(notifyConfig);
        when(baseMapper.selectList(any())).thenReturn(list);

        List<NotifyConfig> notifyConfigList = notifyConfigService.getAllList();

        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, notifyConfigList);
    }

    @Test(expected = ServiceException.class)
    public void testSaveListThrowException() {
        List<NotifyConfig> list = new ArrayList<>();
        notifyConfigService.saveList(list);
    }

    @Test
    public void testSaveList1() {
        List<NotifyConfig> list = new ArrayList<>();
        NotifyConfig notifyConfig1 = new NotifyConfig();
        notifyConfig1.setType(CommonConstants.EMAIL).setId(1L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfig1);
        NotifyConfig notifyConfig2 = new NotifyConfig();
        notifyConfig2.setType(CommonConstants.WE_COM).setId(2L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfig2);
        NotifyConfig notifyConfig3 = new NotifyConfig();
        notifyConfig3.setType(CommonConstants.DING_TALK).setId(3L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfig3);

        doNothing().when(smartValidator).validate(any(), any());

        List<NotifyConfig> oldList = new ArrayList<>();
        NotifyConfig notifyConfig = new NotifyConfig().setType(CommonConstants.EMAIL).setId(1L)
            .setEnable(CommonConstants.ENABLE);
        oldList.add(notifyConfig);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        when(notifyConfigService.saveOrUpdateBatch(anyList())).thenReturn(true);

        notifyConfigService.saveList(list);
        verify(baseMapper, times(1)).selectList(any());
        verify(notifyConfigService, times(2)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testSaveList2() {
        List<NotifyConfig> list = new ArrayList<>();
        NotifyConfig notifyConfig1 = new NotifyConfig();
        notifyConfig1.setType(CommonConstants.EMAIL).setId(1L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfig1);
        NotifyConfig notifyConfig2 = new NotifyConfig();
        notifyConfig2.setType(CommonConstants.WE_COM).setId(2L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfig2);
        NotifyConfig notifyConfig3 = new NotifyConfig();
        notifyConfig3.setType(CommonConstants.DING_TALK).setId(3L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfig3);

        doNothing().when(smartValidator).validate(any(), any());

        List<NotifyConfig> oldList = new ArrayList<>();
        NotifyConfig notifyConfig = new NotifyConfig().setType(CommonConstants.EMAIL).setId(1L)
            .setEnable(CommonConstants.ENABLE);
        oldList.add(notifyConfig);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        when(notifyConfigService.saveOrUpdateBatch(anyList())).thenReturn(true);

        notifyConfigService.saveList(list);
        verify(baseMapper, times(1)).selectList(any());
        verify(notifyConfigService, times(2)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testTestEmailConfig() {
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        notifyConfigReq.setType(CommonConstants.EMAIL);
        notifyConfigReq.setNotifyWayId(1L);
        NotifyWay notifyWay = new NotifyWay().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWay);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        doNothing().when(emailService).sendTest(notifyConfigReq, notifyWay.getEmail(), notifyTemplate.getNotifyTitle(),
            notifyTemplate.getNotifyContent());

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
        verify(emailService, times(1)).sendTest(notifyConfigReq, notifyWay.getEmail(), notifyTemplate.getNotifyTitle(),
            notifyTemplate.getNotifyContent());
    }

    @Test
    public void testTestWeComConfig() {
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        notifyConfigReq.setType(CommonConstants.WE_COM);
        notifyConfigReq.setNotifyWayId(1L);
        NotifyWay notifyWay = new NotifyWay().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWay);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(weComService.sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent())).thenReturn(true);

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
        verify(weComService, times(1)).sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent());
    }

    @Test
    public void testTestDingTalkConfig() {
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        notifyConfigReq.setType(CommonConstants.DING_TALK);
        notifyConfigReq.setNotifyWayId(1L);
        NotifyWay notifyWay = new NotifyWay().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWay);
        NotifyTemplate notifyTemplate = new NotifyTemplate().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        when(dingTalkService.sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent())).thenReturn(true);

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
        verify(dingTalkService, times(1)).sendTest(notifyConfigReq, notifyWay, notifyTemplate.getNotifyContent());
    }

    @Test(expected = ServiceException.class)
    public void testTestConfigThrowException() {
        NotifyConfigReq notifyConfigReq = new NotifyConfigReq();
        notifyConfigReq.setNotifyWayId(1L);
        notifyConfigReq.setType("other");
        NotifyWay notifyWay = new NotifyWay().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWay);

        NotifyTemplate notifyTemplate = new NotifyTemplate().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplate);

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
    }
}
