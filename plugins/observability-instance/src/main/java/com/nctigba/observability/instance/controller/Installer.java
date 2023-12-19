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
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/controller/Installer.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import cn.hutool.json.JSONArray;
import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gitee.starblues.annotation.Extract;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.listener.PluginListener;
import com.nctigba.observability.instance.service.ExporterInstallService;
import com.nctigba.observability.instance.service.PrometheusService;
import com.nctigba.observability.instance.util.MessageSourceUtils;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Extract(bus = PluginListener.pluginId)
public class Installer implements SocketExtract {
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsConnectorManager wsConnectorManager;
    @Autowired
    private PrometheusService prometheusService;
    @Autowired
    private ExporterInstallService exporterService;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsFacade wsFacade;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        wsConnectorManager.register(sessionId, new WsSession(session, sessionId));
    }

    @Override
    public void processMessage(String sessionId, String message) {
        ThreadUtil.execute(() -> {
            try {
                var obj = JSONUtil.parseObj(message);
                var session = wsConnectorManager.getSession(sessionId)
                        .orElseThrow(() -> new RuntimeException("websocket session not exist"));
                MessageSourceUtils.set(obj.getStr("language"));
                switch (obj.getStr("key")) {
                    case "prometheus":
                        prometheusService.install(session,
                                obj.getStr("hostId"),
                                obj.getStr("path"),
                                obj.getStr("username"),
                                obj.getStr("rootPassword", null),
                                obj.getInt("port"),
                                obj.getStr("storageDays"));
                        break;
                    case "uninstall prometheus":
                        prometheusService.uninstall(session, obj.getStr("id"));
                        break;
                    case "exporter":
                        ExporterInstallDTO exporterInstallDTO = new ExporterInstallDTO();
                        JSONArray nodeIdsArray = obj.getJSONArray("nodeIds");
                        List<String> nodeIds = new ArrayList<>();
                        for (int i = 0; i < nodeIdsArray.size(); i++) {
                            String nodeId = nodeIdsArray.getStr(i);
                            nodeIds.add(nodeId);
                        }
                        exporterInstallDTO.setNodeIds(nodeIds);
                        exporterInstallDTO.setEnvId(obj.getStr("envId"));
                        exporterInstallDTO.setHostId(obj.getStr("hostId"));
                        exporterInstallDTO.setPath(obj.getStr("path"));
                        exporterInstallDTO.setUsername(obj.getStr("username"));
                        exporterInstallDTO.setHttpPort(obj.getInt("httpPort"));
                        exporterService.install(session, exporterInstallDTO);
                        break;
                    case "uninstall exporter":
                        exporterService.uninstall(session, obj.getStr("envId"));
                }
            } catch (Exception e) {
                var sw = new StringWriter();
                try (var pw = new PrintWriter(sw);) {
                    e.printStackTrace(pw);
                }
                wsFacade.sendMessage(PluginListener.pluginId, sessionId, sw.toString());
            }
        });
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