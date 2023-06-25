/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.alertcenter.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wuyuebin
 * @date 2023/6/16 14:53
 * @description 常驻参数说明
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AlertContentParam {
    String name() default "";

    String preVal() default "";

    boolean isI18nPreVal() default false;

    String[] group() default {};
}
