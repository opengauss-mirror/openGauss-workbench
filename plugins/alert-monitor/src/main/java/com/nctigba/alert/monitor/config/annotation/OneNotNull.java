/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.config.annotation;

import com.nctigba.alert.monitor.config.validator.OneNotNullValidator;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * At least one field is not blank
 *
 * @since 2023/9/5 11:21
 */
@Documented
@Constraint(validatedBy = {OneNotNullValidator.class})
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneNotNull {
    String message() default "At least one field is not blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String conditionalField() default "";
    String[] conditionalValues() default "";
    String[] checkFields();

    @Target({ElementType.TYPE, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        OneNotNull[] value();
    }
}
