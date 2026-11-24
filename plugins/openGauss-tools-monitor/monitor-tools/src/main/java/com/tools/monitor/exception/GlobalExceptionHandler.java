/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.tools.monitor.exception;

import com.tools.monitor.entity.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 功能描述
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * globalException
     *
     * @param exception exception
     * @return ResponseVO
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseVO globalException(Exception exception) {
        log.error("globalException-->{}", exception.getMessage());
        return ResponseVO.exceptionResponseVO(exception);
    }

    /**
     * paramsExceptionHandler
     *
     * @param exception exception
     * @return ResponseVO
     */
    @ExceptionHandler(ParamsException.class)
    @ResponseBody
    public ResponseVO paramsExceptionHandler(ParamsException exception) {
        if (exception.getMessage().equals("Unable to generate indicator")) {
            return ResponseVO.errorTarget(exception.getMessage());
        }
        return ResponseVO.errorResponseVO(exception.getMessage());
    }

    /**
     * exceptionHandler
     *
     * @param ex      ex
     * @param request request
     * @return ResponseVO
     */
    @ExceptionHandler({ConstraintViolationException.class, BindException.class})
    public ResponseVO exceptionHandler(Exception ex, HttpServletRequest request) {
        log.error("exceptionHandler-->{}", ex.getMessage());
        String msg = "";
        if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) ex;
            ConstraintViolation<?> next = constraintViolationException.getConstraintViolations().iterator().next();
            msg = next.getMessage();
        } else if (ex instanceof BindException) {
            BindException bindException = (BindException) ex;
            msg = bindException.getBindingResult().getFieldError().getDefaultMessage();
        } else {
            log.info("exceptionHandler");
        }
        return ResponseVO.errorResponseVO(msg);
    }
}
