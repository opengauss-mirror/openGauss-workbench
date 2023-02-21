package com.nctigba.datastudio.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.nctigba.datastudio.base.WebSocketServer;

public interface OperationInterface<T> {
    void operate(WebSocketServer webSocketServer, T obj) throws Exception;

    default void operate(WebSocketServer webSocketServer, String str) throws Exception {
        operate(webSocketServer, formatJson(str));
    }

    default T formatJson(String str) {
        return JSON.parseObject(str, new TypeReference<T>() {
        });
    }
}