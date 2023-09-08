/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.http.HttpUtil;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DingTalkServiceTest
 *
 * @since 2023/8/24 19:14
 */
@RunWith(SpringRunner.class)
public class DingTalkServiceTest {
    @Mock
    private NotifyConfigMapper notifyConfigMapper;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @InjectMocks
    private DingTalkService dingTalkService;

    @Test
    public void testSendNull() {
        List<NotifyMessage> dingTalkMessages = new ArrayList<>();
        dingTalkService.send(dingTalkMessages);
    }

    @Test
    public void testSendWithoutNotifyConfig() {
        List<NotifyMessage> dingTalkMessages = new ArrayList<>();
        dingTalkMessages.add(new NotifyMessage());
        List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
        when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
        dingTalkService.send(dingTalkMessages);
        verify(notifyConfigMapper, times(1)).selectList(any());
    }

    @Test
    public void testSendWithWebhookNotSign() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendWithWebhookHasSign() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content").setSign("sign"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendWithApp() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"errcode\": 0, \"access_token\": \"access_token\"}");
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendNoOneWithApp() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"errcode\": 0, \"access_token\": \"access_token\"}");
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setContent("content").setDeptId("").setPersonId(""));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendByEmptyToken1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("");
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testSendByEmptyToken2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("{\"errcode\": 500}");
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyResult1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyResult2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 500}");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> dingTalkConfigList = new ArrayList<>();
            dingTalkConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(dingTalkConfigList);
            List<NotifyMessage> dingTalkMessages = new ArrayList<>();
            dingTalkMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            dingTalkService.send(dingTalkMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendTestByWebhookNoSign() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestByWebhookSign() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook")
                .setSign("sign");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestByApp() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"errcode\": 0, \"access_token\": \"access_token\"}");
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay =
                new NotifyWay().setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("1").setDeptId("1");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendNoOneTestByApp() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"errcode\": 0, \"access_token\": \"access_token\"}");
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay =
                new NotifyWay().setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("").setDeptId("");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestByEmptyToken() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay =
                new NotifyWay().setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("").setDeptId("");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestFailReault1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 500}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestFailResult2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            dingTalkService.sendTest(notifyConfig, notifyWay, "content");
        }
    }
}
