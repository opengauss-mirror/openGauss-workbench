/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  NotBlankSummary.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/validator/annotation/NotBlankSummary.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.validator.annotation;

import com.nctigba.alert.monitor.model.validator.NotBlankSummaryValidator;
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
