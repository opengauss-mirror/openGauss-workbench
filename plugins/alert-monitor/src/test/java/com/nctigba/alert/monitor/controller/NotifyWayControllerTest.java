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
 *  NotifyWayControllerTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/controller/NotifyWayControllerTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.page.TableDataInfo;
import org.opengauss.admin.common.utils.ServletUtils;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test NotifyWayController
 *
 * @since 2023/7/16 12:41
 */
@RunWith(SpringRunner.class)
public class NotifyWayControllerTest {
    @InjectMocks
    private NotifyWayController notifyWayController;
    @Mock
    private NotifyWayService notifyWayService;

    @Test
    public void testGetListPage() {
        try (MockedStatic<ServletUtils> mockedStatic = mockStatic(ServletUtils.class)) {
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageNum")).thenReturn(1);
            mockedStatic.when(() -> ServletUtils.getParameterToInt("pageSize")).thenReturn(10);
            Page page = new Page(1, 10);
            when(notifyWayService.getListPage(anyString(), anyString(), any(Page.class))).thenReturn(page);
            TableDataInfo result = notifyWayController.getListPage("", "");
            verify(notifyWayService, times(1))
                .getListPage(anyString(), anyString(), any(Page.class));
            assertEquals(page.getRecords(), result.getRows());
            assertEquals(page.getTotal(), result.getTotal());
        }
    }

    @Test
    public void testGetList() {
        List<NotifyWayDO> list = new ArrayList<>();
        when(notifyWayService.getList(anyString())).thenReturn(list);
        AjaxResult result = notifyWayController.getList("");
        verify(notifyWayService, times(1)).getList(anyString());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetById() {
        NotifyWayDO notifyWayDO = new NotifyWayDO();
        when(notifyWayService.getById(anyLong())).thenReturn(notifyWayDO);
        AjaxResult result = notifyWayController.getById(1L);
        verify(notifyWayService, times(1)).getById(anyLong());
        assertEquals(notifyWayDO, result.get("data"));
    }

    @Test
    public void testSaveEmailNotifyWayWithCheckFail() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.EMAIL);
            notifyWayController.saveNotifyWay(notifyWayDO);
        }
    }

    @Test
    public void testSaveEmailNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.EMAIL).setEmail("123@163.com");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWeComNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("1").setDeptId("1");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWeComRobotNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveDingTalkNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("1");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveDingTalkRobotNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.DING_TALK)
                .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWebhookNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveSnmpNotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version2c).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveSnmp3NotifyWay() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWayDO);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestNotifyWayReturnErr() {
        NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WE_COM);
        AjaxResult result = notifyWayController.testNotifyWay(notifyWayDO);
        assertEquals(AjaxResult.error(), result);
    }

    @Test
    public void testTestWebHookNotifyWay1() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            when(notifyWayService.testNotifyWay(notifyWayDO)).thenReturn(true);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestWebHookNotifyWay2() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            when(notifyWayService.testNotifyWay(notifyWayDO)).thenReturn(false);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.error(), result);
        }
    }

    @Test
    public void testTestSnmpNotifyWay1() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            when(notifyWayService.testNotifyWay(notifyWayDO)).thenReturn(true);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestSnmpNotifyWay2() {
        try (MockedStatic<MessageSourceUtils> mockedStatic = mockStatic(MessageSourceUtils.class)) {
            mockedStatic.when(() -> MessageSourceUtils.get("validateFail")).thenReturn("Validation failed");
            NotifyWayDO notifyWayDO = new NotifyWayDO().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            when(notifyWayService.testNotifyWay(notifyWayDO)).thenReturn(false);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWayDO);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWayDO.class));
            assertEquals(AjaxResult.error(), result);
        }
    }

    @Test
    public void testDelById() {
        doNothing().when(notifyWayService).delById(anyLong());
        AjaxResult result = notifyWayController.delById(1L);
        verify(notifyWayService, times(1)).delById(anyLong());
        assertEquals(AjaxResult.success(), result);
    }
}
