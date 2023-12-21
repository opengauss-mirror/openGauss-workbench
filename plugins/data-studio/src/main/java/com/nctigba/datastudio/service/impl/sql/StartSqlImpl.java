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
 *  StartSqlImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/StartSqlImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.service.impl.debug.AsyncHelper;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.enums.MessageEnum.BUTTON;
import static com.nctigba.datastudio.enums.MessageEnum.DISCONNECTION;
import static com.nctigba.datastudio.enums.MessageEnum.TABLE;
import static com.nctigba.datastudio.enums.MessageEnum.TEXT;
import static com.nctigba.datastudio.enums.MessageEnum.WINDOW;

/**
 * StartSqlImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("startRun")
public class StartSqlImpl implements OperationInterface {
    @Autowired
    private AsyncHelper asyncHelper;

    @Override
    @Async
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        log.info("StartSqlImpl operate obj: " + obj);
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        String windowName = paramReq.getWindowName();
        if (!conMap.containsKey(paramReq.getUuid())) {
            webSocketServer.sendMessage(windowName, DISCONNECTION,
                    LocaleStringUtils.transLanguageWs("1004", webSocketServer), paramReq.getUuid());
        }
        Connection connection = webSocketServer.getConnection(windowName);
        Statement stat = connection.createStatement();
        webSocketServer.setStatement(windowName, stat);
        ThreadUtil.execAsync(() -> {
            List<SqlHistoryDO> list = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SqlHistoryDO sqlHistoryDO = new SqlHistoryDO();
            Date startTime = new Date();
            Date endTime = new Date();
            boolean isSuccess = true;

            try {
                webSocketServer.sendMessage(windowName, TEXT,
                        LocaleStringUtils.transLanguageWs("2001", webSocketServer), null);
                startTime = new Date();
                boolean result = stat.execute(paramReq.getSql());
                endTime = new Date();
                webSocketServer.sendMessage(windowName, BUTTON,
                        LocaleStringUtils.transLanguageWs("2006", webSocketServer), null);
                OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
                operateStatus.enableStopRun();
                webSocketServer.setOperateStatus(windowName, operateStatus);
                while (true) {
                    if (result) {
                        webSocketServer.sendMessage(windowName, TABLE,
                                LocaleStringUtils.transLanguageWs("2002", webSocketServer),
                                DebugUtils.parseResultSetType(stat.getResultSet()));
                        webSocketServer.sendMessage(windowName, TEXT,
                                LocaleStringUtils.transLanguageWs("2002", webSocketServer), null);
                    } else {
                        if (stat.getUpdateCount() != -1) {
                            webSocketServer.sendMessage(windowName, TEXT, LocaleStringUtils.transLanguageWs(
                                    "2005", webSocketServer) + stat.getUpdateCount(), null);
                        } else {
                            break;
                        }
                    }
                    result = stat.getMoreResults();
                }
                webSocketServer.sendMessage(windowName, TEXT,
                        LocaleStringUtils.transLanguageWs("2003", webSocketServer), null);
            } catch (IOException | SQLException e) {
                log.info("StartSqlImpl operate catch: " + e);
                isSuccess = false;
                endTime = new Date();
                try {
                    webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED, e.getMessage(), e.getStackTrace());
                    webSocketServer.sendMessage(windowName, BUTTON,
                            LocaleStringUtils.transLanguageWs("2006", webSocketServer), null);
                    OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
                    operateStatus.enableStopRun();
                    webSocketServer.setOperateStatus(windowName, operateStatus);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                sqlHistoryDO.setStartTime(df.format(startTime));
                sqlHistoryDO.setSuccess(isSuccess);
                sqlHistoryDO.setSql(paramReq.getSql());
                sqlHistoryDO.setExecuteTime((endTime.getTime() - startTime.getTime()) + "ms");
                sqlHistoryDO.setWebUser(paramReq.getWebUser());
                list.add(sqlHistoryDO);
                asyncHelper.insertSqlHistory(list);
                log.info("StartSqlImpl operate finally: " + list);

                try {
                    stat.close();
                    stat.cancel();
                    webSocketServer.setStatement(windowName, null);
                } catch (SQLException e) {
                    log.info("StartSqlImpl operate Exception: " + e);
                }
            }
        });
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
