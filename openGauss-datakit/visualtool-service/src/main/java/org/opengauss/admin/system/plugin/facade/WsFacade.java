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
 * WsFacade.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/facade/WsFacade.java
 *
 * -------------------------------------------------------------------------
 */


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
        log.debug("call to send message. plugin: {}, sessionId: {}, message: {}", pluginId, sessionId, message);
        wsService.sendMessage(pluginId, sessionId, message);
    }
}
