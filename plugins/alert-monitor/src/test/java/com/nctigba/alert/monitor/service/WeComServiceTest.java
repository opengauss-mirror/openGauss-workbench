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
 * WeComServiceTest
 *
 * @since 2023/8/24 17:51
 */
@RunWith(SpringRunner.class)
public class WeComServiceTest {
    @Mock
    private NotifyConfigMapper notifyConfigMapper;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @InjectMocks
    private WeComService weComService;

    @Test
    public void testSendNull() {
        List<NotifyMessage> weComMessages = new ArrayList<>();
        weComService.send(weComMessages);
    }

    @Test
    public void testSendWithoutNotifyConfig() {
        List<NotifyMessage> weComMessages = new ArrayList<>();
        weComMessages.add(new NotifyMessage());
        List<NotifyConfig> weComConfigList = new ArrayList<>();
        when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
        weComService.send(weComMessages);
        verify(notifyConfigMapper, times(1)).selectList(any());
    }

    @Test
    public void testSendWithWebhook() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            weComService.send(weComMessages);
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendWithApp() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString()))
                .thenReturn("{\"errcode\": 0, \"access_token\": \"access_token\"}");
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            weComService.send(weComMessages);
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
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setContent("content").setDeptId("").setPersonId(""));
            weComService.send(weComMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyToken1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("");
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            weComService.send(weComMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyToken2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("{\"errcode\": 500}");
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setContent("content").setDeptId("deptId").setPersonId("personId"));
            weComService.send(weComMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyResult1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            weComService.send(weComMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendThrowExceptionByEmptyResult2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 500}");
            when(notifyMessageMapper.updateById(any())).thenReturn(1);
            List<NotifyConfig> weComConfigList = new ArrayList<>();
            weComConfigList.add(new NotifyConfig().setAppKey("appKey").setSecret("secret"));
            when(notifyConfigMapper.selectList(any())).thenReturn(weComConfigList);
            List<NotifyMessage> weComMessages = new ArrayList<>();
            weComMessages.add(new NotifyMessage().setWebhook("webhook").setContent("content"));
            weComService.send(weComMessages);
            verify(notifyConfigMapper, times(1)).selectList(any());
            verify(notifyMessageMapper, times(1)).updateById(any());
        }
    }

    @Test
    public void testSendTestByWebhook() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 0}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            weComService.sendTest(notifyConfig, notifyWay, "content");
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
            weComService.sendTest(notifyConfig, notifyWay, "content");
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
            weComService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestByEmptyToken() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString())).thenReturn("");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay =
                new NotifyWay().setSendWay(CommonConstants.APP_SEND_WAY).setPersonId("").setDeptId("");
            weComService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestReturnErr() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("{\"errcode\": 500}");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            weComService.sendTest(notifyConfig, notifyWay, "content");
        }
    }

    @Test
    public void testSendTestReturnEmpty() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            NotifyConfig notifyConfig = new NotifyConfig().setAppKey("appKey").setSecret("secret");
            NotifyWay notifyWay = new NotifyWay().setSendWay(CommonConstants.ROBOT_SEND_WAY).setWebhook("webhook");
            weComService.sendTest(notifyConfig, notifyWay, "content");
        }
    }
}
