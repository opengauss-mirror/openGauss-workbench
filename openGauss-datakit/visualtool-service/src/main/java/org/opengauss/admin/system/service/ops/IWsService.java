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
 * IWsService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/ops/IWsService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service.ops;

import org.opengauss.admin.common.core.domain.model.ops.WsSession;

import jakarta.websocket.Session;
import java.util.Optional;

/**
 * @author lhf
 * @date 2022/8/6 18:14
 **/
public interface IWsService {
    /**
     * Called when a session is established
     *
     * @param type       session type
     * @param businessId session id
     * @param session    session object
     */
    void onOpen(String businessId, Session session);

    /**
     * When the session is closed
     *
     * @param type       session type
     * @param businessId session id
     */
    void onClose(String businessId);

    /**
     * When there is a message in the conversation
     *
     * @param type       session type
     * @param businessId session id
     * @param message    message in conversation
     */
    void onMessage(String businessId, String message);

    /**
     * Send message to client session
     * @param session   session object
     * @param message   message to send
     */
    void sendMessage(Session session, byte[] message);

    /**
     * Get the websocket object of the response
     *
     * @param wsConnectType session type
     * @param businessId    session id
     * @return
     */
    WsSession getRetWsSession(String businessId);

    /**
     * Obtain a refreshed websocket session object. When using the WsSession object, it is recommended to obtain a refreshed one to avoid cache expiration.
     * @param wsSession websocket session object
     * @return
     */
    Optional<Session> obtainFreshSession(WsSession wsSession);
}
