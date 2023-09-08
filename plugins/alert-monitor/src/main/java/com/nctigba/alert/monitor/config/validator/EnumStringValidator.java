/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.validator;

import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.config.annotation.EnumString;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * EnumIntegerValidator
 *
 * @since 2023/8/16 09:36
 */
public class EnumStringValidator implements ConstraintValidator<EnumString, String> {
    private String[] arr;

    @Override
    public void initialize(EnumString constraintAnnotation) {
        arr = constraintAnnotation.values();
    }

    @Override
    public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
        if (StrUtil.isBlank(str)) {
            return true;
        }
        if (Arrays.stream(arr).anyMatch(item -> item.equals(str))) {
            return true;
        }
        return false;
    }
}
