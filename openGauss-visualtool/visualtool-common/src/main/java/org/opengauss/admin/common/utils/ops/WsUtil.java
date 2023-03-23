package org.opengauss.admin.common.utils.ops;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Objects;

/**
 * @author lhf
 * @date 2022/8/15 09:32
 **/
@Slf4j
@Component
public class WsUtil {

    @Autowired
    private WsConnectorManager wsConnectorManager;

    public void sendText(WsSession wsSession, String message) {
        if (StrUtil.isEmpty(message)) {
            return;
        }

        if (Objects.nonNull(wsSession)) {
            wsConnectorManager.getSession(wsSession.getBusinessId()).ifPresent(wss -> {
                Session session = wss.getSession();
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        log.error("Failed to send a message to {}, error: {}", wsSession.getBusinessId(), e);
                    }
                } else {
                    log.error("Connection {} closed", wsSession.getBusinessId());
                }
            });
        }
    }

    public void close(WsSession wsSession) {
        if (Objects.nonNull(wsSession)) {
            wsConnectorManager.removeByVal(wsSession);
            Session session = wsSession.getSession();

            if (Objects.nonNull(session)) {
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("close websocket session {} error : {}", e, wsSession.getBusinessId());
                }
            }
        }
    }
}
