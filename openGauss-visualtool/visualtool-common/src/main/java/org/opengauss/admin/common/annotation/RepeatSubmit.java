package org.opengauss.admin.common.annotation;

import java.lang.annotation.*;

/**
 * Custom annotations prevent repeated form submissions
 *
 * @author xielibo
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
    /**
     * Interval time (ms), less than this time is regarded as repeated submission
     */
    public int interval() default 5000;

    /**
     * tips
     */
    public String message() default "Duplicate submission not allowed, please try again later";
}
