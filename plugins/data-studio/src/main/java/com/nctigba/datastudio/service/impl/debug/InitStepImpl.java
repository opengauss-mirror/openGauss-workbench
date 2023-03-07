package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.BACKTRACE_SQL_PRE;
import static com.nctigba.datastudio.constants.SqlConstants.INFO_LOCALS_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.stack;
import static com.nctigba.datastudio.enums.MessageEnum.variable;

/**
 * init step
 */
@Slf4j
@Service("initStep")
public class InitStepImpl implements OperationInterface {
    @Autowired
    private InputParamImpl inputParam;

    @Autowired
    private AddBreakPointImpl addBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("initStep obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        String sql = paramReq.getSql();
        String schema = DebugUtils.prepareFuncName(sql).split("\\.")[0];
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStopDebug();
        webSocketServer.setOperateStatus(windowName, operateStatus);

        Statement stat = (Statement) webSocketServer.getParamMap(oldWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }
        String name = DebugUtils.prepareName(sql);
        ResultSet oidResult = stat.executeQuery(DebugUtils.getFuncSql(oldWindowName, schema, name, webSocketServer));
        String oid = Strings.EMPTY;
        while (oidResult.next()) {
            oid = oidResult.getString(OID);
            webSocketServer.setParamMap(windowName, OID, oid);
        }
        log.info("initStep oid is: " + oid);

        List<Integer> breakPoints = inputParam.getAvailableBreakPoints(windowName, paramReq, webSocketServer);
        log.info("initStep breakPoints is: " + breakPoints);
        for (Integer i : breakPoints) {
            paramReq.setLine(i);
            addBreakPoint.operate(webSocketServer, paramReq);
        }

        try {
            int differ = (int) webSocketServer.getParamMap(windowName).get(DIFFER);
            ResultSet stackResult = stat.executeQuery(BACKTRACE_SQL_PRE + differ + BACKTRACE_SQL);
            webSocketServer.sendMessage(windowName, stack, SUCCESS, DebugUtils.parseResultSet(stackResult));

            ResultSet variableResult = stat.executeQuery(INFO_LOCALS_SQL);
            Map<String, Object> variableMap = DebugUtils.parseResultSet(variableResult);
            webSocketServer.sendMessage(windowName, variable, SUCCESS, DebugUtils.addMapParam(variableMap, paramReq.getSql()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
