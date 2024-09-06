/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * LogAspect.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/aspectj/LogAspect.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.aspectj;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.opengauss.admin.common.annotation.Log;
import org.opengauss.admin.common.enums.BusinessStatus;
import org.opengauss.admin.common.enums.HttpMethod;
import org.opengauss.admin.common.utils.ServletUtils;
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.common.utils.ip.IpUtils;
import org.opengauss.admin.container.constant.CommonConstant;
import org.opengauss.admin.framework.manager.AsyncManager;
import org.opengauss.admin.framework.manager.factory.AsyncFactory;
import org.opengauss.admin.system.domain.SysOperLog;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

/**
 * Operation log record AOP processing
 *
 * @author xielibo
 * @since 2024-03-11
 */
@Slf4j
@Aspect
@Component
public class LogAspect {
    /**
     * doAfterReturning
     *
     * @param joinPoint     joinPoint
     * @param controllerLog controllerLog
     * @param jsonResult    jsonResult
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * doAfterThrowing
     *
     * @param joinPoint     joinPoint
     * @param controllerLog controllerLog
     * @param e             exception
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    /**
     * handleLog
     *
     * @param joinPoint     joinPoint
     * @param controllerLog controllerLog
     * @param e             exception
     * @param jsonResult    result
     */
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        ServletRequestAttributes sra;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        } else {
            throw new IllegalArgumentException("RequestContextHolder is not ServletRequestAttributes ");
        }
        HttpServletRequest request;
        if (sra.resolveReference(RequestAttributes.REFERENCE_REQUEST) instanceof HttpServletRequest) {
            request = (HttpServletRequest) sra.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        } else {
            throw new IllegalArgumentException("sra is not HttpServletRequest ");
        }
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        String ip = IpUtils.getIpAddr(request);
        operLog.setOperIp(ip);
        operLog.setOperUrl(request.getRequestURI());
        if (e != null) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, CommonConstant.TWO_THOUSAND));
        }
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        operLog.setMethod(className + "." + methodName + "()");
        operLog.setRequestMethod(request.getMethod());
        getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
        RequestContextHolder.setRequestAttributes(sra, true);
        AsyncManager.me().execute(AsyncFactory.recordOper(operLog));
    }

    /**
     * Obtain the description information of the method in the annotation for Controller layer annotation
     *
     * @param joinPoint  joinPoint
     * @param log        log
     * @param operLog    SysOperLog
     * @param jsonResult jsonResult
     * @throws Exception exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, SysOperLog operLog, Object jsonResult) {
        operLog.setBusinessType(log.businessType().ordinal());
        operLog.setTitle(log.title());
        operLog.setOperatorType(log.operatorType().ordinal());
        if (log.isSaveRequestData()) {
            setRequestValue(joinPoint, operLog);
        }
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult)) {
            operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(jsonResult), 0, CommonConstant.TWO_THOUSAND));
        }
    }

    /**
     * Get the parameters of the request and put them in the log
     *
     * @param joinPoint joinPoint
     * @param operLog   operLog
     * @throws Exception Exception
     */
    private void setRequestValue(JoinPoint joinPoint, SysOperLog operLog) {
        String requestMethod = operLog.getRequestMethod();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            operLog.setOperParam(StringUtils.substring(params, 0, CommonConstant.TWO_THOUSAND));
        } else {
            Map<?, ?> paramsMap =
                    (Map<?, ?>) ServletUtils.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            operLog.setOperParam(StringUtils.substring(paramsMap.toString(), 0, CommonConstant.TWO_THOUSAND));
        }
    }

    /**
     * parameter assembly
     *
     * @param paramsArray array
     * @return string
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null) {
            for (Object o : paramsArray) {
                if (StringUtils.isNotNull(o) && !isFilterObject(o)) {
                    Object jsonObj = JSON.toJSON(o);
                    params.append(jsonObj.toString()).append(" ");
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * Determine whether to filter the object
     *
     * @param o object
     * @return boolean
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        }
        if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

}
