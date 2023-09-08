/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.annotation;

import com.nctigba.alert.monitor.config.validator.NotNullSummaryValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NotNullSummary is used on a class and works in conjunction with @NotNullConditional to conditionally perform
 * non-null validation on fields.
 *
 * @since 2023/9/5 09:26
 */
@Documented
@Constraint(validatedBy = {NotNullSummaryValidator.class})
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNullSummary {
    String message() default " is not null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
