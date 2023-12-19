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
 *  OperationInterface.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/OperationInterface.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nctigba.datastudio.base.WebSocketServer;

import java.io.IOException;
import java.sql.SQLException;

/**
 * OperationInterface
 *
 * @since 2023-6-26
 */
public interface OperationInterface<T> {
    /**
     * operate
     *
     * @param webSocketServer webSocketServer
     * @param obj obj
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    void operate(WebSocketServer webSocketServer, T obj) throws SQLException, IOException;

    /**
     * operate
     *
     * @param webSocketServer webSocketServer
     * @param str str
     * @throws SQLException SQLException
     * @throws IOException IOException
     */
    default void operate(WebSocketServer webSocketServer, String str) throws SQLException, IOException {
        operate(webSocketServer, formatJson(str));
    }

    /**
     * format json
     *
     * @param str str
     * @return T
     */
    default T formatJson(String str) {
        return JSON.parseObject(str, new TypeReference<T>() {
        });
    }
}