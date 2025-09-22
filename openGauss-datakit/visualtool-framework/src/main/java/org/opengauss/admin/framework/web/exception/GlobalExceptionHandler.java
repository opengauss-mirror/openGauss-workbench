/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * GlobalExceptionHandler.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-framework/src/main/java/org/opengauss/admin/framework/web/exception/GlobalExceptionHandler.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.framework.web.exception;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.enums.ResponseCode;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler
 *
 * @author xielibo
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public AjaxResult handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("Validation failed: ");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append("：").append(fieldError.getDefaultMessage()).append(", ");
        }
        String msg = sb.toString();
        String requestUri = request.getRequestURI();
        log.error("Parameter exception occurred. Request URL'{}', params{}, message{}", requestUri, JSONObject.toJSONString(request.getParameterMap()), msg);
        return AjaxResult.error(ResponseCode.BAD_REQUEST.code(), msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AjaxResult handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException ex) {
        String requestUri = request.getRequestURI();
        log.error("Parameter exception occurred. Request URL'{}', params{}, message{}", requestUri, JSONObject.toJSONString(request.getParameterMap()), ex.getMessage());
        return AjaxResult.error(ResponseCode.BAD_REQUEST.code(), ResponseCode.BAD_REQUEST.msg());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public AjaxResult handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining());
        String requestUri = request.getRequestURI();
        log.error("Parameter exception occurred. Request URL'{}', params{}, message{}", requestUri, JSONObject.toJSONString(request.getParameterMap()), message);
        return AjaxResult.error(ResponseCode.BAD_REQUEST.code(), ResponseCode.BAD_REQUEST.msg());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public AjaxResult handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException ex) {
        String requestUri = request.getRequestURI();
        log.error("Parameter exception occurred. Request URL'{}', params{}, message{}", requestUri, JSONObject.toJSONString(request.getParameterMap()), ex.getMessage());
        return AjaxResult.error(ResponseCode.BAD_REQUEST.code(), ResponseCode.BAD_REQUEST.msg());
    }

    /**
     * Permission verification exception
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("Permission verification exception. Request URL'{}', message{}", requestUri, e.getMessage());
        return AjaxResult.error(ResponseCode.FORBIDDEN.code());
    }

    /**
     * The request method does not support
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public AjaxResult handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("The request method does not support. Request URL'{}',Not Support'{}' {}", requestUri, e.getMethod(), e.getMessage());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * Business Error
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? AjaxResult.error(code, e.getMessage()) : AjaxResult.error(e.getMessage());
    }

    /**
     * unknown runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public AjaxResult handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        StringWriter errorsWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorsWriter));
        log.error("Unknown runtime exception. Request URL'{}' {}", requestUri, errorsWriter.toString());
        return AjaxResult.error(e.getMessage());
    }

    /**
     * System Error
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("System Error. Request URL '{}'  {}", requestUri, e.getMessage());
        return AjaxResult.error(e.getMessage());
    }
    @ExceptionHandler(BindException.class)
    public AjaxResult handleBindException(BindException e,  HttpServletRequest request) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        String requestUri = request.getRequestURI();
        log.error("System Error. Request URL '{}'  {}", requestUri, message);
        return AjaxResult.error(message);
    }
}
