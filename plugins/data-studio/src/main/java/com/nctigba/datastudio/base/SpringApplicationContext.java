/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.base;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * SpringApplicationContext
 *
 * @since 2023-6-26
 */
@Component
@Lazy(false)
public class SpringApplicationContext implements ApplicationContextAware, DisposableBean {
    private static ApplicationContext applicationContext = null;

    private SpringApplicationContext() {
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        SpringApplicationContext.applicationContext = applicationContext;
    }

    /**
     * clear holder
     */
    public static void clearHolder() {
        applicationContext = null;
    }

    /**
     * destroy
     */
    @Override
    public void destroy() {
        clearHolder();
    }
}