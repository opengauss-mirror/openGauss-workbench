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
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/handler/ops/cache/WsConnectorManager.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.handler.ops.cache;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/8/11 20:37
 **/
@Component
@Slf4j
public class WsConnectorManager {
    private static final ConcurrentHashMap<String, WsSession> CONNECTOR_CONTEXT = new ConcurrentHashMap<>();

    /**
     * register session
     *
     * @param businessId business ID
     * @param wsSession  websocket session
     */
    public void register(String businessId, WsSession wsSession) {
        log.info("register-WsConnectorManager.java businessId: {}", businessId);
        CONNECTOR_CONTEXT.put(businessId, wsSession);
    }

    /**
     * remove session
     *
     * @param businessId business ID
     */
    public void remove(String businessId) {
        log.info("remove1-WsConnectorManager.java businessId: {}", businessId);
        CONNECTOR_CONTEXT.remove(businessId);
    }

    /**
     * get session
     *
     * @param businessId business ID
     * @return websocket session
     */
    public Optional<WsSession> getSession(String businessId) {
        return Optional.ofNullable(CONNECTOR_CONTEXT.get(businessId));
    }

    public void removeByVal(WsSession wsSession) {
        CONNECTOR_CONTEXT.forEach((k, v) -> {
            if (v.equals(wsSession)) {
                log.info("remove2-WsConnectorManager.java key {}", k);
                CONNECTOR_CONTEXT.remove(k);
            }
        });
    }
}
