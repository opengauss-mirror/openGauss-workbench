package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.service.OperationInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.enums.MessageEnum.OTHER;

/**
 * AnonymousBlockImpl
 *
 * @since 2023-8-17
 */
@Slf4j
@Service("anonymousBlock")
public class AnonymousBlockImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws IOException {
        PublicParamQuery paramReq = (PublicParamQuery) obj;
        log.info("funcProcedure paramReq: " + paramReq);

        String rootWindowName = paramReq.getRootWindowName();
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatus = webSocketServer.getOperateStatus(windowName);
        operateStatus.enableStartAnonymous();
        webSocketServer.setOperateStatus(windowName, operateStatus);
        webSocketServer.sendMessage(windowName, OTHER, SUCCESS, null);
        webSocketServer.setParamMap(rootWindowName, "0", windowName);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
