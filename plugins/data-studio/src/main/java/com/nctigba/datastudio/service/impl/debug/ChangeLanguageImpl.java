/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.alibaba.fastjson.JSON;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.service.OperationInterface;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ChangeLanguageImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service("changeLanguage")
public class ChangeLanguageImpl implements OperationInterface {
    @Override
    public void operate(WebSocketServer webSocketServer, Object obj) {
        PublicParamQuery paramReq = DebugUtils.changeParamType(obj);
        log.info("connection paramReq is: " + paramReq);
        webSocketServer.setLanguage(paramReq.getLanguage());
    }

    @Override
    public Object formatJson(String str) {
        return JSON.parseObject(str, PublicParamQuery.class);
    }
}
