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
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/WsServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import com.gitee.starblues.annotation.Extract;
import com.jcraft.jsch.JSchException;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.SSHChannelManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.TaskManager;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import com.jcraft.jsch.ChannelShell;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;


/**
 * @author lhf
 * @date 2022/8/6 18:14
 **/
@Slf4j
@Service
@Extract(bus = "ops-handler")
public class WsServiceImpl implements SocketExtract {
    @Autowired
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    private SSHChannelManager sshChannelManager;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        log.info("WebSocket onOpen, pluginId: {}", pluginId);
        wsConnectorManager.register(sessionId,new WsSession(session,sessionId));
    }

    @Override
    public void processMessage(String sessionId, String message) {
        log.info("WebSocket processMessage, message: {}", message);
        ChannelShell channelShell = sshChannelManager.getChannelShell(sessionId).orElseThrow(() -> new OpsException("No connection information found"));
        jschUtil.sendToChannelShell(channelShell, message);
    }

    @Override
    public void onClose(String pluginId, String sessionId) {
        log.info("WebSocket onClose, pluginId: {}", pluginId);
        sshChannelManager.getChannelShell(sessionId).ifPresent(channelShell -> {
            com.jcraft.jsch.Session session = null;
            try {
                session = channelShell.getSession();
            } catch (JSchException e) {
                log.error("get channelSession By channelShell fail",e);
            }

            if (Objects.nonNull(session) && session.isConnected()){
                session.disconnect();
            }

            if (channelShell.isConnected()){
                channelShell.disconnect();
            }
        });

        wsConnectorManager.getSession(sessionId).ifPresent(wsSession -> {
            try {
                wsSession.getSession().close();
            } catch (IOException e) {
                log.error("close websocket session fail",e);
            }
        });

        TaskManager.remove(sessionId).ifPresent(future -> future.cancel(true));
        wsConnectorManager.remove(sessionId);
    }
}
