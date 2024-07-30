/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  Installer.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/Installer.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.starblues.annotation.Extract;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.listener.PluginListener;
import com.nctigba.observability.sql.util.MessageSourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Service
@Extract(bus = PluginListener.pluginId)
public class Installer implements SocketExtract {
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsConnectorManager wsConnectorManager;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsFacade wsFacade;
    @Autowired
    private AgentServiceImpl agentServiceImpl;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        wsConnectorManager.register(sessionId, new WsSession(session, sessionId));
        log.info("Connection successful......");
    }

    @Override
    public void processMessage(String sessionId, String message) {
        ThreadUtil.execute(() -> {
            try {
                var obj = JSONUtil.parseObj(message);
                MessageSourceUtils.set(obj.getStr("language"));
                var session = wsConnectorManager.getSession(sessionId)
                        .orElseThrow(() -> new RuntimeException("websocket session not exist"));
                switch (obj.getStr("key")) {
                    case "agent":
                        agentServiceImpl.install(session, obj.getStr("nodeId"), obj.getInt("port"),
                                obj.getStr("username"), obj.getStr("rootPassword"),
                                obj.getStr("path"), obj.getStr("callbackPath"));
                        break;
                    case "uninstall agent":
                        agentServiceImpl.uninstall(session, obj.getStr("nodeId"),
                                obj.getStr("rootPassword"));
                        break;
                }
            } catch (Exception e) {
                var sw = new StringWriter();
                try (var pw = new PrintWriter(sw);) {
                    e.printStackTrace(pw);
                }
                wsFacade.sendMessage(PluginListener.pluginId, sessionId, sw.toString());
            }
        });
        log.info("Received message and processed it......" + message);
    }

    @Override
    public void onClose(String pluginId, String sessionId) {
        wsConnectorManager.getSession(sessionId).ifPresent(wsSession -> {
            try {
                wsSession.getSession().close();
            } catch (IOException e) {
                log.error("close websocket session fail", e);
            }
        });
        TaskManager.remove(sessionId).ifPresent(future -> future.cancel(true));
        wsConnectorManager.remove(sessionId);
    }
}