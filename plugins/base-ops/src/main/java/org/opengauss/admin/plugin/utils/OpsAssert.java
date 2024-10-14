/*
 * Copyright (c) 2024-2024 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsAssert.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/utils/OpsAssert.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import org.opengauss.admin.common.exception.ops.OpsException;

import java.util.Objects;

/**
 * OpsAssert
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
public class OpsAssert {
    /**
     * Assert a boolean expression, throwing an {@code OpsException}
     * if the expression evaluates to {@code false}.
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     *
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws OpsException if {@code expression} is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new OpsException(message);
        }
    }

    /**
     * Assert a object non-null, throwing an {@code OpsException}
     * if object is null ,then throwing an {@code OpsException}
     *
     * @param object a  Object
     * @param message the exception message to use if the assertion fails
     * @throws OpsException if {@code expression} is {@code false}
     */
    public static void nonNull(Object object, String message) {
        if (Objects.isNull(object)) {
            throw new OpsException(message);
        }
    }
}
