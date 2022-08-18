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
        error.printStackTrace();
        log.error("onError，pluginId:{},sessionId:{}",pluginId,sessionId);
    }

    @OnMessage
    public void onMessage(@PathParam("pluginId") String pluginId,
                          @PathParam("sessionId") String sessionId,
                          String message, Session session) {
        log.info("onMessage，pluginId:{},sessionId:{}",pluginId,sessionId);
        wsService.onMessage(pluginId,sessionId,message);
    }
}
