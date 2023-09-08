
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.NotifyConfig;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WeComService
 *
 * @since 2023/8/9 16:38
 */
@Service
@Slf4j
public class WeComService {
    @Value("${notify.weCom.accessTokenUrl}")
    private String accessTokenUrl;
    @Value("${notify.weCom.sendMsgUrl}")
    private String sendUrl;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    /**
     * send messages
     *
     * @param weComMessages List<NotifyMessage>
     */
    public void send(List<NotifyMessage> weComMessages) {
        if (CollectionUtil.isEmpty(weComMessages)) {
            return;
        }
        List<NotifyConfig> weComConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getEnable, CommonConstants.ENABLE)
                .eq(NotifyConfig::getType, CommonConstants.WE_COM));
        if (CollectionUtil.isEmpty(weComConfigList)) {
            return;
        }
        NotifyConfig notifyConfig = weComConfigList.get(0);
        String accessToken = getAccessToken(notifyConfig.getAppKey(), notifyConfig.getSecret());
        for (NotifyMessage weComMessage : weComMessages) {
            boolean isSuccess = sendMsg(weComMessage, accessToken, notifyConfig.getAgentId());
            if (isSuccess) {
                weComMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                weComMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the WeCom send fail:{}", weComMessage);
            }
            notifyMessageMapper.updateById(weComMessage);
        }
    }

    private String getAccessToken(String corpId, String secret) {
        String url = accessTokenUrl + "?corpid=" + corpId + "&corpsecret=" + secret;
        String result = HttpUtil.get(url);
        if (StrUtil.isBlank(result)) {
            return "";
        }
        JSONObject resJson = JSONUtil.parseObj(result);
        if (resJson.getInt("errcode") != 0) {
            return "";
        }
        return resJson.getStr("access_token");
    }

    /**
     * send message test
     *
     * @param notifyConfig NotifyConfig
     * @param notifyWay NotifyWay
     * @param notifyContent String
     * @return boolean
     */
    public boolean sendTest(NotifyConfig notifyConfig, NotifyWay notifyWay, String notifyContent) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setWebhook(notifyWay.getWebhook()).setContent(notifyContent).setPersonId(notifyWay.getPersonId())
            .setDeptId(notifyWay.getDeptId());
        return sendMsg(notifyMessage, getAccessToken(notifyConfig.getAppKey(), notifyConfig.getSecret()),
            notifyConfig.getAgentId());
    }

    private boolean sendMsg(NotifyMessage notifyMessage, String accessToken, String agentId) {
        JSONObject params = new JSONObject();
        params.put("msgtype", "text");
        JSONObject subParams = new JSONObject();
        subParams.put("content", notifyMessage.getContent());
        params.append("text", subParams);
        String res = "";
        String webhook = notifyMessage.getWebhook();
        if (StrUtil.isNotBlank(webhook)) {
            res = HttpUtil.post(webhook, params.toString());
        } else {
            if (StrUtil.isBlank(accessToken)) {
                log.error("The accessToken of  WeCom is null");
                return false;
            }
            params.put("touser",
                StrUtil.isNotBlank(notifyMessage.getPersonId()) ? notifyMessage.getPersonId() : "");
            params.put("toparty",
                StrUtil.isNotBlank(notifyMessage.getDeptId()) ? notifyMessage.getDeptId() : "");
            params.put("agentid", agentId);
            webhook = sendUrl + "?access_token=" + accessToken;
            res = HttpUtil.post(webhook, params.toString());
        }
        if (StrUtil.isBlank(res) || JSONUtil.parseObj(res).getInt("errcode") != 0) {
            log.error("the message is send fail:{}", res);
            return false;
        }
        return true;
    }
}
