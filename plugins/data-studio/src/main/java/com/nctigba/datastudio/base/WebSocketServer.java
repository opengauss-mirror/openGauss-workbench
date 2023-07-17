/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.base;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.starblues.annotation.Extract;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.enums.MessageEnum;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.result.WebDsResult;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.formula.functions.T;
import org.opengauss.admin.system.plugin.extract.SocketExtract;
import org.opengauss.admin.system.plugin.facade.WsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.COMPILE;
import static com.nctigba.datastudio.constants.CommonConstants.EXECUTE;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.START_RUN;
import static com.nctigba.datastudio.constants.CommonConstants.STOP_DEBUG;
import static com.nctigba.datastudio.constants.CommonConstants.STOP_RUN;
import static com.nctigba.datastudio.constants.CommonConstants.TWO_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.WEBDS_PLUGIN;
import static com.nctigba.datastudio.enums.MessageEnum.WINDOW;

/**
 * WebSocketServer
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
@Extract(bus = "webds-plugin")
public class WebSocketServer implements SocketExtract {
    private final Map<String, Map<String, Object>> paramMap = new HashMap<>();
    private final Map<String, Connection> connectionMap = new HashMap<>();
    private final Map<String, Statement> statementMap = new HashMap<>();
    private final Map<String, OperateStatusDO> operationStatusMap = new HashMap<>();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private WsFacade wsFacade;
    private String language = Strings.EMPTY;

    @Override
    public void onOpen(String pluginId, String sessionId, Session session) {
        log.info("onOpen pluginId: " + pluginId + ",sessionId:" + sessionId);
    }

    @Override
    public void processMessage(String sessionId, String message) {
        log.info("sessionId: " + sessionId + ",message:" + message);
        if (StringUtils.isBlank(message)) {
            return;
        }

        boolean isJson = JSONUtil.isJson(message);
        if (!isJson) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(message);
        String operation = jsonObject.getString("operation");
        if (StringUtils.isEmpty(operation)) {
            return;
        }
        OperationInterface<T> aa = SpringApplicationContext.getApplicationContext().getBean(operation,
                OperationInterface.class);
        ThreadUtil.execAsync(() -> {
            try {
                aa.operate(this, message);
            } catch (IOException | SQLException e) {
                try {
                    sendMessage(sessionId, WINDOW, FIVE_HUNDRED, e.getMessage(), e.getStackTrace());
                    if (!operation.equals(START_RUN) && !operation.equals(STOP_RUN)
                            && !operation.equals(COMPILE) && !operation.equals(EXECUTE)) {
                        SpringApplicationContext.getApplicationContext().getBean(STOP_DEBUG, OperationInterface.class)
                                .operate(this, message);
                    }
                } catch (IOException | SQLException ex) {
                    log.error("method error: ", ex);
                }
            }
        });
    }

    @Override
    public void onClose(String pluginId, String sessionId) {
        log.info("onClose pluginId: " + pluginId + ",sessionId:" + sessionId);
        Statement statement = statementMap.get(sessionId);
        Connection connection = connectionMap.get(sessionId);
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }

    /**
     * server send message to client
     *
     * @param sessionId sessionId
     * @param type      type
     * @param code      code
     * @param message   message
     * @param obj       obj
     * @throws IOException IOException
     */
    public synchronized void sendMessage(
            String sessionId, MessageEnum type, String code, String message, Object obj) throws IOException {
        WebDsResult result = WebDsResult.ok(type.toString(), message).addData(obj);
        result.setCode(code);
        wsFacade.sendMessage(WEBDS_PLUGIN, sessionId, objectMapper.writeValueAsString(result));
    }

    /**
     * server send message to client
     *
     * @param sessionId sessionId
     * @param type      type
     * @param message   message
     * @param obj       obj
     * @throws IOException IOException
     */
    public void sendMessage(String sessionId, MessageEnum type, String message, Object obj) throws IOException {
        sendMessage(sessionId, type, TWO_HUNDRED, message, obj);
    }

    public Connection getConnection(String sessionId) {
        return this.connectionMap.get(sessionId);
    }

    public void setConnection(String sessionId, Connection connection) {
        this.connectionMap.put(sessionId, connection);
    }

    /**
     * create connection
     *
     * @param uuid    uuid
     * @param winName winName
     * @return Connection
     * @throws SQLException SQLException
     */
    public Connection createConnection(String uuid, String winName) throws SQLException {
        return SpringApplicationContext.getApplicationContext().getBean("connectionConfig",
                ConnectionConfig.class).connectDatabaseMap(uuid, winName);
    }

    public Statement getStatement(String sessionId) {
        return this.statementMap.get(sessionId);
    }

    public void setStatement(String sessionId, Statement statement) {
        this.statementMap.put(sessionId, statement);
    }

    public OperateStatusDO getOperateStatus(String sessionId) {
        OperateStatusDO operateStatus = this.operationStatusMap.get(sessionId);
        if (operateStatus == null) {
            return new OperateStatusDO();
        }
        return operateStatus;
    }

    public void setOperateStatus(String sessionId, OperateStatusDO operateStatus) {
        this.operationStatusMap.put(sessionId, operateStatus);
    }

    public Map<String, Object> getParamMap(String sessionId) {
        Map<String, Object> map = this.paramMap.get(sessionId);
        if (CollectionUtils.isEmpty(map)) {
            return new HashMap<>();
        }
        return map;
    }

    public void setParamMap(String sessionId, String key, Object obj) {
        Map<String, Object> map = getParamMap(sessionId);
        map.put(key, obj);
        this.paramMap.put(sessionId, map);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
