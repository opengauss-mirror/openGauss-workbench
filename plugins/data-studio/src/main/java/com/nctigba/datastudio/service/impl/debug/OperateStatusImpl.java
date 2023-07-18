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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.RESULT;
import static com.nctigba.datastudio.constants.CommonConstants.SUCCESS;
import static com.nctigba.datastudio.enums.MessageEnum.OPERATE_STATUS;

/**
 * OperateStatusImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("operateStatus")
public class OperateStatusImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) throws IOException {
        log.info("operateStatus obj is: " + obj);
        PublicParamReq paramReq = DebugUtils.changeParamType(obj);
        String windowName = paramReq.getWindowName();
        OperateStatusDO operateStatusDO = webSocketServer.getOperateStatus(windowName);
        Map<String, Object> map = new HashMap<>();
        map.put(RESULT, operateStatusDO);
        webSocketServer.sendMessage(windowName, OPERATE_STATUS, SUCCESS, map);
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamReq.class);
    }
}
