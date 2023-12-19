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

import java.io.IOException;

import javax.websocket.Session;

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
import com.nctigba.observability.log.listener.PluginListener;
import com.nctigba.observability.log.service.ElasticsearchService;
import com.nctigba.observability.log.service.FilebeatService;
import com.nctigba.observability.log.util.MessageSourceUtil;

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
	private ElasticsearchService elasticsearchService;
	@Autowired
	private FilebeatService filebeatService;
	@Autowired
	@AutowiredType(Type.PLUGIN_MAIN)
	private WsFacade wsFacade;

	@Override
	public void onOpen(String pluginId, String sessionId, Session session) {
		wsConnectorManager.register(sessionId, new WsSession(session, sessionId));
		System.out.println("连接成功。。。。。。。。");
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
					elasticsearchService.install(session, obj.getStr("hostId"), obj.getStr("path"), obj.getStr("username"), obj.getStr("rootPassword", null),
							obj.getInt("port"));
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
		System.out.println("接收到消息并处理。。。。。。。。" + message);
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