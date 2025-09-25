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
 * SocketExtract.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/extract/SocketExtract.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.extract;

import jakarta.websocket.Session;

/**
 * @className: SocketExtract
 * @description: Used to receive messages from clients
 * @author: xielibo
 * @date: 2022-11-14 21:33
 **/
public interface SocketExtract {

    /**
     * Connection success callback
     * @param pluginId
     * @param sessionId
     * @param session
     */
    public void onOpen(String pluginId, String sessionId, Session session);


    /**
     * Receive and process messages
     * @param sessionId
     * @param message
     */
    public void processMessage(String sessionId, String message);

    /**
     * Connection close callback
     * @param sessionId
     * @param sessionId
     */
    public void onClose(String pluginId, String sessionId);

}
