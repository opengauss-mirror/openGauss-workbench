/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * WsServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/impl/WsServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops.impl;

import com.jcraft.jsch.ChannelShell;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.SSHChannelManager;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.service.ops.IWsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lhf
 * @date 2022/8/6 18:14
 **/
@Slf4j
@Service
public class WsServiceImpl implements IWsService {
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private WsConnectorManager wsConnectorManager;

    @Override
    public void onOpen(String businessId, Session session) {
        wsConnectorManager.register(businessId, new WsSession(session, businessId));
    }

    @Override
    public void onClose(String businessId) {
        wsConnectorManager.getSession(businessId).ifPresent(wsSession -> {
            try {
                wsSession.getSession().close();
            } catch (IOException e) {
                log.error("websocket session close fail", e);
            }
        });
        TaskManager.remove(businessId).ifPresent(future -> future.cancel(true));
        wsConnectorManager.remove(businessId);
    }

    @Override
    public void onMessage(String businessId, String message) {
        ChannelShell channelShell = SSHChannelManager.getChannelShell(businessId).orElseThrow(() -> new OpsException("No connection information found"));
        jschUtil.sendToChannelShell(channelShell, message);
    }

    @Override
    public void sendMessage(Session session, byte[] message) {
        try {
            session.getBasicRemote().sendText(new String(message));
        } catch (IOException e) {
            log.error("Failed to send ws message", e);
        }
    }

    @Override
    public WsSession getRetWsSession(String businessId) {
        return wsConnectorManager
                .getSession(businessId)
                .orElseThrow(() -> new OpsException("response session does not exist"));
    }

    @Override
    public Optional<Session> obtainFreshSession(WsSession wsSession) {
        if (wsSession.getSession().isOpen()){
            return Optional.ofNullable(wsSession.getSession());
        }

        AtomicReference<Session> session = new AtomicReference<>();
        wsConnectorManager.getSession(wsSession.getBusinessId()).ifPresent(wsSession1 -> session.set(wsSession1.getSession()));
        return Optional.ofNullable(session.get());
    }
}
