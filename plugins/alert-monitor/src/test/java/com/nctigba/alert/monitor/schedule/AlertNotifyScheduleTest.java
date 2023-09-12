/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.service.DingTalkService;
import com.nctigba.alert.monitor.service.EmailService;
import com.nctigba.alert.monitor.service.ThirdPartyService;
import com.nctigba.alert.monitor.service.WeComService;
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
public class AlertNotifyScheduleTest {
    @InjectMocks
    private AlertNotifySchedule alertNotifySchedule;
    @Mock
    private NotifyMessageMapper notifyMessageMapper;
    @Mock
    private EmailService emailService;
    @Mock
    private WeComService weComService;
    @Mock
    private DingTalkService dingTalkService;
    @Mock
    private ThirdPartyService thirdPartyService;

    @Test
    public void testAlertNotifyScheduleWithNull() {
        List<NotifyMessage> notifyMessages = new ArrayList<>();
        when(notifyMessageMapper.selectList(any())).thenReturn(notifyMessages);
        alertNotifySchedule.alertNotifySchedule();
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
        NotifyMessage notifyMessage4 = new NotifyMessage().setMessageType("webhook");
        notifyMessages.add(notifyMessage4);
        NotifyMessage notifyMessage5 = new NotifyMessage().setMessageType("SNMP");
        notifyMessages.add(notifyMessage5);
        when(notifyMessageMapper.selectList(any())).thenReturn(notifyMessages);
        doNothing().when(emailService).send(anyList());
        doNothing().when(weComService).send(anyList());
        doNothing().when(dingTalkService).send(anyList());
        doNothing().when(thirdPartyService).sendWebhookMsgList(anyList());
        doNothing().when(thirdPartyService).sendSnmpMsgList(anyList());
        alertNotifySchedule.alertNotifySchedule();
        verify(notifyMessageMapper, times(1)).selectList(any());
        verify(emailService, times(1)).send(anyList());
        verify(weComService, times(1)).send(anyList());
        verify(dingTalkService, times(1)).send(anyList());
        verify(thirdPartyService, times(1)).sendWebhookMsgList(anyList());
        verify(thirdPartyService, times(1)).sendSnmpMsgList(anyList());
    }
}
