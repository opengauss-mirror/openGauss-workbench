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
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/controller/Installer.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.starblues.annotation.Extract;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.log.listener.PluginListener;
import com.nctigba.observability.log.model.dto.ElasticSearchInstallDTO;
import com.nctigba.observability.log.service.impl.ElasticsearchService;
import com.nctigba.observability.log.service.impl.FilebeatService;
import com.nctigba.observability.log.util.MessageSourceUtil;
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

@Slf4j
@Service
@Extract(bus = PluginListener.pluginId)
public class Installer implements SocketExtract {
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private FilebeatService filebeatService;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsFacade wsFacade;

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
                MessageSourceUtil.set(obj.getStr("language"));
                var session = wsConnectorManager.getSession(sessionId)
                        .orElseThrow(() -> new RuntimeException("websocket session not exist"));
                switch (obj.getStr("key")) {
                    case "elasticsearch":
                        ElasticSearchInstallDTO installDTO = new ElasticSearchInstallDTO();
                        installDTO.setHostId(obj.getStr("hostId"));
                        installDTO.setPath(obj.getStr("path"));
                        installDTO.setUsername(obj.getStr("username"));
                        installDTO.setPort(obj.getInt("port"));
                        elasticsearchService.install(session, installDTO);
                        break;
                    case "uninstall elasticsearch":
                        elasticsearchService.uninstall(session, obj.getStr("id"));
                        break;
                    case "filebeat":
                        filebeatService.install(session, obj.getStr("path"), obj.getStr("nodeId"), obj);
                        break;
                    case "uninstall filebeat":
                        filebeatService.uninstall(session, obj.getStr("nodeId"));
                }
            } catch (Exception e) {
                wsFacade.sendMessage(PluginListener.pluginId, sessionId, e.getMessage());
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