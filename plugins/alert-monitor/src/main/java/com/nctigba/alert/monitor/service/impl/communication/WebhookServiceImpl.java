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
 *  WebhookServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/communication/WebhookServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.communication;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.util.TextParserUtils;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebhookServiceImpl
 *
 * @since 2023/11/27 11:16
 */
@Service
@Slf4j
public class WebhookServiceImpl implements CommunicationService {
    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    private NotifyTemplateMapper templateMapper;

    /**
     * send messages
     *
     * @param notifyMessageDOList notifyMessageList
     */
    @Override
    public void send(List<NotifyMessageDO> notifyMessageDOList) {
        if (CollectionUtil.isEmpty(notifyMessageDOList)) {
            return;
        }
        List<NotifyMessageDO> notifyMessageDOS = notifyMessageDOList.stream().filter(
            item -> item.getMessageType().equals(getType())).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(notifyMessageDOS)) {
            return;
        }
        for (NotifyMessageDO msg : notifyMessageDOS) {
            JSONObject paramsJson = StrUtil.isBlank(msg.getWebhookInfo()) ? new JSONObject() : new JSONObject(
                msg.getWebhookInfo());
            boolean isSuccess = sendMsg(msg.getWebhook(), paramsJson, msg.getTitle(), msg.getContent());
            if (isSuccess) {
                msg.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                msg.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the webhook send fail:{}", msg);
            }
            notifyMessageMapper.updateById(msg);
        }
    }

    private boolean sendMsg(String webhook, JSONObject paramsJson, String notifyTitle, String notifyContent) {
        JSONObject params = paramsJson.getJSONObject("params");
        String paramStr = "";
        if (CollectionUtil.isNotEmpty(params)) {
            paramStr = params.keySet().stream()
                .filter(key -> StrUtil.isNotBlank(key) && StrUtil.isNotBlank(params.getStr(key)))
                .map(key -> key + "=" + params.getStr(key)).collect(Collectors.joining("&"));
        }
        String url = webhook;
        if (StrUtil.isNotBlank(paramStr)) {
            url += (url.indexOf("?") > -1 ? "?" : "&") + paramStr;
        }
        HttpRequest post = HttpRequest.post(url);
        JSONObject header = paramsJson.getJSONObject("header");
        if (CollectionUtil.isNotEmpty(header)) {
            for (String headerKey : header.keySet()) {
                if (StrUtil.isNotBlank(headerKey) && StrUtil.isNotBlank(header.getStr(headerKey))) {
                    post = post.header(headerKey, header.getStr(headerKey));
                }
            }
        }
        String body = paramsJson.getStr("body");
        if (StrUtil.isNotBlank(body)) {
            Map map = new HashMap();
            map.put("notifyTitle", notifyTitle);
            map.put("notifyContent",
                StringEscapeUtils.escapeJson(notifyContent));
            post = post.body(TextParserUtils.parse(body, map));
        }
        String result = post.execute().body();
        JSONObject resultCode = paramsJson.getJSONObject("resultCode");
        if (CollectionUtil.isNotEmpty(resultCode)) {
            if (StrUtil.isBlank(result)) {
                return false;
            }
            JSONObject resultJson = new JSONObject(result);
            for (String key : resultCode.keySet()) {
                String val = resultJson.getStr(key);
                if (!val.equals(resultCode.getStr(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        JSONObject paramsJson = new JSONObject();
        String params = notifyWayDO.getParams();
        if (StrUtil.isNotBlank(params)) {
            paramsJson.put("params", new JSONObject(params));
        }
        String header = notifyWayDO.getHeader();
        if (StrUtil.isNotBlank(header)) {
            paramsJson.put("header", new JSONObject(header));
        }
        paramsJson.put("body", notifyWayDO.getBody());
        String resultCode = notifyWayDO.getResultCode();
        if (StrUtil.isNotBlank(resultCode)) {
            paramsJson.put("resultCode", new JSONObject(resultCode));
        }
        NotifyTemplateDO notifyTemplateDO = templateMapper.selectById(notifyWayDO.getNotifyTemplateId());
        return sendMsg(notifyWayDO.getWebhook(), paramsJson, notifyTemplateDO.getNotifyTitle(),
            notifyTemplateDO.getNotifyContent());
    }

    @Override
    public String getType() {
        return CommonConstants.WEBHOOK;
    }
}