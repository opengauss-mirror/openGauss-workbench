/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util.bean;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

/**
 * BeanValidators
 *
 * @author liu
 * @since 2022-10-01
 */
public class BeanValidators {
    /**
     * validateWithException
     *
     * @param validator validator
     * @param object    object
     * @param groups    groups
     * @throws ConstraintViolationException ConstraintViolationException
     */
    public static void validateWithException(Validator validator, Object object, Class<?>... groups)
            throws ConstraintViolationException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }
}
