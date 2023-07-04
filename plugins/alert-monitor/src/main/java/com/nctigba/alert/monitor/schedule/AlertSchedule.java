/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyuebin
 * @date 2023/5/6 16:43
 * @description
 */
@Component
@Slf4j
public class AlertSchedule {
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void alertNotifySchedule() {
        List<NotifyMessage> notifyMessages = notifyMessageMapper.selectList(
                Wrappers.<NotifyMessage>lambdaQuery().eq(NotifyMessage::getStatus, 0).eq(NotifyMessage::getIsDeleted,
                        0));
        if (CollectionUtil.isEmpty(notifyMessages)) {
            return;
        }
        List<NotifyMessage> emailMessages = notifyMessages.stream().filter(
                item -> item.getMessageType().equals("email")).collect(Collectors.toList());
        List<NotifyMessage> weComMessages = notifyMessages.stream().filter(
                item -> item.getMessageType().equals("WeCom")).collect(Collectors.toList());
        List<NotifyMessage> dingTalkMessages = notifyMessages.stream().filter(
                item -> item.getMessageType().equals("DingTalk")).collect(Collectors.toList());

        emailService.send(emailMessages);
    }
}
