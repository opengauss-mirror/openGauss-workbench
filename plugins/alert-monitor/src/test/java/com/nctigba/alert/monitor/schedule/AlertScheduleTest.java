/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.service.EmailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test AlertSchedule
 *
 * @since 2023/7/16 12:52
 */
@RunWith(SpringRunner.class)
public class AlertScheduleTest {
    @InjectMocks
    private AlertSchedule alertSchedule;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @Mock
    private EmailService emailService;

    @Test
    public void testAlertNotifyScheduleWithNull() {
        List<NotifyMessage> notifyMessages = new ArrayList<>();
        when(notifyMessageMapper.selectList(any())).thenReturn(notifyMessages);
        alertSchedule.alertNotifySchedule();
        verify(notifyMessageMapper, times(1)).selectList(any());
    }

    @Test
    public void testAlertNotifySchedule() {
        List<NotifyMessage> notifyMessages = new ArrayList<>();
        NotifyMessage notifyMessage1 = new NotifyMessage().setMessageType("email");
        notifyMessages.add(notifyMessage1);
        NotifyMessage notifyMessage2 = new NotifyMessage().setMessageType("WeCom");
        notifyMessages.add(notifyMessage2);
        NotifyMessage notifyMessage3 = new NotifyMessage().setMessageType("DingTalk");
        notifyMessages.add(notifyMessage3);
        when(notifyMessageMapper.selectList(any())).thenReturn(notifyMessages);
        doNothing().when(emailService).send(anyList());
        alertSchedule.alertNotifySchedule();
        verify(notifyMessageMapper, times(1)).selectList(any());
        verify(emailService, times(1)).send(anyList());
    }
}
