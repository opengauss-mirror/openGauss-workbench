
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
 *  WeComServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/communication/WeComServiceImpl.java
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
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
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
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WeComService
 *
 * @since 2023/8/9 16:38
 */
@Service
@Slf4j
public class WeComServiceImpl implements CommunicationService {
    @Value("${notify.weCom.accessTokenUrl}")
    private String accessTokenUrl;
    @Value("${notify.weCom.sendMsgUrl}")
    private String sendUrl;
    @Autowired
    private NotifyConfigMapper notifyConfigMapper;
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    private NotifyTemplateService notifyTemplateService;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

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
        List<NotifyConfigDO> weComConfigList = notifyConfigMapper.selectList(
            Wrappers.<NotifyConfigDO>lambdaQuery().eq(NotifyConfigDO::getEnable, CommonConstants.ENABLE)
                .eq(NotifyConfigDO::getType, CommonConstants.WE_COM));
        if (CollectionUtil.isEmpty(weComConfigList)) {
            return;
        }
        NotifyConfigDO notifyConfigDO = weComConfigList.get(0);
        String accessToken = getAccessToken(notifyConfigDO.getAppKey(),
            encryptionUtils.decrypt(notifyConfigDO.getSecret()));
        for (NotifyMessageDO weComMessage : notifyMessageDOS) {
            boolean isSuccess = sendMsg(weComMessage, accessToken, notifyConfigDO.getAgentId());
            if (isSuccess) {
                weComMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                weComMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the WeCom send fail:{}", weComMessage);
            }
            notifyMessageMapper.updateById(weComMessage);
        }
    }

    @Override
    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        NotifyTemplateDO notifyTemplateDO = notifyTemplateService.getById(notifyWayDO.getNotifyTemplateId());
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setWebhook(notifyWayDO.getWebhook()).setContent(notifyTemplateDO.getNotifyContent())
            .setPersonId(notifyWayDO.getPersonId()).setDeptId(notifyWayDO.getDeptId());
        return sendMsg(notifyMessageDO, getAccessToken(notifyConfigDO.getAppKey(),
                encryptionUtils.decrypt(notifyConfigDO.getSecret())), notifyConfigDO.getAgentId());
    }

    @Override
    public String getType() {
        return CommonConstants.WE_COM;
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
     * @param notifyConfigDO NotifyConfig
     * @param notifyWayDO NotifyWay
     * @param notifyContent String
     * @return boolean
     */
    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO, String notifyContent) {
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setWebhook(notifyWayDO.getWebhook()).setContent(notifyContent)
            .setPersonId(notifyWayDO.getPersonId()).setDeptId(notifyWayDO.getDeptId());
        return sendMsg(notifyMessageDO, getAccessToken(notifyConfigDO.getAppKey(),
                encryptionUtils.decrypt(notifyConfigDO.getSecret())), notifyConfigDO.getAgentId());
    }

    private boolean sendMsg(NotifyMessageDO notifyMessageDO, String accessToken, String agentId) {
        JSONObject params = new JSONObject();
        params.put("msgtype", "text");
        JSONObject subParams = new JSONObject();
        subParams.put("content", notifyMessageDO.getContent());
        params.append("text", subParams);
        String res = "";
        String webhook = notifyMessageDO.getWebhook();
        if (StrUtil.isNotBlank(webhook)) {
            res = HttpUtil.post(webhook, params.toString());
        } else {
            if (StrUtil.isBlank(accessToken)) {
                log.error("The accessToken of  WeCom is null");
                return false;
            }
            params.put("touser",
                StrUtil.isNotBlank(notifyMessageDO.getPersonId()) ? notifyMessageDO.getPersonId() : "");
            params.put("toparty",
                StrUtil.isNotBlank(notifyMessageDO.getDeptId()) ? notifyMessageDO.getDeptId() : "");
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
