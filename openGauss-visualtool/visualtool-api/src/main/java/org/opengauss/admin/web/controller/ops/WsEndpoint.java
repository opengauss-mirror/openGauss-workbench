package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.system.service.ops.IWsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * websocket
 * @author lhf
 * @date 2022/8/7 14:00
 **/
@Slf4j
@Component
@EnableWebSocket
@ServerEndpoint("/websocket/{type}/{businessId}")
public class WsEndpoint {

    private static IWsService wsService;

    @Autowired
    public void setWsService(IWsService wsService){
        WsEndpoint.wsService = wsService;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("type") String type,
                       @PathParam("businessId") String businessId) {
        log.info("onOpen, type:{},businessId:{}", type, businessId);
        wsService.onOpen(businessId, session);
    }

    @OnClose
    public void onClose(@PathParam("type") String type,
                        @PathParam("businessId") String businessId) {
        log.info("onClose, type:{},businessId:{}", type, businessId);
        wsService.onClose(businessId);
    }

    @OnError
    public void onError(@PathParam("type") String type,
                        @PathParam("businessId") String businessId,
                        Throwable error) {
        log.error("onError，type:{},businessId:{},err",type,businessId,error);
    }

    @OnMessage
    public void onMessage(@PathParam("type") String type,
                          @PathParam("businessId") String businessId,
                          String message, Session session) {
        log.info("onMessage, type:{},businessId:{},message:{}", type, businessId, message);
        wsService.onMessage(businessId, message);
    }
}
