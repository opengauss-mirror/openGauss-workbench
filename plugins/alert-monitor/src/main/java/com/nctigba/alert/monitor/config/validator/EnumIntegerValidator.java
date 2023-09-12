/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import com.nctigba.alert.monitor.config.annotation.EnumInteger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * EnumIntegerValidator
 *
 * @since 2023/8/16 09:48
 */
public class EnumIntegerValidator implements ConstraintValidator<EnumInteger, Integer> {
    private int[] arr;

    @Override
    public void initialize(EnumInteger constraintAnnotation) {
        arr = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        if (integer == null) {
            return true;
        }
        if (Arrays.stream(arr).anyMatch(item -> integer.equals(item))) {
            return true;
        }
        return false;
    }
}
