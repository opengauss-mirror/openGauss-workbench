/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.snmp4j.mp.SnmpConstants;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ThirdPartyServiceTest
 *
 * @since 2023/8/24 19:27
 */
@RunWith(SpringRunner.class)
public class ThirdPartyServiceTest {
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @Mock
    private NotifyTemplateMapper templateMapper;
    @InjectMocks
    private ThirdPartyService thirdPartyService;

    @Test
    public void testSendNullWebhookMsgList() {
        List<NotifyMessage> messages = new ArrayList<>();
        thirdPartyService.sendWebhookMsgList(messages);
    }

    @Test
    public void testSendWebhookMsgList() {
        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            HttpRequest post = mock(HttpRequest.class);
            mockedStatic.when(() -> HttpRequest.post(anyString())).thenReturn(post);
            when(post.header(anyString(), anyString())).thenReturn(post);
            when(post.body(anyString())).thenReturn(post);
            HttpResponse response = mock(HttpResponse.class);
            when(post.execute()).thenReturn(response);
            when(response.body()).thenReturn("").thenReturn("{\"errCode\": 0}")
                .thenReturn("{\"errCode\": 500}").thenReturn("");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyMessage> messages = new ArrayList<>();
            NotifyMessage message1 = new NotifyMessage().setMessageType(CommonConstants.WEBHOOK).setTitle("title")
                .setContent("content").setWebhook("webhook").setId(1L);
            messages.add(message1);
            NotifyMessage message2 = new NotifyMessage().setMessageType(CommonConstants.WEBHOOK).setTitle("title")
                .setContent("content").setWebhook("webhook").setWebhookInfo("{\"header\":{\"a\":\"b\"},"
                    + "\"params\":{\"k\":\"v\"},\"body\":\"body\",\"resultCode\":{\"errCode\": 0}}").setId(2L);
            ;
            messages.add(message2);
            NotifyMessage message3 = new NotifyMessage().setMessageType(CommonConstants.WEBHOOK).setTitle("title")
                .setContent("content").setWebhook("webhook?test=xxx").setWebhookInfo("{\"header\":{\"a\":\"b\"},"
                    + "\"params\":{\"k\":\"v\"},\"body\":\"body\",\"resultCode\":{\"errCode\": 0}}").setId(3L);
            ;
            messages.add(message3);
            NotifyMessage message4 = new NotifyMessage().setMessageType(CommonConstants.WEBHOOK).setTitle("title")
                .setContent("content").setWebhook("webhook").setWebhookInfo("{\"header\":{\"a\":\"b\"},"
                    + "\"params\":{\"k\":\"v\"},\"body\":\"body\",\"resultCode\":{\"errCode\": 0}}").setId(2L);
            ;
            messages.add(message4);
            thirdPartyService.sendWebhookMsgList(messages);
            verify(notifyMessageMapper, times(4)).updateById(any());
        }
    }

    @Test
    public void testTestWebhookByNotifyWay() {
        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            HttpRequest post = mock(HttpRequest.class);
            mockedStatic.when(() -> HttpRequest.post(anyString())).thenReturn(post);
            when(post.header(anyString(), anyString())).thenReturn(post);
            when(post.body(anyString())).thenReturn(post);
            HttpResponse response = mock(HttpResponse.class);
            when(post.execute()).thenReturn(response);
            when(response.body()).thenReturn("{\"errCode\": 0}");
            NotifyWay notifyWay = new NotifyWay().setWebhook("webhook").setParams("{\"k\":\"v\"}")
                .setHeader("{\"a\":\"b\"}").setBody("body").setResultCode("{\"errCode\": 0}").setNotifyTemplateId(1L);
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content").setNotifyTitle("title");
            when(templateMapper.selectById(1L)).thenReturn(notifyTemplate);
            thirdPartyService.testWebhookByNotifyWay(notifyWay);
        }
    }

    @Test
    public void testTestWebhookByNotifyWayWithNullParam() {
        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            HttpRequest post = mock(HttpRequest.class);
            mockedStatic.when(() -> HttpRequest.post(anyString())).thenReturn(post);
            when(post.header(anyString(), anyString())).thenReturn(post);
            when(post.body(anyString())).thenReturn(post);
            HttpResponse response = mock(HttpResponse.class);
            when(post.execute()).thenReturn(response);
            when(response.body()).thenReturn("");
            NotifyWay notifyWay = new NotifyWay().setWebhook("webhook").setNotifyTemplateId(1L);
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content");
            when(templateMapper.selectById(1L)).thenReturn(notifyTemplate);
            thirdPartyService.testWebhookByNotifyWay(notifyWay);
        }
    }

    @Test
    public void testTestWebhookWithReturnErr() {
        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content").setNotifyTitle("title");
            when(templateMapper.selectById(1L)).thenReturn(notifyTemplate);
            HttpRequest post = mock(HttpRequest.class);
            mockedStatic.when(() -> HttpRequest.post(anyString())).thenReturn(post);
            when(post.header(anyString(), anyString())).thenReturn(post);
            when(post.body(anyString())).thenReturn(post);
            HttpResponse response = mock(HttpResponse.class);
            when(post.execute()).thenReturn(response);
            when(response.body()).thenReturn("{\"errCode\": 500}");
            NotifyWay notifyWay = new NotifyWay().setWebhook("webhook?test=xxx").setParams("{\"k\":\"v\"}")
                .setHeader("{\"a\":\"b\"}").setBody("body").setResultCode("{\"errCode\": 0}").setNotifyTemplateId(1L);
            thirdPartyService.testWebhookByNotifyWay(notifyWay);
        }
    }

    @Test
    public void testTestWebhook() {
        try (MockedStatic<HttpRequest> mockedStatic = mockStatic(HttpRequest.class)) {
            NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content").setNotifyTitle("title");
            when(templateMapper.selectById(1L)).thenReturn(notifyTemplate);
            HttpRequest post = mock(HttpRequest.class);
            mockedStatic.when(() -> HttpRequest.post(anyString())).thenReturn(post);
            when(post.header(anyString(), anyString())).thenReturn(post);
            when(post.body(anyString())).thenReturn(post);
            HttpResponse response = mock(HttpResponse.class);
            when(post.execute()).thenReturn(response);
            when(response.body()).thenReturn("");
            NotifyWay notifyWay = new NotifyWay().setWebhook("webhook?test=xxx").setParams("{\"k\":\"v\"}")
                .setHeader("{\"a\":\"b\"}").setBody("body").setResultCode("{\"errCode\": 0}").setNotifyTemplateId(1L);
            thirdPartyService.testWebhookByNotifyWay(notifyWay);
        }
    }

    @Test
    public void testTestSnmpV1ByNotifyWay() {
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content");
        when(templateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        NotifyWay notifyWay = new NotifyWay().setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpOid(".1.2.2.2.2.2.2.2")
            .setSnmpCommunity("community").setSnmpVersion(SnmpConstants.version1).setNotifyTemplateId(1L);
        thirdPartyService.testSnmpByNotifyWay(notifyWay);
        verify(templateMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testTestSnmpV2cByNotifyWay() {
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content");
        when(templateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        NotifyWay notifyWay = new NotifyWay().setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpOid(".1.2.2.2.2.2.2.2")
            .setSnmpCommunity("community").setSnmpVersion(SnmpConstants.version2c).setNotifyTemplateId(1L);
        thirdPartyService.testSnmpByNotifyWay(notifyWay);
        verify(templateMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testTestSnmpV3ByNotifyWay() {
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content");
        when(templateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        NotifyWay notifyWay = new NotifyWay().setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpOid(".1.2.2.2.2.2.2.2")
            .setSnmpCommunity("community").setSnmpVersion(SnmpConstants.version3).setNotifyTemplateId(1L)
            .setSnmpUsername("user").setSnmpAuthPasswd("admin123").setSnmpPrivPasswd("admin1234");
        thirdPartyService.testSnmpByNotifyWay(notifyWay);
        verify(templateMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testTestSnmpByNotifyWay() {
        NotifyTemplate notifyTemplate = new NotifyTemplate().setNotifyContent("content");
        when(templateMapper.selectById(anyLong())).thenReturn(notifyTemplate);
        NotifyWay notifyWay = new NotifyWay().setSnmpIp("127.0.0.1").setSnmpPort("162").setSnmpOid(".1.2.2.2.2.2.2.2")
            .setSnmpCommunity("community").setSnmpVersion(4).setNotifyTemplateId(1L)
            .setSnmpUsername("user").setSnmpAuthPasswd("admin123").setSnmpPrivPasswd("admin1234");
        thirdPartyService.testSnmpByNotifyWay(notifyWay);
        verify(templateMapper, times(1)).selectById(anyLong());
    }

    @Test
    public void testSendNullSnmpMsgList() {
        List<NotifyMessage> snmpMessages = new ArrayList<>();
        thirdPartyService.sendSnmpMsgList(snmpMessages);
    }

    @Test
    public void testSendSnmpMsgList() {
        when(notifyMessageMapper.updateById(any())).thenReturn(1);
        List<NotifyMessage> snmpMessages = new ArrayList<>();
        NotifyMessage notifyMessage1 = new NotifyMessage().setSnmpInfo("");
        snmpMessages.add(notifyMessage1);
        NotifyMessage notifyMessage2 = new NotifyMessage().setContent("content")
            .setSnmpInfo("{\"snmpCommunity\":\"community\",\"isDeleted\":0,\"snmpVersion\":0,"
                + "\"snmpOid\":\".1.2.2.2.2.2.2.2\",\"snmpPort\":\"162\",\"snmpIp\":\"127.0.0.1\"}");
        snmpMessages.add(notifyMessage2);
        NotifyMessage notifyMessage3 = new NotifyMessage().setContent("content")
            .setSnmpInfo("{\"snmpCommunity\":\"community\",\"isDeleted\":0,\"snmpVersion\":1,"
                + "\"snmpOid\":\".1.2.2.2.2.2.2.2\",\"snmpPort\":\"162\",\"snmpIp\":\"127.0.0.1\"}");
        snmpMessages.add(notifyMessage3);
        NotifyMessage notifyMessage4 = new NotifyMessage().setContent("content")
            .setSnmpInfo("{\"snmpCommunity\":\"community\",\"snmpPrivPasswd\":\"admin1234\",\"isDeleted\":0,"
                + "\"snmpVersion\":3,\"snmpOid\":\".1.2.2.2.2.2.2.2\",\"snmpAuthPasswd\":\"admin123\","
                + "\"snmpPort\":\"162\",\"snmpIp\":\"127.0.0.1\",\"snmpUsername\":\"user\"}");
        snmpMessages.add(notifyMessage4);
        NotifyMessage notifyMessage5 = new NotifyMessage().setContent("content")
            .setSnmpInfo("{\"snmpCommunity\":\"community\",\"snmpPrivPasswd\":\"admin1234\",\"isDeleted\":0,"
                + "\"snmpVersion\":4,\"snmpOid\":\".1.2.2.2.2.2.2.2\",\"snmpAuthPasswd\":\"admin123\","
                + "\"snmpPort\":\"162\",\"snmpIp\":\"127.0.0.1\",\"snmpUsername\":\"user\"}");
        snmpMessages.add(notifyMessage5);
        thirdPartyService.sendSnmpMsgList(snmpMessages);
        verify(notifyMessageMapper, times(4)).updateById(any());
    }
}
