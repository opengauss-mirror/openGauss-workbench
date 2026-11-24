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

package com.nctigba.alert.monitor.model.validator;

import cn.hutool.core.util.StrUtil;
import com.nctigba.alert.monitor.model.validator.annotation.EnumString;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
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
