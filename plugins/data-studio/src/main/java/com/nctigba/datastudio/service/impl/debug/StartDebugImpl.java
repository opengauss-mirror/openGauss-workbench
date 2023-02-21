package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.FEN_CED_MODE;
import static com.nctigba.datastudio.constants.CommonConstants.LAN_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ALL_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_MODES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_NAMES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_SRC;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_BIN;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_COST;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_IS_STRICT;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_KIND;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_RET_SET;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ROWS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_SHIPPABLE;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_SRC;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_VOLATILE;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SPACE;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.CommonConstants.TYP_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.COMMA;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_LEFT;
import static com.nctigba.datastudio.constants.SqlConstants.PARENTHESES_RIGHT;
import static com.nctigba.datastudio.enums.MessageEnum.paramWindow;
import static com.nctigba.datastudio.enums.MessageEnum.window;

/**
 * start debug
 */
@Slf4j
@Service("startDebug")
public class StartDebugImpl implements OperationInterface {
    @Autowired
    private InputParamImpl inputParam;

    @Autowired
    private FuncProcedureImpl funcProcedure;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("startDebug obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.setDebug(paramReq.isDebug());
        if (!operateStatus.isStartDebug()) {
            return;
        }
        String sql = paramReq.getSql();
        String name = DebugUtils.prepareName(sql);
        String schema = DebugUtils.prepareFuncName(sql).split("\\.")[0];
        log.info("startDebug sql is: " + sql);

        Statement statement = webSocketServer.getStatement(windowName);
        if (statement == null) {
            Connection connection = webSocketServer.getConnection(windowName);
            if (connection == null) {
                connection = webSocketServer.createConnection(paramReq.getUuid(), windowName);
                webSocketServer.setConnection(windowName, connection);
            }
            statement = connection.createStatement();
            webSocketServer.setStatement(windowName, statement);
        }
        ResultSet funcResult = statement.executeQuery(DebugUtils.getFuncSql(windowName, name, webSocketServer));
        Map<String, Object> paramMap = new HashMap<>();
        while (funcResult.next()) {
            paramMap.put(PRO_NAME, funcResult.getString(PRO_NAME));
            paramMap.put(LAN_NAME, funcResult.getString(LAN_NAME));
            paramMap.put(TYP_NAME, funcResult.getString(TYP_NAME));
            paramMap.put(PRO_ARG_TYPES, funcResult.getString(PRO_ARG_TYPES));
            paramMap.put(PRO_ALL_ARG_TYPES, funcResult.getString(PRO_ALL_ARG_TYPES));
            paramMap.put(PRO_ARG_MODES, funcResult.getString(PRO_ARG_MODES));
            paramMap.put(PRO_ARG_NAMES, funcResult.getString(PRO_ARG_NAMES));
            paramMap.put(PRO_SRC, funcResult.getString(PRO_SRC));
            paramMap.put(PRO_BIN, funcResult.getString(PRO_BIN));
            paramMap.put(PRO_ARG_SRC, funcResult.getString(PRO_ARG_SRC));
            paramMap.put(PRO_KIND, funcResult.getString(PRO_KIND));
            paramMap.put(PRO_VOLATILE, funcResult.getString(PRO_VOLATILE));
            paramMap.put(PRO_COST, funcResult.getInt(PRO_COST));
            paramMap.put(PRO_ROWS, funcResult.getInt(PRO_ROWS));
            paramMap.put(FEN_CED_MODE, funcResult.getBoolean(FEN_CED_MODE));
            paramMap.put(PRO_SHIPPABLE, funcResult.getBoolean(PRO_SHIPPABLE));
            paramMap.put(PRO_IS_STRICT, funcResult.getBoolean(PRO_IS_STRICT));
            paramMap.put(PRO_RET_SET, funcResult.getBoolean(PRO_RET_SET));
        }
        log.info("startDebug paramMap is: " + paramMap);

        if (CollectionUtils.isEmpty(paramMap)) {
            webSocketServer.sendMessage(windowName, window, "500",
                    "function/procedure doesn't exist!", null);
            return;
        } else {
            String currentSql = funcProcedure.parseResult(windowName, paramMap, webSocketServer, schema);
            String replaceSql = sql.replace("\n", "").replace(" ", "");
            String replaceCurrentSql = currentSql.replace("\n", "").replace(" ", "");
            if (!replaceSql.equals(replaceCurrentSql)) {
                webSocketServer.sendMessage(windowName, window, "500",
                        "function/procedure has been changed, please compile first!", null);
                return;
            }
        }

        if (operateStatus.isDebug()) {
            operateStatus.enableStopDebug();
            webSocketServer.setOperateStatus(windowName, operateStatus);
        }
        Map<String, List<Map<String, String>>> map = new HashMap<>();
        List<Map<String, String>> itemList = getInputParam(sql);
        map.put(RESULT, itemList);
        log.info("startDebug inputParam map is: " + itemList);

        if (CollectionUtils.isEmpty(itemList)) {
            inputParam.operate(webSocketServer, paramReq);
        } else {
            webSocketServer.sendMessage(windowName, paramWindow, SUCCESS, map);
        }
    }

    private List<Map<String, String>> getInputParam(String sql) {
        List<Integer> leftList = DebugUtils.getIndexList(sql, PARENTHESES_LEFT);
        List<Integer> rightList = DebugUtils.getIndexList(sql, PARENTHESES_RIGHT);
        int start = leftList.get(0);
        int end = rightList.get(rightList.size() - 1);
        sql = sql.replace("\n", " ");
        String substring = sql.substring(start + 1, end);
        log.info("startDebug substring is: " + substring);

        List<Map<String, String>> list = new ArrayList<>();
        if (!StringUtils.isEmpty(substring)) {
            String[] splits = substring.split(COMMA);
            for (String s : splits) {
                if (!s.trim().toLowerCase().startsWith("out")) {
                    Map<String, String> itemMap = new HashMap<>();
                    String[] split = s.trim().split(SPACE);
                    if (split.length == 1) {
                        itemMap.put("", split[0]);
                    } else if (split.length == 3) {
                        itemMap.put(split[1], split[2]);
                    } else {
                        itemMap.put(split[0], split[1]);
                    }
                    list.add(itemMap);
                }
            }
        }
        log.info("startDebug getInputParam list is: " + list);
        return list;
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
