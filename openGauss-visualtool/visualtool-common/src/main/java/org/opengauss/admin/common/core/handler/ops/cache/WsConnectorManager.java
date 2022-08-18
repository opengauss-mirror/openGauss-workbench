package org.opengauss.admin.common.core.handler.ops.cache;

import org.opengauss.admin.common.core.domain.model.ops.WsSession;
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
     * @param businessId business ID
     * @param wsSession  websocket session
     */
    public void register(String businessId, WsSession wsSession) {
        CONNECTOR_CONTEXT.put(businessId, wsSession);
    }

    /**
     * remove session
     *
     * @param businessId  business ID
     */
    public void remove(String businessId) {
        CONNECTOR_CONTEXT.remove(businessId);
    }

    /**
     * get session
     *
     * @param businessId  business ID
     * @return websocket session
     */
    public Optional<WsSession> getSession(String businessId) {
        return Optional.ofNullable(CONNECTOR_CONTEXT.get(businessId));
    }
}
