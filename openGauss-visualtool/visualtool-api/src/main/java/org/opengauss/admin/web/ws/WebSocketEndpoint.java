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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

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
        log.info("onOpen, pluginId:{},sessionId:{}",pluginId,sessionId);
        wsService.onOpen(pluginId,sessionId,session);
    }

    @OnClose
    public void onClose(@PathParam("pluginId") String pluginId,
                        @PathParam("sessionId") String sessionId) {
        log.info("onClose, pluginId:{},sessionId:{}",pluginId,sessionId);
        wsService.onClose(pluginId,sessionId);
    }

    @OnError
    public void onError(@PathParam("pluginId") String pluginId,
                        @PathParam("sessionId") String sessionId,
                        Throwable error) {
        log.error("onError，pluginId:{},sessionId:{},errorMessage:{}",pluginId,sessionId,error.getMessage());
    }

    @OnMessage
    public void onMessage(@PathParam("pluginId") String pluginId,
                          @PathParam("sessionId") String sessionId,
                          String message, Session session) {
        log.info("onMessage，pluginId:{},sessionId:{}",pluginId,sessionId);
        wsService.onMessage(pluginId,sessionId,message);
    }
}
