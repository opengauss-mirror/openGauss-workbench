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
 * WsUtil.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/ops/WsUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.ops;

import cn.hutool.core.util.StrUtil;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.Session;
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
