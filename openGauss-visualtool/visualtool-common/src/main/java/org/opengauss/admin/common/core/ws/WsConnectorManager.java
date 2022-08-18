package org.opengauss.admin.common.core.ws;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @className: WsConnectorManager
 * @description: WsConnectorManager
 * @author: xielibo
 * @date: 2022-11-14 22:31
 **/
@Slf4j
public class WsConnectorManager {

    private static final ConcurrentHashMap<String, Session> CONNECTOR_CONTEXT = new ConcurrentHashMap<>();

    /**
     * register session
     * @param pluginId
     * @param sessionId
     * @param session
     */
    public static void register(String pluginId, String sessionId, Session session) {
        CONNECTOR_CONTEXT.put(prepareKey(pluginId, sessionId), session);
    }

    /**
     * remove session
     */
    public static void remove(String pluginId, String sessionId) {
        CONNECTOR_CONTEXT.remove(prepareKey(pluginId, sessionId));
    }

    /**
     * get session
     */
    public static Session getSession(String pluginId, String sessionId) {
        log.info("cache sessions list.");
        CONNECTOR_CONTEXT.entrySet().stream().forEach(x -> {
            System.out.println(x.getKey());
        });
        return CONNECTOR_CONTEXT.get(prepareKey(pluginId, sessionId));
    }

    private static String prepareKey(String pluginId, String sessionId) {
        return pluginId + "_" + sessionId;
    }
}
