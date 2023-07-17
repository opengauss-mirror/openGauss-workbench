/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DEFINITION;
import static com.nctigba.datastudio.constants.CommonConstants.FIVE_HUNDRED;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_DEF_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.PARAM_WINDOW;
import static com.nctigba.datastudio.enums.MessageEnum.WINDOW;

/**
 * StartDebugImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("startDebug")
public class StartDebugImpl implements OperationInterface {
    @Autowired
    private InputParamImpl inputParam;

    @Autowired
    private FuncProcedureImpl funcProcedure;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws SQLException, IOException {
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        log.info("startDebug paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String windowName = paramReq.getWindowName();
        Statement statement = webSocketServer.getStatement(rootWindowName);
        if (statement == null) {
            statement = webSocketServer.getConnection(rootWindowName).createStatement();
            webSocketServer.setStatement(rootWindowName, statement);
        }

        String oid = paramReq.getOid();
        String definition = Strings.EMPTY;
        try (
                ResultSet defResultSet = statement.executeQuery(String.format(QUERY_DEF_SQL, oid))
        ) {
            while (defResultSet.next()) {
                definition = defResultSet.getString(DEFINITION);
                if (StringUtils.isEmpty(definition)) {
                    throw new CustomException(LocaleString.transLanguageWs("2015", webSocketServer));
                } else {
                    definition = DebugUtils.sqlHandleAfter(definition);
                }
            }
            log.info("startDebug definition: " + definition);
        }

        if (!paramReq.getSql().equals(definition)) {
            webSocketServer.sendMessage(windowName, WINDOW, FIVE_HUNDRED,
                    LocaleString.transLanguageWs("1007", webSocketServer), null);
            return;
        }

        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.setDebug(true);
        operateStatus.enableStopDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        List<Map<String, Object>> paramList = DebugUtils.getParamMap(webSocketServer, windowName, oid);
        log.info("startDebug paramList: " + paramList);
        map.put(RESULT, paramList);
        if (CollectionUtils.isEmpty(paramList)) {
            inputParam.operate(webSocketServer, paramReq);
        } else {
            webSocketServer.sendMessage(windowName, PARAM_WINDOW, SUCCESS, map);
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
