/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.aop;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Component;

import com.nctigba.observability.instance.service.ClusterManager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * aop for switch data source, using nodeId or clusterId
 *
 * @since 2023年8月1日
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DatasourceAspect {
    private final ClusterManager clusterManager;

    /**
     * {@code @Ds @interface} used by this join point
     *
     * @param joinPoint point cut object
     * @return target method result
     * @throws Throwable target method Throwable
     */
    @Around("@annotation(com.nctigba.observability.instance.aop.Ds)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        Class<?> declaringType = signature.getDeclaringType();

        Ds annotation = declaringType.getMethod(signature.getName(), getParameterTypes(joinPoint))
                .getAnnotation(Ds.class);
        int index = annotation.index();
        if (args.length <= index) {
            index = args.length - 1;
        }
        var obj = args[index];
        String path = annotation.value();
        Object nodeId;
        if (StrUtil.isNotBlank(path)) {
            if (obj instanceof Map) {
                nodeId = ((Map<?, ?>) obj).get(path);
            } else {
                nodeId = BeanUtil.getProperty(obj, path);
            }
        } else {
            nodeId = obj;
        }
        if (StrUtil.isBlankIfStr(nodeId)) {
            throw new CustomException("node not selected");
        }
        try {
            clusterManager.setCurrentDatasource(nodeId.toString());
            return joinPoint.proceed();
        } finally {
            clusterManager.pool();
        }
    }

    private Class<?>[] getParameterTypes(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) signature;
            return methodSignature.getMethod().getParameterTypes();
        }

        // unused for code check
        return DatasourceAspect.class.getClasses();
    }
}
