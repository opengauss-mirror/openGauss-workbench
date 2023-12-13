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
 *  NotifyConfigServiceImplTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/impl/NotifyConfigServiceImplTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.mapper.NotifyWayMapper;
import com.nctigba.alert.monitor.model.query.NotifyConfigQuery;
import com.nctigba.alert.monitor.service.impl.communication.WeComServiceImpl;
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
    private NotifyWayMapper notifyWayMapper;
    @Mock
    private NotifyTemplateMapper notifyTemplateMapper;
    @Mock
    private SmartValidator smartValidator;
    @Mock
    private WeComServiceImpl weComServiceImpl;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
            NotifyConfigDO.class);
    }

    @Test
    public void testGetAllListNull() {
        List<NotifyConfigDO> list = new ArrayList<>();
        when(baseMapper.selectList(any())).thenReturn(list);
        List<NotifyConfigDO> result = notifyConfigService.getAllList();
        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list.size(), result.size());
    }

    @Test
    public void testGetAllList() {
        NotifyConfigDO notifyConfigDO = new NotifyConfigDO();
        notifyConfigDO.setType("email");
        List<NotifyConfigDO> list = new ArrayList<>();
        list.add(notifyConfigDO);
        when(baseMapper.selectList(any())).thenReturn(list);

        List<NotifyConfigDO> notifyConfigDOList = notifyConfigService.getAllList();

        verify(baseMapper, times(1)).selectList(any());
        assertEquals(list, notifyConfigDOList);
    }

    @Test(expected = ServiceException.class)
    public void testSaveListThrowException() {
        List<NotifyConfigDO> list = new ArrayList<>();
        notifyConfigService.saveList(list);
    }

    @Test
    public void testSaveList1() {
        List<NotifyConfigDO> list = new ArrayList<>();
        NotifyConfigDO notifyConfigDO1 = new NotifyConfigDO();
        notifyConfigDO1.setType(CommonConstants.EMAIL).setId(1L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfigDO1);
        NotifyConfigDO notifyConfigDO2 = new NotifyConfigDO();
        notifyConfigDO2.setType(CommonConstants.WE_COM).setId(2L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfigDO2);
        NotifyConfigDO notifyConfigDO3 = new NotifyConfigDO();
        notifyConfigDO3.setType(CommonConstants.DING_TALK).setId(3L).setEnable(CommonConstants.ENABLE);
        list.add(notifyConfigDO3);

        doNothing().when(smartValidator).validate(any(), any());

        List<NotifyConfigDO> oldList = new ArrayList<>();
        NotifyConfigDO notifyConfigDO = new NotifyConfigDO().setType(CommonConstants.EMAIL).setId(1L)
            .setEnable(CommonConstants.ENABLE);
        oldList.add(notifyConfigDO);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        when(notifyConfigService.saveOrUpdateBatch(anyList())).thenReturn(true);

        notifyConfigService.saveList(list);
        verify(baseMapper, times(1)).selectList(any());
        verify(notifyConfigService, times(2)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testSaveList2() {
        List<NotifyConfigDO> list = new ArrayList<>();
        NotifyConfigDO notifyConfigDO1 = new NotifyConfigDO();
        notifyConfigDO1.setType(CommonConstants.EMAIL).setId(1L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfigDO1);
        NotifyConfigDO notifyConfigDO2 = new NotifyConfigDO();
        notifyConfigDO2.setType(CommonConstants.WE_COM).setId(2L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfigDO2);
        NotifyConfigDO notifyConfigDO3 = new NotifyConfigDO();
        notifyConfigDO3.setType(CommonConstants.DING_TALK).setId(3L).setEnable(CommonConstants.DISABLE);
        list.add(notifyConfigDO3);

        doNothing().when(smartValidator).validate(any(), any());

        List<NotifyConfigDO> oldList = new ArrayList<>();
        NotifyConfigDO notifyConfigDO = new NotifyConfigDO().setType(CommonConstants.EMAIL).setId(1L)
            .setEnable(CommonConstants.ENABLE);
        oldList.add(notifyConfigDO);
        when(baseMapper.selectList(any())).thenReturn(oldList);
        when(notifyConfigService.saveOrUpdateBatch(anyList())).thenReturn(true);

        notifyConfigService.saveList(list);
        verify(baseMapper, times(1)).selectList(any());
        verify(notifyConfigService, times(2)).saveOrUpdateBatch(anyList());
    }

    @Test
    public void testTestWeComConfig() {
        NotifyConfigQuery notifyConfigReq = new NotifyConfigQuery();
        notifyConfigReq.setType(CommonConstants.WE_COM);
        notifyConfigReq.setNotifyWayId(1L);
        NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWayDO);
        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplateDO);
        when(weComServiceImpl.sendTest(notifyConfigReq, notifyWayDO, notifyTemplateDO.getNotifyContent()))
            .thenReturn(true);

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
        verify(weComServiceImpl, times(1)).sendTest(notifyConfigReq, notifyWayDO, notifyTemplateDO.getNotifyContent());
    }

    @Test(expected = ServiceException.class)
    public void testTestConfigThrowException() {
        NotifyConfigQuery notifyConfigReq = new NotifyConfigQuery();
        notifyConfigReq.setNotifyWayId(1L);
        notifyConfigReq.setType("other");
        NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyTemplateId(1L);
        when(notifyWayMapper.selectById(anyLong())).thenReturn(notifyWayDO);

        NotifyTemplateDO notifyTemplateDO = new NotifyTemplateDO().setId(1L);
        when(notifyTemplateMapper.selectById(anyLong())).thenReturn(notifyTemplateDO);

        notifyConfigService.testConfig(notifyConfigReq);

        verify(notifyWayMapper, times(1)).selectById(anyLong());
        verify(notifyTemplateMapper, times(1)).selectById(anyLong());
    }
}
