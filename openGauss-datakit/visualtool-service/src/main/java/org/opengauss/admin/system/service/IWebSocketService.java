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
 * IWebSocketService.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/service/IWebSocketService.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.service;

import jakarta.websocket.Session;

/**
 * @author lhf
 * @date 2022/8/6 18:14
 **/
public interface IWebSocketService {
    /**
     * Called when a session is established
     */
    void onOpen(String pluginId, String sessionId, Session session);

    /**
     * When the session is closed
     */
    void onClose(String pluginId, String sessionId);

    /**
     * When there is a message in the conversation
     */
    void onMessage(String pluginId, String sessionId, String message);

    void sendMessage(String pluginId, String sessionId, String message);

    /**
     * Send message to client session
     * @param session   session object
     * @param message   message to send
     */
    void sendMessage(Session session, String message);

}
