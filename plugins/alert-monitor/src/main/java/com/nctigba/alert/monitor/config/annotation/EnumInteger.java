/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.annotation;

import com.nctigba.alert.monitor.config.validator.EnumIntegerValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnumValue
 *
 * @since 2023/8/16 09:33
 */
@Documented
@Constraint(validatedBy = {EnumIntegerValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumInteger {
    String message() default "value is not invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] values();
}
