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
 *  SpringApplicationContext.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/base/SpringApplicationContext.java
 *
 *  -------------------------------------------------------------------------
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