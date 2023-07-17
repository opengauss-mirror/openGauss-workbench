/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.NODE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PORT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.T_STR;
import static com.nctigba.datastudio.constants.SqlConstants.ATTACH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_COVERAGE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DEBUG_SERVER_INFO_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_CODE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INSERT_COVERAGE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.TURN_ON_SQL;

/**
 * CreateCoverageRateImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("createCoverageRate")
public class CreateCoverageRateImpl implements OperationInterface {
    @Autowired
    private AsyncHelper asyncHelper;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("createCoverage paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Connection connection = webSocketServer.getConnection(windowName);
        Statement statement = connection.createStatement();
        webSocketServer.setStatement(windowName, statement);

        String oid = paramReq.getOid();
        String nodeName = Strings.EMPTY;
        String port = Strings.EMPTY;
        try (
                ResultSet serverInfo = statement.executeQuery(DEBUG_SERVER_INFO_SQL)
        ) {
            while (serverInfo.next()) {
                String oldOid = serverInfo.getString(FUNC_OID);
                if (oldOid.equals(oid)) {
                    nodeName = serverInfo.getString(NODE_NAME);
                    port = serverInfo.getString(PORT);
                }
            }
            log.info("createCoverage nodeName and port is: " + nodeName + "---" + port);
        }

        if (StringUtils.isEmpty(nodeName) && StringUtils.isEmpty(port)) {
            try (
                    ResultSet turnNoResult = statement.executeQuery(String.format(TURN_ON_SQL, oid))
            ) {
                while (turnNoResult.next()) {
                    nodeName = turnNoResult.getString(NODE_NAME);
                    port = turnNoResult.getString(PORT);
                    log.info("inputParam nodeName and port is: " + nodeName + "---" + port);
                }
            }
        }

        boolean isCoverage = paramReq.isCoverage();
        paramReq.setCoverage(true);
        asyncHelper.task(webSocketServer, paramReq);

        Connection conn = webSocketServer.createConnection(paramReq.getUuid(), windowName);
        Statement statNew = conn.createStatement();
        webSocketServer.setParamMap(windowName, CONNECTION, conn);
        webSocketServer.setParamMap(windowName, STATEMENT, statNew);
        int differ = DebugUtils.changeParamType(webSocketServer, windowName, DIFFER);
        String sql = String.format(ATTACH_SQL, nodeName, port);

        if (isCoverage) {
            assembleParam(paramReq, connection, sql, statNew, differ);
        } else {
            statNew.execute(sql);
            statNew.executeQuery(CONTINUE_SQL);
        }
    }

    private void assembleParam(
            PublicParamReq paramReq, Connection connection, String sql, Statement statNew,
            int differ) throws SQLException {
        StringBuilder runLines = new StringBuilder();
        try (
                ResultSet rs = statNew.executeQuery(sql)
        ) {
            while (rs.next()) {
                runLines.append(rs.getInt(LINE_NO) + differ).append(COMMA);
            }
        }
        statNew.execute(CREATE_COVERAGE_SQL);
        StringBuilder allLines = new StringBuilder();
        try (
                ResultSet allLineResult = statNew.executeQuery(String.format(INFO_CODE_SQL, paramReq.getOid()))
        ) {
            while (allLineResult.next()) {
                if (T_STR.equals(allLineResult.getString(CAN_BREAK))) {
                    allLines.append(allLineResult.getInt(LINE_NO) + differ).append(COMMA);
                }
            }
        }
        allLines.deleteCharAt(allLines.length() - 1);
        log.info("createCoverage allLines: " + allLines);

        int index = 0;
        do {
            try (
                    ResultSet resultSet = statNew.executeQuery(NEXT_SQL)
            ) {
                if (resultSet.next()) {
                    index = resultSet.getInt(LINE_NO);
                    if (index != 0) {
                        runLines.append(index + differ).append(COMMA);
                    }
                }
            }
        } while (index != 0);
        runLines.deleteCharAt(runLines.length() - 1);
        log.info("createCoverage runLines: " + runLines);
        insertTable(connection, paramReq, runLines.toString(), allLines.toString());
    }

    private void insertTable(
            Connection connection, PublicParamReq paramReq, String runLines,
            String allLines) throws SQLException {
        List<Object> list = new ArrayList<>();
        List<Map<String, Object>> inputParamsList = paramReq.getInputParams();
        for (Map<String, Object> map : inputParamsList) {
            Set<String> keys = map.keySet();
            for (String key : keys) {
                list.add(map.get(key));
            }
        }

        try (
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_COVERAGE_SQL)
        ) {
            preparedStatement.setObject(1, paramReq.getOid());
            preparedStatement.setObject(2, System.currentTimeMillis());
            preparedStatement.setObject(3, runLines);
            preparedStatement.setObject(4, null);
            preparedStatement.setObject(5, System.currentTimeMillis());
            preparedStatement.setObject(6, paramReq.getSql());
            preparedStatement.setObject(7, list.toString());
            preparedStatement.setObject(8, allLines);
            preparedStatement.execute();
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
