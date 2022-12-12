package com.nctigba.observability.instance.factory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.handler.instance.InstanceHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * The database type implementation class of InstanceHandler uses the self implemented bean name of databaseType, such as openGauss<br>
 *
 * @author yangjie
 */

@Slf4j
@Component
public class InstanceHandlerFactory {

    private Map<String, InstanceHandler> handlerMap;

    @Resource
    private List<InstanceHandler> handlerList;

    @PostConstruct
    public void init() {
        handlerMap = handlerList.stream().collect(Collectors.toMap(InstanceHandler::getDatabaseType, Function.identity()));
        log.info("load InstanceHandler complete. entity:{}", JSON.toJSONString(handlerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entity -> AopUtils.getTargetClass(entity.getValue())))));

    }

    /**
     * The processing class of the database is returned according to the database type.
     * If it is not found, the default processing class is returned
     *
     * @param databaseType Database Type
     * @return InstanceHandler Database processing class
     */
    public InstanceHandler getInstance(String databaseType) {
        return handlerMap.containsKey(databaseType) ? handlerMap.get(databaseType) : handlerMap.get(DatabaseType.DEFAULT.getDbType());
    }
}
