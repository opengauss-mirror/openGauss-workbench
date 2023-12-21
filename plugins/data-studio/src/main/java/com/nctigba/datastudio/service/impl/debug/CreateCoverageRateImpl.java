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
 *  CreateCoverageRateImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/CreateCoverageRateImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
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
import static com.nctigba.datastudio.constants.CommonConstants.LINE_NO;
import static com.nctigba.datastudio.constants.CommonConstants.NODE_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PORT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.T_STR;
import static com.nctigba.datastudio.constants.SqlConstants.ATTACH_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_COVERAGE_SQL;
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
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("createCoverage paramReq: " + paramReq);
        String windowName = paramReq.getWindowName();
        Connection connection = webSocketServer.getConnection(windowName);
        Statement statement = connection.createStatement();
        webSocketServer.setStatement(windowName, statement);

        String oid = paramReq.getOid();
        String nodeName = DebugUtils.changeParamType(webSocketServer, windowName, NODE_NAME);
        String port = DebugUtils.changeParamType(webSocketServer, windowName, PORT);

        try (
                ResultSet turnNoResult = statement.executeQuery(String.format(TURN_ON_SQL, oid))
        ) {
            while (turnNoResult.next()) {
                nodeName = turnNoResult.getString(NODE_NAME);
                port = turnNoResult.getString(PORT);
                log.info("inputParam nodeName and port is: " + nodeName + "---" + port);
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        } finally {
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
    }

    private void assembleParam(
            PublicParamQuery paramReq, Connection connection, String sql, Statement statNew,
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
            Connection connection, PublicParamQuery paramReq, String runLines,
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
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
