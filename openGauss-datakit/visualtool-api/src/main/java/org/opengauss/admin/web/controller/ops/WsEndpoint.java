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
 * WsEndpoint.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/WsEndpoint.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.system.service.ops.IWsService;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import jakarta.websocket.OnOpen;
import jakarta.websocket.OnError;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

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

    /**
     * ws endpoint on error handler
     *
     * @param type type
     * @param businessId bussiness id
     * @param error error msg
     */
    @OnError
    public void onError(@PathParam("type") String type, @PathParam("businessId") String businessId, Throwable error) {
        log.error("onError,type:{},businessId:{},err ", type, businessId, error);
    }

    @OnMessage(maxMessageSize = 10 * 1024 * 1024)
    public void onMessage(@PathParam("type") String type,
                          @PathParam("businessId") String businessId,
                          String message, Session session) {
        log.info("onMessage, type:{},businessId:{},message:{}", type, businessId, message);
        wsService.onMessage(businessId, message);
    }
}
