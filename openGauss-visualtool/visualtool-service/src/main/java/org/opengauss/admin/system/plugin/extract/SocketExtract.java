package org.opengauss.admin.system.plugin.extract;

import javax.websocket.Session;

/**
 * @className: SocketExtract
 * @description: Used to receive messages from clients
 * @author: xielibo
 * @date: 2022-11-14 21:33
 **/
public interface SocketExtract {

    /**
     * Connection success callback
     * @param pluginId
     * @param sessionId
     * @param session
     */
    public void onOpen(String pluginId, String sessionId, Session session);


    /**
     * Receive and process messages
     * @param sessionId
     * @param message
     */
    public void processMessage(String sessionId, String message);

    /**
     * Connection close callback
     * @param sessionId
     * @param sessionId
     */
    public void onClose(String pluginId, String sessionId);

}
