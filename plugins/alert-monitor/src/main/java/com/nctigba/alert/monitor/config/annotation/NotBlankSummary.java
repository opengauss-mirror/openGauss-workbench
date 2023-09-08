/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.annotation;

import com.nctigba.alert.monitor.config.validator.NotBlankSummaryValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NotBlankSummary is used on a class and works in conjunction with @NotBlankConditional to conditionally perform
 * non-blank validation on fields.
 *
 * @since 2023/9/5 10:21
 */
@Documented
@Constraint(validatedBy = {NotBlankSummaryValidator.class})
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankSummary {
    String message() default " is not blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
