package org.opengauss.admin.system.service;

import javax.websocket.Session;

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
