/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The core Annotation to switch datasource. It can be annotated at method.
 *
 * @since 2023年8月1日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ds {
    /**
     * parameter index of nodeId
     */
    int index() default 0;

    /**
     * if parameter is a map or a bean, key name or property
     */
    String value() default "";
}