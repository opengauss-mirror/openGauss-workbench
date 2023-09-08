
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
import org.apache.commons.codec.binary.Base64;
import org.opengauss.admin.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DingTalkService
 *
 * @since 2023/8/9 17:58
 */
@Service
@Slf4j
public class DingTalkService {
    @Value("${notify.dingTalk.accessTokenUrl}")
    private String accessTokenUrl;
    @Value("${notify.dingTalk.sendMsgUrl}")
    private String sendUrl;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    /**
     * send messages
     *
     * @param dingTalkMessages List<NotifyMessage>
     */
    public void send(List<NotifyMessage> dingTalkMessages) {
        if (CollectionUtil.isEmpty(dingTalkMessages)) {
            return;
        }
        List<NotifyConfig> dingConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfig>lambdaQuery().eq(NotifyConfig::getEnable, CommonConstants.ENABLE)
                .eq(NotifyConfig::getType, CommonConstants.DING_TALK));
        if (CollectionUtil.isEmpty(dingConfigList)) {
            return;
        }
        NotifyConfig notifyConfig = dingConfigList.get(0);
        String accessToken = getAccessToken(notifyConfig.getAppKey(), notifyConfig.getSecret());
        for (NotifyMessage dingMessage : dingTalkMessages) {
            boolean isSuccess = sendMsg(dingMessage, notifyConfig.getAgentId(), accessToken);
            if (isSuccess) {
                dingMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                dingMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the DingTalk send fail:{}", dingTalkMessages);
            }
            notifyMessageMapper.updateById(dingMessage);
        }
    }

    private boolean sendMsg(NotifyMessage dingMessage, String agentId, String accessToken) {
        String res = "";
        String webhook = dingMessage.getWebhook();
        if (StrUtil.isNotBlank(webhook)) {
            Long timestamp = System.currentTimeMillis();
            if (StrUtil.isNotBlank(dingMessage.getSign())) {
                String sign = encryptionSign(dingMessage.getSign(), timestamp);
                webhook += "&timestamp=" + timestamp + "&sign=" + sign;
            }
            res = sendMsgByRobot(webhook, dingMessage.getContent());
        } else {
            if (StrUtil.isBlank(accessToken)) {
                log.error("The accessToken of null");
                return false;
            }
            res = sendMsgByApp(dingMessage, agentId, accessToken);
        }
        if (StrUtil.isBlank(res) || JSONUtil.parseObj(res).getInt("errcode") != 0) {
            log.error("the message is send fail:{}", res);
            return false;
        }
        return true;
    }

    private String sendMsgByApp(NotifyMessage dingMessage, String agentId, String accessToken) {
        JSONObject params = new JSONObject();
        params.put("userid_list",
            StrUtil.isNotBlank(dingMessage.getPersonId()) ? dingMessage.getPersonId() : "");
        params.put("dept_id_list",
            StrUtil.isNotBlank(dingMessage.getDeptId()) ? dingMessage.getDeptId() : "");
        params.put("agent_id", agentId);
        JSONObject subParams = new JSONObject();
        subParams.put("msgtype", "text");
        JSONObject subSubParams = new JSONObject();
        subSubParams.put("content", dingMessage.getContent());
        subParams.put("text", subSubParams);
        params.put("msg", subParams);
        String url = sendUrl + "?access_token="
            + accessToken;
        return HttpUtil.post(url, params.toString());
    }

    private String sendMsgByRobot(String webhook, String content) {
        JSONObject params = new JSONObject();
        params.put("msgtype", "text");
        JSONObject subParams = new JSONObject();
        subParams.put("content", content);
        params.put("text", subParams);
        return HttpUtil.post(webhook, params.toString());
    }

    private String getAccessToken(String appKey, String secret) {
        String url = accessTokenUrl + "?appkey=" + appKey + "&appsecret=" + secret;
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
     * send messages test
     *
     * @param notifyConfig NotifyConfig
     * @param notifyWay NotifyWay
     * @param content String
     * @return boolean
     */
    public boolean sendTest(NotifyConfig notifyConfig, NotifyWay notifyWay, String content) {
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setContent(content);
        Integer sendWay = notifyWay.getSendWay();
        if (sendWay != null && sendWay == 1) {
            notifyMessage.setWebhook(notifyWay.getWebhook()).setSign(notifyWay.getSign());
        } else {
            notifyMessage.setPersonId(notifyWay.getPersonId()).setDeptId(notifyWay.getDeptId());
        }
        return sendMsg(notifyMessage, notifyConfig.getAgentId(), getAccessToken(notifyConfig.getAppKey(),
            notifyConfig.getSecret()));
    }

    private String encryptionSign(String sign, Long timestamp) {
        try {
            String stringToSign = timestamp + CommonConstants.LINE_SEPARATOR + sign;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sign.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            log.error("encryption failed:", e);
            throw new ServiceException("the sign encryption failed");
        }
    }
}
