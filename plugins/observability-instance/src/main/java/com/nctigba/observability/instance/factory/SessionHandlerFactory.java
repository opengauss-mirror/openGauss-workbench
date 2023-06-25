/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.factory;

import com.alibaba.fastjson.JSON;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.handler.session.SessionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SessionHandlerFactory {
    private Map<String, SessionHandler> handlerMap;

    @Resource
    private List<SessionHandler> handlerList;

    @PostConstruct
    public void init() {
        handlerMap = handlerList.stream()
                .collect(Collectors.toMap(SessionHandler::getDatabaseType, Function.identity()));
        log.info("load TopSQLHandler complete. entity:{}", JSON.toJSONString(handlerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entity -> AopUtils.getTargetClass(entity.getValue())))));

    }

    /**
     * get TopSQL handler for different types of databases
     * 
     * @param databaseType database type
     * @return TopSQL handler
     */
    public SessionHandler getInstance(String databaseType) {
        return handlerMap.containsKey(databaseType) ? handlerMap.get(databaseType)
                : handlerMap.get(DatabaseType.DEFAULT.getDbType());
    }
}
