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
 *  SNMPServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/communication/SNMPServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.communication;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.NotifyMessageMapper;
import com.nctigba.alert.monitor.mapper.NotifyTemplateMapper;
import com.nctigba.alert.monitor.model.dto.NotifySnmpDTO;
import com.nctigba.alert.monitor.model.entity.NotifyConfigDO;
import com.nctigba.alert.monitor.model.entity.NotifyMessageDO;
import com.nctigba.alert.monitor.model.entity.NotifyTemplateDO;
import com.nctigba.alert.monitor.model.entity.NotifyWayDO;
import com.nctigba.alert.monitor.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
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
 * SNMPServiceImpl
 *
 * @since 2023/11/27 11:28
 */
@Service
@Slf4j
public class SNMPServiceImpl implements CommunicationService {
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
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

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
        for (NotifyMessageDO snmpMessage : notifyMessageDOS) {
            String snmpInfo = snmpMessage.getSnmpInfo();
            if (StrUtil.isBlank(snmpInfo)) {
                continue;
            }
            NotifySnmpDTO notifySnmpDto = JSONUtil.toBean(snmpInfo, NotifySnmpDTO.class);
            boolean isSuccess = sendMsg(notifySnmpDto, snmpMessage.getContent());
            if (isSuccess) {
                snmpMessage.setStatus(1).setUpdateTime(LocalDateTime.now());
            } else {
                snmpMessage.setStatus(2).setUpdateTime(LocalDateTime.now());
                log.error("the SNMP trap send fail:{}", snmpMessage);
            }
            notifyMessageMapper.updateById(snmpMessage);
        }
    }

    @Override
    public boolean sendTest(NotifyConfigDO notifyConfigDO, NotifyWayDO notifyWayDO) {
        NotifySnmpDTO notifySnmpDto = BeanUtil.copyProperties(notifyWayDO, NotifySnmpDTO.class);
        NotifyTemplateDO notifyTemplateDO = templateMapper.selectById(notifyWayDO.getNotifyTemplateId());
        return sendMsg(notifySnmpDto, notifyTemplateDO.getNotifyContent());
    }

    @Override
    public String getType() {
        return CommonConstants.SNMP;
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

    private boolean sendMsg(NotifySnmpDTO notifySnmpDto, String msg) {
        try (TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping()) {
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

            try (Snmp snmp = new Snmp(transport)) {
                if (notifySnmpDto.getSnmpVersion().equals(SnmpConstants.version3)) {
                    SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());
                    USM usm = new USM(SecurityProtocols.getInstance(),
                        new OctetString(MPv3.createLocalEngineID()), 0);
                    SecurityModels.getInstance().addSecurityModel(usm);
                    OctetString userName = new OctetString(notifySnmpDto.getSnmpUsername());
                    OctetString authPass = new OctetString(encryptionUtils.decrypt(notifySnmpDto.getSnmpAuthPasswd()));
                    OctetString privPass = new OctetString(encryptionUtils.decrypt(notifySnmpDto.getSnmpPrivPasswd()));
                    snmp.getUSM().addUser(userName, new UsmUser(userName, AuthMD5.ID, authPass, PrivDES.ID, privPass));
                }
                snmp.send(pdu, target);
            } catch (IOException e) {
                log.error("SNMP send failed", e);
                return false;
            }
            return true;
        } catch (IOException e) {
            log.error("SNMP send failed", e);
            return false;
        }
    }
}
