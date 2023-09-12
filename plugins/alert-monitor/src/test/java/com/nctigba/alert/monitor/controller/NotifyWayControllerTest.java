/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.service.NotifyWayService;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
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
        List<NotifyWay> list = new ArrayList<>();
        when(notifyWayService.getList(anyString())).thenReturn(list);
        AjaxResult result = notifyWayController.getList("");
        verify(notifyWayService, times(1)).getList(anyString());
        assertEquals(list, result.get("data"));
    }

    @Test
    public void testGetById() {
        NotifyWay notifyWay = new NotifyWay();
        when(notifyWayService.getById(anyLong())).thenReturn(notifyWay);
        AjaxResult result = notifyWayController.getById(1L);
        verify(notifyWayService, times(1)).getById(anyLong());
        assertEquals(notifyWay, result.get("data"));
    }

    @Test
    public void testSaveEmailNotifyWayWithCheckFail() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.EMAIL);
            notifyWayController.saveNotifyWay(notifyWay);
        }
    }

    @Test
    public void testSaveEmailNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.EMAIL).setEmail("123@163.com");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWeComNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("1").setDeptId("1");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWeComRobotNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveDingTalkNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WE_COM)
                .setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("1");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveDingTalkRobotNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.DING_TALK)
                .setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveWebhookNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveSnmpNotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version2c).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testSaveSnmp3NotifyWay() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            doNothing().when(notifyWayService).saveNotifyWay(notifyWay);
            AjaxResult result = notifyWayController.saveNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).saveNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestNotifyWayReturnErr() {
        NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WE_COM);
        AjaxResult result = notifyWayController.testNotifyWay(notifyWay);
        assertEquals(AjaxResult.error(), result);
    }

    @Test
    public void testTestWebHookNotifyWay1() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            when(notifyWayService.testNotifyWay(notifyWay)).thenReturn(true);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestWebHookNotifyWay2() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.WEBHOOK).setWebhook("http");
            when(notifyWayService.testNotifyWay(notifyWay)).thenReturn(false);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.error(), result);
        }
    }

    @Test
    public void testTestSnmpNotifyWay1() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            when(notifyWayService.testNotifyWay(notifyWay)).thenReturn(true);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWay.class));
            assertEquals(AjaxResult.success(), result);
        }
    }

    @Test
    public void testTestSnmpNotifyWay2() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            mockedStatic.when(() -> MessageSourceUtil.get("validateFail")).thenReturn("Validation failed");
            NotifyWay notifyWay = new NotifyWay().setNotifyType(CommonConstants.SNMP).setSnmpIp("127.0.0.1")
                .setSnmpVersion(SnmpConstants.version3).setSnmpPort("8080").setSnmpCommunity("public")
                .setSnmpOid("1.2.2.2.2.2.22.2").setSnmpUsername("user").setSnmpAuthPasswd("auth")
                .setSnmpPrivPasswd("priv");
            when(notifyWayService.testNotifyWay(notifyWay)).thenReturn(false);
            AjaxResult result = notifyWayController.testNotifyWay(notifyWay);
            verify(notifyWayService, times(1)).testNotifyWay(any(NotifyWay.class));
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
