package com.nctigba.observability.sql.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
import com.nctigba.observability.sql.listener.PluginListener;
import com.nctigba.observability.sql.service.AgentService;

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
	@AutowiredType(Type.PLUGIN_MAIN)
	private WsFacade wsFacade;
	@Autowired
	private AgentService agentService;

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
				var session = wsConnectorManager.getSession(sessionId)
						.orElseThrow(() -> new RuntimeException("websocket session not exist"));
				switch (obj.getStr("key")) {
				case "agent":
					agentService.install(session, obj.getStr("nodeId"), obj.getStr("rootPassword"),
							obj.getStr("callbackPath"));
					break;
				case "uninstall agent":
					agentService.uninstall(session, obj.getStr("nodeId"), obj.getStr("rootPassword"));
				}
			} catch (Exception e) {
				var sw = new StringWriter();
				try (var pw = new PrintWriter(sw);) {
					e.printStackTrace(pw);
				}
				wsFacade.sendMessage(PluginListener.pluginId, sessionId, sw.toString());
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