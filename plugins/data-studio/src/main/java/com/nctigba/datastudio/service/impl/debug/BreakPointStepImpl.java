package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.BEGIN;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.CONTINUE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.variable;
import static com.nctigba.datastudio.enums.MessageEnum.variableHighLight;

/**
 * break point step
 */
@Slf4j
@Service("breakPointStep")
public class BreakPointStepImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("breakPointStep obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        List<Integer> breakPoints = paramReq.getBreakPoints();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (!operateStatus.isBreakPointStep()) {
            return;
        }

        try {
            Statement stat = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
            if (CollectionUtils.isEmpty(breakPoints)) {
                stat.executeQuery(CONTINUE_SQL);
            } else {
                int begin = (int) webSocketServer.getParamMap(windowName).get(BEGIN);
                if (!breakPoints.contains(begin + 1)) {
                    stat.executeQuery(CONTINUE_SQL);
                }
            }

            int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
            ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

            ResultSet variableResult = stat.executeQuery(INFO_LOCALS_SQL);
            Map<String, Object> variableMap = DebugUtils.parseResultSet(variableResult);
            webSocketServer.sendMessage(windowName, variable, SUCCESS, variableMap);

            int line = 0;
            List<List<Object>> list = (List<List<Object>>) variableMap.get(RESULT);
            log.info("breakPointStep list is: " + list);
            for (int i = list.size() - 1; i >= 0; i--) {
                String o = (String) list.get(i).get(1);
                if (!"0".equals(o)) {
                    line = i;
                    break;
                }
            }
            Map<String, Integer> map = new HashMap<>();
            map.put(RESULT, line);
            webSocketServer.sendMessage(windowName, variableHighLight, SUCCESS, map);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
