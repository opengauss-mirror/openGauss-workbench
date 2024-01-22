/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.tun.ws;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gitee.starblues.annotation.Extract;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.TaskManager;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.opengauss.tun.config.PluginExtensionInfoConfig;
import org.opengauss.tun.domain.TrainingConfig;
import org.opengauss.tun.mapper.TrainingConfigMpper;
import org.opengauss.tun.service.impl.TuningServiceImpl;
import org.opengauss.tun.utils.response.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;

/**
 * WebSocketServer
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
@Service
@Extract(bus = PluginExtensionInfoConfig.PLUGIN_ID)
public class WebSocketServer implements SocketExtract {
    @Autowired
    private TuningServiceImpl tuningService;

    @Autowired
    private TrainingConfigMpper configMpper;

    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsConnectorManager wsConnectorManager;

    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    private WsFacade wsFacade;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        wsConnectorManager.register(sessionId, new WsSession(session, sessionId));
        log.info("Connection successful...........");
    }

    @Override
    public void processMessage(String sessionId, String message) {
        if (StrUtil.isEmpty(message)) {
            log.info("Received an empty message and cannot process it.");
            return;
        }
        log.info("Received an empty message and cannot process it. Received message and processed...");
        push(sessionId, message);
    }

    private void push(String sessionId, String message) {
        if (StrUtil.isEmpty(message)) {
            log.error("message is empty");
            return;
        }
        JSONObject jsonObject = JSON.parseObject(message);
        String clusterName = jsonObject.getString("clusterName");
        String db = jsonObject.getString("db");
        JSONArray timeIntervalArray = jsonObject.getJSONArray("timeInterval");
        String startTime = "";
        String endTime = "";
        if (timeIntervalArray != null && timeIntervalArray.size() >= 2) {
            startTime = timeIntervalArray.getString(0);
            endTime = timeIntervalArray.getString(1);
        }
        int pageNum = jsonObject.getIntValue("pageNum");
        int pageSize = jsonObject.getIntValue("pageSize");
        TrainingConfig config = TrainingConfig.builder().clusterName(clusterName).startTime(startTime).endTime(endTime)
                .db(db).build();
        RespBean respBean = tuningService.getAllTuning(config, pageNum, pageSize);
        wsFacade.sendMessage(PluginExtensionInfoConfig.PLUGIN_ID, sessionId, JSON.toJSONString(respBean));
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