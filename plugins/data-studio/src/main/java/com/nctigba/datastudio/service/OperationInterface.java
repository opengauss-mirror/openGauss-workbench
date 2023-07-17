/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
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