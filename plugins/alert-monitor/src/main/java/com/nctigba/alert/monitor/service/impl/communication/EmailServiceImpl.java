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
 *  EmailServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/communication/EmailServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.service.CommunicationService;
import com.nctigba.alert.monitor.service.NotifyMessageService;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wuyuebin
 * @date 2023/5/7 13:24
 * @description
 */
@Service
@Slf4j
public class EmailServiceImpl implements CommunicationService {
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private NotifyMessageService notifyMessageService;
    @Autowired
    private NotifyTemplateService notifyTemplateService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    private Properties applyProperties(NotifyConfigDO notifyConfigDO) {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", notifyConfigDO.getSever());
        properties.setProperty("mail.smtp.port", notifyConfigDO.getPort() + "");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.user", notifyConfigDO.getSender());
        properties.setProperty("mail.from", notifyConfigDO.getAccount());
        return properties;
    }

    public void send(List<NotifyMessageDO> notifyMessageDOList) {
        if (CollectionUtil.isEmpty(notifyMessageDOList)) {
            return;
        }
        List<NotifyMessageDO> notifyMessageDOS = notifyMessageDOList.stream().filter(
            item -> item.getMessageType().equals(getType())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(notifyMessageDOS)) {
            return;
        }
        List<NotifyConfigDO> emailConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfigDO>lambdaQuery().eq(NotifyConfigDO::getEnable, CommonConstants.ENABLE)
                .eq(NotifyConfigDO::getType, CommonConstants.EMAIL));
        if (CollectionUtil.isEmpty(emailConfigList)) {
            notifyMessageDOS.forEach(item -> item.setStatus(3).setUpdateTime(LocalDateTime.now()));
            notifyMessageService.saveOrUpdateBatch(notifyMessageDOS);
            return;
        }
        NotifyConfigDO notifyConfigDO = emailConfigList.get(0);
        Properties properties = this.applyProperties(notifyConfigDO);
        for (NotifyMessageDO notifyMessageDO : notifyMessageDOList) {
            try {
                Session session = getSession(properties, notifyConfigDO.getAccount(),
                    encryptionUtils.decrypt(notifyConfigDO.getPasswd()));
                MimeMessage mimeMessage = new MimeMessage(session);
                String[] receiverArr = notifyMessageDO.getEmail().split(",");
                InternetAddress[] addresses = Stream.of(receiverArr).map(item -> {
                    try {
                        return new InternetAddress(item);
                    } catch (AddressException e) {
                        throw new ServiceException("receiver exception:" + e.getMessage());
                    }
                }).toArray(InternetAddress[]::new);
                mimeMessage.setFrom(new InternetAddress(
                    MimeUtility.encodeWord(notifyConfigDO.getSender()) + " <" + notifyConfigDO.getAccount() + ">"));
                mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
                mimeMessage.setSubject(notifyMessageDO.getTitle());
                mimeMessage.setContent(parseContent(notifyMessageDO.getContent()), "text/html;charset=UTF-8");
                Transport.send(mimeMessage);
                notifyMessageDO.setStatus(1).setUpdateTime(LocalDateTime.now());
                notifyMessageService.saveOrUpdate(notifyMessageDO);
            } catch (MessagingException | UnsupportedEncodingException | ServiceException e) {
                notifyMessageDO.setStatus(2).setUpdateTime(LocalDateTime.now());
                notifyMessageService.saveOrUpdate(notifyMessageDO);
                log.error("the email send fail:", e);
            }
        }
    }

    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        Properties properties = this.applyProperties(notifyConfigDO);
        try {
            Session session = getSession(properties, notifyConfigDO.getAccount(),
                encryptionUtils.decrypt(notifyConfigDO.getPasswd()));
            MimeMessage mimeMessage = new MimeMessage(session);
            String[] receiverArr = notifyWayDO.getEmail().split(",");
            InternetAddress[] addresses = Stream.of(receiverArr).map(item -> {
                try {
                    return new InternetAddress(item);
                } catch (AddressException e) {
                    throw new ServiceException("receiver exception:" + e.getMessage());
                }
            }).toArray(InternetAddress[]::new);
            mimeMessage.setFrom(new InternetAddress(
                MimeUtility.encodeWord(notifyConfigDO.getSender()) + " <" + notifyConfigDO.getAccount() + ">"));
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            NotifyTemplateDO notifyTemplateDO = notifyTemplateService.getById(notifyWayDO.getNotifyTemplateId());
            mimeMessage.setSubject(notifyTemplateDO.getNotifyTitle());
            mimeMessage.setContent(parseContent(notifyTemplateDO.getNotifyContent()), "text/html;charset=UTF-8");
            Transport.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException | ServiceException e) {
            throw new ServiceException("test sentTest exception:" + e.getMessage());
        }
        return true;
    }

    @Override
    public String getType() {
        return CommonConstants.EMAIL;
    }

    private String parseContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return "<div style='white-space: pre-line;'> " + content.replaceAll(CommonConstants.LINE_SEPARATOR, "<br/>")
            + "</div>";
    }

    private Session getSession(Properties properties, String account, String passwd) {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account, passwd);
            }
        });
    }
}