package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.system.service.IWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @className: WsFacade
 * @description: process websocket messages provided to plugins.
 * @author: xielibo
 * @date: 2022-11-15 12:49
 **/
@Slf4j
@Service
public class WsFacade {

    @Autowired
    private IWebSocketService wsService;

    /**
     * send messages to clients
     * @param pluginId
     * @param sessionId
     * @param message
     */
    public void sendMessage(String pluginId, String sessionId, String message) {
        log.info("call to send message. plugin: {}, sessionId: {}, message: {}", pluginId, sessionId, message);
        wsService.sendMessage(pluginId, sessionId, message);
    }
}
