package org.opengauss.admin.plugin.domain.model.ops.cache;


import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
@Component
public class WsConnectorManager {
    private static final ConcurrentHashMap<String, WsSession> CONNECTOR_CONTEXT = new ConcurrentHashMap<>();

    /**
     * register session
     *
     * @param sessionId  business ID
     * @param wsSession   websocket session
     */
    public void register(String sessionId, WsSession wsSession) {
        CONNECTOR_CONTEXT.put(sessionId, wsSession);
    }

    /**
     * remove session
     *
     * @param sessionId    business ID
     */
    public void remove(String sessionId) {
        CONNECTOR_CONTEXT.remove(sessionId);
    }

    /**
     * get session
     *
     * @param sessionId    business ID
     * @return websocket session
     */
    public Optional<WsSession> getSession(String sessionId) {
        return Optional.ofNullable(CONNECTOR_CONTEXT.get(sessionId));
    }

    public void removeByVal(WsSession wsSession) {
        CONNECTOR_CONTEXT.forEach((k,v)->{
            if (v.equals(wsSession)){
                CONNECTOR_CONTEXT.remove(k);
            }
        });
    }
}
