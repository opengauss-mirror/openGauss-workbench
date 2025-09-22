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
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/core/ws/WsConnectorManager.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.core.ws;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;


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
     *
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
        return CONNECTOR_CONTEXT.get(prepareKey(pluginId, sessionId));
    }

    private static String prepareKey(String pluginId, String sessionId) {
        return pluginId + "_" + sessionId;
    }
}
