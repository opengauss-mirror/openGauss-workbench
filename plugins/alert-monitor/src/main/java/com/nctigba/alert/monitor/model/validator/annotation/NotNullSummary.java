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
 *  NotNullSummary.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/validator/annotation/NotNullSummary.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.validator.annotation;

import com.nctigba.alert.monitor.model.validator.NotNullSummaryValidator;
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
