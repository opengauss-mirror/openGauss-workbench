/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * EmailService function test
 *
 * @since 2023/7/5 16:29
 */
@RunWith(SpringRunner.class)
public class EmailServiceTest {

    @Mock
    private NotifyConfigMapper notifyConfigMapper;

    @Mock
    private NotifyMessageMapper notifyMessageMapper;

    @InjectMocks
    private EmailService emailService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSendWithNullNotifyMessage() {
        // Call the method to be tested with a null NotifyMessage
        NotifyMessage notifyMessage = null;
        emailService.send(notifyMessage);

        // Verify that the method has returned without doing anything
        verifyZeroInteractions(notifyConfigMapper, notifyMessageMapper);
    }

    @Test
    public void testSendWithNoEmailConfig() {
        // Call the method to be tested with a null NotifyMessage
        NotifyMessage notifyMessage = new NotifyMessage();
        List<NotifyConfig> emailConfigList = new ArrayList<>();
        when(notifyConfigMapper.selectList(any())).thenReturn(emailConfigList);
        emailService.send(notifyMessage);
        verify(notifyConfigMapper, times(1)).selectList(any());
    }

    @Test
    public void testSendWithValidReceiver() {
        // Set up the mock NotifyConfigMapper to return a valid NotifyConfig
        NotifyConfig notifyConfig = new NotifyConfig();
        notifyConfig.setSever("smtp.example.com");
        notifyConfig.setPort(25);
        notifyConfig.setType("email");
        notifyConfig.setSender("sender@example.com");
        notifyConfig.setAccount("account@example.com");
        notifyConfig.setPasswd("passwd");
        when(notifyConfigMapper.selectList(any())).thenReturn(Collections.singletonList(notifyConfig));

        // Set up the mock NotifyMessageMapper to return a valid NotifyMessage
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setEmail("");
        notifyMessage.setTitle("Test Email");
        notifyMessage.setContent("<html><body><h1>Test Email</h1><p>This is a test email.</p></body></html>");
        when(notifyMessageMapper.updateById(any())).thenReturn(1);

        // Call the method to be tested with the valid NotifyMessage
        emailService.send(notifyMessage);

        // Verify that the method has interacted with the mock dependencies
        Mockito.verify(notifyConfigMapper, times(1)).selectList(any());
        Mockito.verify(notifyMessageMapper, times(1)).updateById(notifyMessage);
    }

    @Test
    public void testSend() {
        try (MockedStatic<Transport> mockedStatic = mockStatic(Transport.class)) {
            NotifyConfig notifyConfig = new NotifyConfig();
            notifyConfig.setSever("smtp.example.com");
            notifyConfig.setPort(25);
            notifyConfig.setType("email");
            notifyConfig.setEmail("account@example.com");
            notifyConfig.setSender("sender@example.com");
            notifyConfig.setAccount("account@example.com");
            notifyConfig.setPasswd("passwd");
            when(notifyConfigMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(notifyConfig));

            // Set up the mock NotifyMessageMapper to return a valid NotifyMessage
            NotifyMessage notifyMessage = new NotifyMessage();
            notifyMessage.setEmail("test@example.com");
            notifyMessage.setTitle("Test Email");
            notifyMessage.setContent("<html><body><h1>Test Email</h1><p>This is a test email.</p></body></html>");

            mockedStatic.when(() -> Transport.send(any())).thenAnswer(invocationOnMock -> null);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);

            // Call the method to be tested with the valid NotifyMessage
            emailService.send(notifyMessage);

            // Verify that the method has interacted with the mock dependencies
            Mockito.verify(notifyConfigMapper, Mockito.times(1)).selectList(any());
            Mockito.verify(notifyMessageMapper, Mockito.times(1)).updateById(notifyMessage);
        }
    }

    @Test
    public void testSendListWithNullNotifyMessageList() {
        List<NotifyMessage> notifyMessageList = new ArrayList<>();
        emailService.send(notifyMessageList);
        verifyZeroInteractions(notifyConfigMapper, notifyMessageMapper);
    }

    @Test
    public void testSendListWithNoEmailConfig() {
        List<NotifyMessage> notifyMessageList = new ArrayList<>();
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessageList.add(notifyMessage);
        List<NotifyConfig> emailConfigList = new ArrayList<>();
        when(notifyConfigMapper.selectList(any())).thenReturn(emailConfigList);
        emailService.send(notifyMessageList);
        verify(notifyConfigMapper, times(1)).selectList(any());
    }

    @Test
    public void testSendListWithValidReceiver() {
        NotifyConfig notifyConfig = new NotifyConfig();
        notifyConfig.setSever("smtp.example.com");
        notifyConfig.setPort(25);
        notifyConfig.setType("email");
        notifyConfig.setSender("sender@example.com");
        notifyConfig.setAccount("account@example.com");
        notifyConfig.setPasswd("passwd");
        when(notifyConfigMapper.selectList(any())).thenReturn(Collections.singletonList(notifyConfig));

        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setEmail("");
        notifyMessage.setTitle("Test Email");
        notifyMessage.setContent("<html><body><h1>Test Email</h1><p>This is a test email.</p></body></html>");
        List<NotifyMessage> notifyMessageList = new ArrayList<>();
        notifyMessageList.add(notifyMessage);
        when(notifyMessageMapper.updateById(any())).thenReturn(1);

        emailService.send(notifyMessageList);

        Mockito.verify(notifyConfigMapper, times(1)).selectList(any());
        Mockito.verify(notifyMessageMapper, times(1)).updateById(notifyMessage);
    }

    @Test
    public void testSendList() {
        try (MockedStatic<Transport> mockedStatic = mockStatic(Transport.class)) {
            // Set up the mock NotifyMessageMapper to return a valid NotifyMessage
            NotifyMessage notifyMessage = new NotifyMessage();
            notifyMessage.setEmail("test@example.com");
            notifyMessage.setTitle("Test Email");
            notifyMessage.setContent("<html><body><h1>Test Email</h1><p>This is a test email.</p></body></html>");
            List<NotifyMessage> notifyMessageList = new ArrayList<>();
            notifyMessageList.add(notifyMessage);

            NotifyConfig notifyConfig = new NotifyConfig();
            notifyConfig.setSever("smtp.example.com");
            notifyConfig.setPort(25);
            notifyConfig.setType("email");
            notifyConfig.setSender("sender@example.com");
            notifyConfig.setAccount("account@example.com");
            notifyConfig.setPasswd("passwd");
            List<NotifyConfig> configList = new ArrayList<>();
            configList.add(notifyConfig);
            when(notifyConfigMapper.selectList(any())).thenReturn(configList);

            mockedStatic.when(() -> Transport.send(any())).thenAnswer(invocationOnMock -> null);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);

            // Call the method to be tested with the valid NotifyMessage
            emailService.send(notifyMessageList);

            // Verify that the method has interacted with the mock dependencies
            Mockito.verify(notifyConfigMapper, Mockito.times(1)).selectList(any());
            Mockito.verify(notifyMessageMapper, Mockito.times(1)).updateById(notifyMessage);
        }
    }

    @Test
    public void testSendList2() {
        try (MockedStatic<Transport> mockedStatic = mockStatic(Transport.class)) {
            // Set up the mock NotifyMessageMapper to return a valid NotifyMessage
            NotifyMessage notifyMessage = new NotifyMessage();
            notifyMessage.setEmail("test@example.com");
            notifyMessage.setTitle("Test Email");
            notifyMessage.setContent("");
            List<NotifyMessage> notifyMessageList = new ArrayList<>();
            notifyMessageList.add(notifyMessage);

            NotifyConfig notifyConfig = new NotifyConfig();
            notifyConfig.setSever("smtp.example.com");
            notifyConfig.setPort(25);
            notifyConfig.setType("email");
            notifyConfig.setSender("sender@example.com");
            notifyConfig.setAccount("account@example.com");
            notifyConfig.setPasswd("passwd");
            List<NotifyConfig> configList = new ArrayList<>();
            configList.add(notifyConfig);
            when(notifyConfigMapper.selectList(any())).thenReturn(configList);

            mockedStatic.when(() -> Transport.send(any())).thenAnswer(invocationOnMock -> null);
            when(notifyMessageMapper.updateById(any())).thenReturn(1);

            // Call the method to be tested with the valid NotifyMessage
            emailService.send(notifyMessageList);

            // Verify that the method has interacted with the mock dependencies
            Mockito.verify(notifyConfigMapper, Mockito.times(1)).selectList(any());
            Mockito.verify(notifyMessageMapper, Mockito.times(1)).updateById(notifyMessage);
        }
    }

    @Test(expected = ServiceException.class)
    public void testSendTestWithValidReceiver() {
        // Set up the mock NotifyConfigMapper to return a valid NotifyConfig
        NotifyConfig notifyConfig = new NotifyConfig();
        notifyConfig.setSever("smtp.example.com");
        notifyConfig.setPort(25);
        notifyConfig.setType("email");
        notifyConfig.setSender("sender@example.com");
        notifyConfig.setAccount("account@example.com");
        notifyConfig.setPasswd("passwd");

        // Call the method to be tested with valid parameters
        emailService.sendTest(notifyConfig, "", "Test Email", "<html><body><h1>Test "
            + "Email</h1><p>This is a test email.</p></body></html>");
    }

    @Test
    public void testSendTest() {
        try (MockedStatic<Transport> mockedStatic = mockStatic(Transport.class)) {
            // Call the method to be tested with an invalid receiver email address
            NotifyConfig notifyConfig = new NotifyConfig();
            notifyConfig.setSever("smtp.example.com");
            notifyConfig.setPort(25);
            notifyConfig.setType("email");
            notifyConfig.setSender("sender@example.com");
            notifyConfig.setAccount("account");
            notifyConfig.setPasswd("passwd");
            mockedStatic.when(() -> Transport.send(any(MimeMessage.class))).thenAnswer(invocationOnMock -> null);
            emailService.sendTest(notifyConfig, "test@example.com", "Test Email",
                "<html><body><h1>Test Email</h1><p>This is a test email.</p></body></html>");
        }
    }
}
