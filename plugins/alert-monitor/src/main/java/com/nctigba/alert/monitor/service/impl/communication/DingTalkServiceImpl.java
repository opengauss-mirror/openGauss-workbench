
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
 *  DingTalkServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/communication/DingTalkServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.mapper.NotifyConfigMapper;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.service.CommunicationService;
import com.nctigba.alert.monitor.service.NotifyTemplateService;
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
import java.util.stream.Collectors;

/**
 * DingTalkService
 *
 * @since 2023/8/9 17:58
 */
@Service
@Slf4j
public class DingTalkServiceImpl implements CommunicationService {
    @Value("${notify.dingTalk.accessTokenUrl}")
    private String accessTokenUrl;
    @Value("${notify.dingTalk.sendMsgUrl}")
    private String sendUrl;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    private NotifyTemplateService notifyTemplateService;

    /**
     * send messages
     *
     * @param notifyMessageDOList List<NotifyMessage>
     */
    public void send(List<NotifyMessageDO> notifyMessageDOList) {
        if (CollectionUtil.isEmpty(notifyMessageDOList)) {
            return;
        }
        List<NotifyMessageDO> notifyMessageDOS = notifyMessageDOList.stream().filter(
            item -> item.getMessageType().equals(getType())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(notifyMessageDOS)) {
            return;
        }
        List<NotifyConfigDO> dingConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfigDO>lambdaQuery().eq(NotifyConfigDO::getEnable, CommonConstants.ENABLE)
                .eq(NotifyConfigDO::getType, CommonConstants.DING_TALK));
        if (CollectionUtil.isEmpty(dingConfigList)) {
            return;
        }
        NotifyConfigDO notifyConfigDO = dingConfigList.get(0);
        String accessToken = getAccessToken(notifyConfigDO.getAppKey(), notifyConfigDO.getSecret());
        for (NotifyMessageDO dingMessage : notifyMessageDOS) {
            boolean isSuccess = sendMsg(dingMessage, notifyConfigDO.getAgentId(), accessToken);
            if (isSuccess) {
                dingMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                dingMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the DingTalk send fail:{}", dingMessage);
            }
            notifyMessageMapper.updateById(dingMessage);
        }
    }

    /**
     * send messages test
     *
     * @param notifyConfigDO notifyConfig
     * @param notifyWayDO notifyWay
     * @return boolean
     */
    @Override
    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        NotifyTemplateDO notifyTemplateDO = notifyTemplateService.getById(notifyWayDO.getNotifyTemplateId());
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setContent(notifyTemplateDO.getNotifyContent());
        Integer sendWay = notifyWayDO.getSendWay();
        if (sendWay != null && sendWay == 1) {
            notifyMessageDO.setWebhook(notifyWayDO.getWebhook()).setSign(notifyWayDO.getSign());
        } else {
            notifyMessageDO.setPersonId(notifyWayDO.getPersonId()).setDeptId(notifyWayDO.getDeptId());
        }
        return sendMsg(notifyMessageDO, notifyConfigDO.getAgentId(), getAccessToken(notifyConfigDO.getAppKey(),
            notifyConfigDO.getSecret()));
    }

    @Override
    public String getType() {
        return CommonConstants.DING_TALK;
    }

    private boolean sendMsg(NotifyMessageDO dingMessage, String agentId, String accessToken) {
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

    private String sendMsgByApp(NotifyMessageDO dingMessage, String agentId, String accessToken) {
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
