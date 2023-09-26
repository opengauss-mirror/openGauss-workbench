
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.dto.NotifySnmpDto;
import com.nctigba.alert.monitor.entity.NotifyMessage;
import com.nctigba.alert.monitor.entity.NotifyTemplate;
import com.nctigba.alert.monitor.entity.NotifyWay;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.utils.TextParser;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.AbstractTarget;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * send message by third-party
 *
 * @since 2023/8/18 00:21
 */
@Service
@Slf4j
public class ThirdPartyService {
    private static Map<Integer, PDU> pduMap = new HashMap<>();

    static {
        PDUv1 pdUv1 = new PDUv1();
        pdUv1.setType(PDU.V1TRAP);
        pduMap.put(SnmpConstants.version1, pdUv1);
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        pduMap.put(SnmpConstants.version2c, pdu);
        ScopedPDU scopedPDU = new ScopedPDU();
        scopedPDU.setType(PDU.NOTIFICATION);
        pduMap.put(SnmpConstants.version3, scopedPDU);
    }

    @Autowired
    private NotifyMessageMapper notifyMessageMapper;
    @Autowired
    private NotifyTemplateMapper templateMapper;

    /**
     * send messages
     *
     * @param webhookMessages List<NotifyMessage>
     */
    public void sendWebhookMsgList(List<NotifyMessage> webhookMessages) {
        if (CollectionUtil.isEmpty(webhookMessages)) {
            return;
        }
        for (NotifyMessage msg : webhookMessages) {
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
            map.put("notifyContent", notifyContent.replaceAll(CommonConstants.LINE_SEPARATOR, "\\n"));
            post = post.body(new TextParser().parse(body, map));
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

    /**
     * send messages test
     *
     * @param notifyWay NotifyWay
     * @return boolean
     */
    public boolean testWebhookByNotifyWay(NotifyWay notifyWay) {
        JSONObject paramsJson = new JSONObject();
        String params = notifyWay.getParams();
        if (StrUtil.isNotBlank(params)) {
            paramsJson.put("params", new JSONObject(params));
        }
        String header = notifyWay.getHeader();
        if (StrUtil.isNotBlank(header)) {
            paramsJson.put("header", new JSONObject(header));
        }
        paramsJson.put("body", notifyWay.getBody());
        String resultCode = notifyWay.getResultCode();
        if (StrUtil.isNotBlank(resultCode)) {
            paramsJson.put("resultCode", new JSONObject(resultCode));
        }
        NotifyTemplate notifyTemplate = templateMapper.selectById(notifyWay.getNotifyTemplateId());
        return sendMsg(notifyWay.getWebhook(), paramsJson, notifyTemplate.getNotifyTitle(),
            notifyTemplate.getNotifyContent());
    }

    /**
     * send SNMP messages test
     *
     * @param notifyWay NotifyWay
     * @return boolean
     */
    public boolean testSnmpByNotifyWay(NotifyWay notifyWay) {
        NotifySnmpDto notifySnmpDto = new NotifySnmpDto();
        BeanUtil.copyProperties(notifyWay, notifySnmpDto);
        NotifyTemplate notifyTemplate = templateMapper.selectById(notifyWay.getNotifyTemplateId());
        return sendSnmpMsg(notifySnmpDto, notifyTemplate.getNotifyContent());
    }

    private Optional<AbstractTarget> createSourceTarget(Integer snmpVersion, String community, String username) {
        switch (snmpVersion) {
            case SnmpConstants.version1:
                CommunityTarget v1Target = new CommunityTarget();
                v1Target.setCommunity(new OctetString(community));
                v1Target.setVersion(SnmpConstants.version1);
                return Optional.of(v1Target);
            case SnmpConstants.version2c:
                CommunityTarget v2Target = new CommunityTarget();
                v2Target.setCommunity(new OctetString(community));
                v2Target.setVersion(SnmpConstants.version2c);
                return Optional.of(v2Target);
            case SnmpConstants.version3:
                UserTarget target = new UserTarget();
                target.setVersion(SnmpConstants.version3);
                target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
                target.setSecurityName(new OctetString(username));
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
                return Optional.of(target);
            default:
                return Optional.empty();
        }
    }

    private boolean sendSnmpMsg(NotifySnmpDto notifySnmpDto, String msg) {
        try {
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();
            Integer snmpVersion = notifySnmpDto.getSnmpVersion();
            PDU pdu = pduMap.get(snmpVersion);
            if (pdu == null) {
                return false;
            }
            VariableBinding vb = new VariableBinding();
            OID oid = new OID(notifySnmpDto.getSnmpOid());
            vb.setOid(oid);
            vb.setVariable(new OctetString(msg));
            pdu.add(vb);

            AbstractTarget target = createSourceTarget(notifySnmpDto.getSnmpVersion(),
                notifySnmpDto.getSnmpCommunity(), notifySnmpDto.getSnmpUsername()).get();
            if (target == null) {
                return false;
            }
            Address targetAddress =
                GenericAddress.parse("udp:" + notifySnmpDto.getSnmpIp() + "/" + notifySnmpDto.getSnmpPort());
            target.setAddress(targetAddress);
            // retry times when commuication error
            target.setRetries(2);
            // timeout
            target.setTimeout(1500);

            Snmp snmp = new Snmp(transport);
            if (notifySnmpDto.getSnmpVersion().equals(SnmpConstants.version3)) {
                SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());
                USM usm = new USM(SecurityProtocols.getInstance(),
                    new OctetString(MPv3.createLocalEngineID()), 0);
                SecurityModels.getInstance().addSecurityModel(usm);
                OctetString userName = new OctetString(notifySnmpDto.getSnmpUsername());
                OctetString authPass = new OctetString(notifySnmpDto.getSnmpAuthPasswd());
                OctetString privPass = new OctetString(notifySnmpDto.getSnmpPrivPasswd());
                snmp.getUSM().addUser(userName, new UsmUser(userName, AuthMD5.ID, authPass, PrivDES.ID, privPass));
            }
            snmp.send(pdu, target);
            return true;
        } catch (IOException e) {
            log.error("SNMP send failed", e);
            return false;
        }
    }

    /**
     * send SNMP messages
     *
     * @param snmpMessages List<NotifyMessage>
     */
    public void sendSnmpMsgList(List<NotifyMessage> snmpMessages) {
        if (CollectionUtil.isEmpty(snmpMessages)) {
            return;
        }
        for (NotifyMessage snmpMessage : snmpMessages) {
            String snmpInfo = snmpMessage.getSnmpInfo();
            if (StrUtil.isBlank(snmpInfo)) {
                continue;
            }
            NotifySnmpDto notifySnmpDto = JSONUtil.toBean(snmpInfo, NotifySnmpDto.class);
            boolean isSuccess = sendSnmpMsg(notifySnmpDto, snmpMessage.getContent());
            if (isSuccess) {
                snmpMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                snmpMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the SNMP trap send fail:{}", snmpMessage);
            }
            notifyMessageMapper.updateById(snmpMessage);
        }
    }
}
