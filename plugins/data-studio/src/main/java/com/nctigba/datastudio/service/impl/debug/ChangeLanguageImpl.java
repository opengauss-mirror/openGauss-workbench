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
 *  ChangeLanguageImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/debug/ChangeLanguageImpl.java
 *
 *  -------------------------------------------------------------------------
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
