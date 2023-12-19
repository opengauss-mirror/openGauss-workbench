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
 *  DatasourceAspect.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/aspectj/DatasourceAspect.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.aspectj;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.aspectj.annotation.Ds;
import com.nctigba.alert.monitor.util.ClusterManagerUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.opengauss.admin.common.exception.CustomException;


import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * aop for switch data source, using nodeId
 *
 * @since 2023/12/11 09:24
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DatasourceAspect {
    private final ClusterManagerUtils clusterManagerUtils;

    /**
     * {@code @Ds @interface} used by this join point
     *
     * @param joinPoint point cut object
     * @return target method result
     * @throws Throwable target method Throwable
     */
    @Around("@annotation(com.nctigba.alert.monitor.aspectj.annotation.Ds)")
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
            clusterManagerUtils.setCurrentDatasource(nodeId.toString());
            return joinPoint.proceed();
        } finally {
            clusterManagerUtils.pool();
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
