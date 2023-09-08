/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.service.DingTalkService;
import com.nctigba.alert.monitor.service.EmailService;
import com.nctigba.alert.monitor.service.ThirdPartyService;
import com.nctigba.alert.monitor.service.WeComService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AlertNotifySchedule
 *
 * @author wuyuebin
 * @since 2023/5/6 16:43
 */
@Component
@Slf4j
public class AlertNotifySchedule {
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WeComService weComService;

    @Autowired
    private DingTalkService dingTalkService;

    @Autowired
    private ThirdPartyService thirdPartyService;

    /**
     * notify schedule
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void alertNotifySchedule() {
        List<NotifyMessage> notifyMessages = notifyMessageMapper.selectList(
            Wrappers.<NotifyMessage>lambdaQuery().eq(NotifyMessage::getStatus, 0).eq(NotifyMessage::getIsDeleted, 0));
        if (CollectionUtil.isEmpty(notifyMessages)) {
            return;
        }
        List<NotifyMessage> emailMessages = new ArrayList<>();
        List<NotifyMessage> weComMessages = new ArrayList<>();
        List<NotifyMessage> dingTalkMessages = new ArrayList<>();
        List<NotifyMessage> webhookMsgs = new ArrayList<>();
        List<NotifyMessage> snmpMsgs = new ArrayList<>();
        for (NotifyMessage notifyMessage : notifyMessages) {
            String messageType = notifyMessage.getMessageType();
            if (StrUtil.isBlank(messageType)) {
                continue;
            }
            if (messageType.equals(CommonConstants.EMAIL)) {
                emailMessages.add(notifyMessage);
            } else if (messageType.equals(CommonConstants.WE_COM)) {
                weComMessages.add(notifyMessage);
            } else if (messageType.equals(CommonConstants.DING_TALK)) {
                dingTalkMessages.add(notifyMessage);
            } else if (messageType.equals(CommonConstants.WEBHOOK)) {
                webhookMsgs.add(notifyMessage);
            } else {
                snmpMsgs.add(notifyMessage);
            }
        }
        emailService.send(emailMessages);
        weComService.send(weComMessages);
        dingTalkService.send(dingTalkMessages);
        thirdPartyService.sendWebhookMsgList(webhookMsgs);
        thirdPartyService.sendSnmpMsgList(snmpMsgs);
    }
}
