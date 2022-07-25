package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.NEXT_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.variable;
import static com.nctigba.datastudio.enums.MessageEnum.variableHighLight;

/**
 * single step
 */
@Slf4j
@Service("singleStep")
public class SingleStepImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("singleStep obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        if (!operateStatus.isSingleStep()) {
            return;
        }

        int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
        Statement stat = (Statement) webSocketServer.getParamMap(windowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        try {
            stat.execute(NEXT_SQL);
            ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

            ResultSet variableResult = stat.executeQuery(INFO_LOCALS_SQL);
            Map<String, Object> variableMap = DebugUtils.parseResultSet(variableResult);
            webSocketServer.sendMessage(windowName, variable, SUCCESS, variableMap);

            int line = 0;
            List<List<Object>> list = (List<List<Object>>) variableMap.get(RESULT);
            log.info("singleStep list is: " + list);
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
