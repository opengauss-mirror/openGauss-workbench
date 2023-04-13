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
 * WsConnectorManager.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/cache/WsConnectorManager.java
 *
 * -------------------------------------------------------------------------
 */

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
