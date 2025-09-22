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
 * WebSocketServiceImpl.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/impl/WebSocketServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.impl;
import com.gitee.starblues.spring.extract.ExtractFactory;
import org.opengauss.admin.common.core.ws.WsConnectorManager;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import org.opengauss.admin.system.service.IWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
import java.io.IOException;
import java.util.List;

/**
 * @className: WsServiceImpl
 * @description: process websocket messages service
 * @author: xielibo
 * @date: 2022-11-15 12:49
 **/
@Slf4j
@Component
public class WebSocketServiceImpl implements IWebSocketService {
    @Autowired
    private ExtractFactory extractFactory;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        WsConnectorManager.register(pluginId,sessionId,session);
        log.info("callback plugin. plugin is {}", pluginId);
        List<SocketExtract> extractByInterClass = extractFactory.getExtractByInterClass(pluginId, SocketExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the SocketExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).onOpen(pluginId, sessionId, session);
        } else {
            log.error("No implementation found");
        }
    }

    @Override
    public void onClose(String pluginId, String sessionId) {
        WsConnectorManager.remove(pluginId, sessionId);
        log.info("callback plugin. plugin is {}", pluginId);
        List<SocketExtract> extractByInterClass = extractFactory.getExtractByInterClass(pluginId, SocketExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the SocketExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).onClose(pluginId,sessionId);
        } else {
            log.error("No implementation found");
        }
    }

    /**
     * After receiving the message, forward it to the plugin for processing
     * @param pluginId
     * @param sessionId
     * @param message
     */
    @Override
    public void onMessage(String pluginId, String sessionId, String message) {
        log.info("forward to plugin for processing. plugin is {}", pluginId);
        List<SocketExtract> extractByInterClass = extractFactory.getExtractByInterClass(pluginId, SocketExtract.class);
        if (extractByInterClass.size() > 0) {
            log.info("Get the SocketExtract implementation class. {}" , extractByInterClass.get(0).getClass().getPackage());
            extractByInterClass.get(0).processMessage(sessionId,message);
        } else {
            log.error("No implementation found");
        }
    }

    @Override
    public void sendMessage(String pluginId, String sessionId, String message){
        sendMessage(WsConnectorManager.getSession(pluginId, sessionId), message);
    }

    @Override
    public void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("Failed to send ws message", e);
        }
    }

}
