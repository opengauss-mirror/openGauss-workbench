package com.nctigba.observability.instance.factory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.nctigba.observability.instance.constants.OSType;
import com.nctigba.observability.instance.handler.os.OSHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

/**
 * The server type implementation class of the OSHandler uses the bean name implemented by the osType itself, such as CentOS<br>
 *
 * @author yangjie
 */

@Slf4j
@Component
public class OSHandlerFactory {

    private Map<String, OSHandler> handlerMap;

    @Resource
    private List<OSHandler> handlerList;

    @PostConstruct
    public void init() {
        handlerMap = handlerList.stream().collect(Collectors.toMap(OSHandler::getOSType, Function.identity()));
        log.info("load OSHandler complete. entity:{}", JSON.toJSONString(handlerMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entity -> AopUtils.getTargetClass(entity.getValue())))));

    }

    /**
     * Returns the processing class of the operating system based on the server operating system type. If not found, returns the default processing class
     *
     * @param osType Database Type
     * @return OSHandler Database processing class
     */
    public OSHandler getInstance(String osType) {
        return handlerMap.containsKey(osType) ? handlerMap.get(osType) : handlerMap.get(OSType.DEFAULT.getOsType());
    }
}
