/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.opengauss.plugin.alertcenter.constant.CommonConstants;
import org.opengauss.plugin.alertcenter.entity.NotifyConfig;
import org.opengauss.plugin.alertcenter.entity.NotifyMessage;
import org.opengauss.plugin.alertcenter.mapper.NotifyConfigMapper;
import org.opengauss.plugin.alertcenter.mapper.NotifyMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * @author wuyuebin
 * @date 2023/5/7 13:24
 * @description
 */
@Service
@Slf4j
public class EmailService {
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;

    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    private Properties applyProperties(NotifyConfig notifyConfig) {
        Properties properties = new Properties();
        // 邮件服务器
        properties.setProperty("mail.smtp.host", notifyConfig.getSever());
        // 端口号
        properties.setProperty("mail.smtp.port", notifyConfig.getPort() + "");
        // 需要身份验证
        properties.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.user", notifyConfig.getSender());
        properties.setProperty("mail.from", notifyConfig.getAccount());
        return properties;
    }

    public void send(NotifyMessage notifyMessage) {
        if (notifyMessage == null) {
            return;
        }
        List<NotifyConfig> emailConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getEnable, 1).eq(NotifyConfig::getType, "email"));
        if (CollectionUtil.isEmpty(emailConfigList)) {
            return;
        }
        NotifyConfig notifyConfig = emailConfigList.get(0);
        Properties properties = this.applyProperties(notifyConfig);
        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(notifyConfig.getAccount(), notifyConfig.getPasswd());
                }
            });
            MimeMessage mimeMessage = new MimeMessage(session);
            String[] receiverArr = notifyMessage.getEmail().split(",");
            InternetAddress[] addresses = Stream.of(receiverArr).map(item -> {
                try {
                    return new InternetAddress(item);
                } catch (AddressException e) {
                    throw new ServiceException("receiver exception:" + e.getMessage());
                }
            }).toArray(InternetAddress[]::new);
            mimeMessage.setFrom(new InternetAddress(
                MimeUtility.encodeWord(notifyConfig.getSender()) + " <" + notifyConfig.getAccount() + ">"));
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            mimeMessage.setSubject(notifyMessage.getTitle());
            mimeMessage.setContent(parseContent(notifyMessage.getContent()), "text/html;charset=UTF-8");
            Transport.send(mimeMessage);
            notifyMessage.setStatus(1);
            notifyMessageMapper.updateById(notifyMessage);
        } catch (MessagingException | UnsupportedEncodingException | ServiceException e) {
            notifyMessage.setStatus(2);
            notifyMessageMapper.updateById(notifyMessage);
            log.error("the email send fail:", e);
        }
    }

    public void send(List<NotifyMessage> notifyMessageList) {
        if (CollectionUtil.isEmpty(notifyMessageList)) {
            return;
        }
        List<NotifyConfig> emailConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getEnable, 1).eq(NotifyConfig::getType, "email"));
        if (CollectionUtil.isEmpty(emailConfigList)) {
            return;
        }
        NotifyConfig notifyConfig = emailConfigList.get(0);
        Properties properties = this.applyProperties(notifyConfig);
        for (NotifyMessage notifyMessage : notifyMessageList) {
            try {
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(notifyConfig.getAccount(), notifyConfig.getPasswd());
                    }
                });
                MimeMessage mimeMessage = new MimeMessage(session);
                String[] receiverArr = notifyMessage.getEmail().split(",");
                InternetAddress[] addresses = Stream.of(receiverArr).map(item -> {
                    try {
                        return new InternetAddress(item);
                    } catch (AddressException e) {
                        throw new ServiceException("receiver exception:" + e.getMessage());
                    }
                }).toArray(InternetAddress[]::new);
                mimeMessage.setFrom(new InternetAddress(
                    MimeUtility.encodeWord(notifyConfig.getSender()) + " <" + notifyConfig.getAccount() + ">"));
                mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
                mimeMessage.setSubject(notifyMessage.getTitle());
                mimeMessage.setContent(parseContent(notifyMessage.getContent()), "text/html;charset=UTF-8");
                Transport.send(mimeMessage);
                notifyMessage.setStatus(1);
                notifyMessageMapper.updateById(notifyMessage);
            } catch (MessagingException | UnsupportedEncodingException | ServiceException e) {
                notifyMessage.setStatus(2);
                notifyMessageMapper.updateById(notifyMessage);
                log.error("the email send fail:", e);
            }
        }
    }

    public void sendTest(NotifyConfig notifyConfig, String receiver, String title, String content) {
        Properties properties = this.applyProperties(notifyConfig);
        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(notifyConfig.getAccount(), notifyConfig.getPasswd());
                }
            });
            MimeMessage mimeMessage = new MimeMessage(session);
            String[] receiverArr = receiver.split(",");
            InternetAddress[] addresses = Stream.of(receiverArr).map(item -> {
                try {
                    return new InternetAddress(item);
                } catch (AddressException e) {
                    throw new ServiceException("receiver exception:" + e.getMessage());
                }
            }).toArray(InternetAddress[]::new);
            mimeMessage.setFrom(new InternetAddress(
                MimeUtility.encodeWord(notifyConfig.getSender()) + " <" + notifyConfig.getAccount() + ">"));
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            mimeMessage.setSubject(title);
            mimeMessage.setContent(parseContent(content), "text/html;charset=UTF-8");
            Transport.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException | ServiceException e) {
            throw new ServiceException("test sentTest exception:" + e.getMessage());
        }
    }

    private String parseContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return "<div style='white-space: pre-line;'> " + content.replaceAll(CommonConstants.LINE_SEPARATOR, "<br/>")
            + "</div>";
    }
}
