package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.PublicParamReq;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.FUNC_OID;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.constants.SqlConstants.FINISH_SQL;
import static com.nctigba.datastudio.enums.MessageEnum.closeWindow;

/**
 * step out
 */
@Slf4j
@Service("stepOut")
public class StepOutImpl implements OperationInterface {
    @Autowired
    private SingleStepImpl singleStep;

    @Autowired
    private DeleteBreakPointImpl deleteBreakPoint;

    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws Exception {
        log.info("stepOut obj is: " + obj);
        PublicParamReq paramReq = (PublicParamReq) obj;
        String windowName = paramReq.getWindowName();
        String oldWindowName = paramReq.getOldWindowName();
        Statement stat = (Statement) webSocketServer.getParamMap(oldWindowName).get(STATEMENT);
        if (stat == null) {
            return;
        }

        Map<Integer, String> breakPointMap = (Map<Integer, String>) webSocketServer.getParamMap(windowName).get(BREAK_POINT);
        log.info("stepOut breakPointMap is: " + breakPointMap);
        if (!CollectionUtils.isEmpty(breakPointMap)) {
            Set<Integer> integers = breakPointMap.keySet();
            List<Integer> list = new ArrayList<>(integers);
            for (Integer integer : list) {
                paramReq.setLine(integer);
                deleteBreakPoint.operate(webSocketServer, paramReq);
            }
        }

        String oid = (String) webSocketServer.getParamMap(windowName).get(OID);
        log.info("stepOut oid is: " + oid);
        ResultSet resultSet = stat.executeQuery(FINISH_SQL);
        String newOid = Strings.EMPTY;
        while (resultSet.next()) {
            newOid = resultSet.getString(FUNC_OID);
            log.info("stepOut newOid is: " + newOid);
        }

        if (!oid.equals(newOid) && !paramReq.isCloseWindow()) {
            webSocketServer.sendMessage(windowName, closeWindow, SUCCESS, null);
        }
        singleStep.showDebugInfo(webSocketServer, paramReq);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
