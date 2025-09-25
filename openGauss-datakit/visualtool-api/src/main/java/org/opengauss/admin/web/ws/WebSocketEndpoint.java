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
 * WebSocketEndpoint.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/ws/WebSocketEndpoint.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.web.ws;

import org.opengauss.admin.system.service.IWebSocketService;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * @className: WsSocketController
 * @description: WebSocketEndpoint
 * @author: xielibo
 * @date: 2022-11-14 22:31
 **/
@Slf4j
@Component
@EnableWebSocket
@ServerEndpoint("/ws/{pluginId}/{sessionId}")
public class WebSocketEndpoint {

    private static IWebSocketService wsService;
    @Autowired
    public void setWsService(IWebSocketService wsService){
        WebSocketEndpoint.wsService = wsService;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("pluginId") String pluginId,
                       @PathParam("sessionId") String sessionId) {
        log.info("WebSocket onOpen, pluginId: {}", pluginId);
        wsService.onOpen(pluginId,sessionId,session);
    }

    @OnClose
    public void onClose(@PathParam("pluginId") String pluginId,
                        @PathParam("sessionId") String sessionId) {
        log.info("WebSocket onClose, pluginId: {}", pluginId);
        wsService.onClose(pluginId,sessionId);
    }

    @OnError
    public void onError(@PathParam("pluginId") String pluginId,
                        @PathParam("sessionId") String sessionId,
                        Throwable error) {
        log.error("WebSocket onError, pluginId: {}, errorMessage: {}", pluginId, error.getMessage());
    }

    @OnMessage(maxMessageSize = 10 * 1024 * 1024)
    public void onMessage(@PathParam("pluginId") String pluginId,
                          @PathParam("sessionId") String sessionId,
                          String message, Session session) {
        log.info("WebSocket onMessage, pluginId: {}", pluginId);
        wsService.onMessage(pluginId,sessionId,message);
    }
}
