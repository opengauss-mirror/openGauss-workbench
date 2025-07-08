/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 */

package org.opengauss.admin.common.utils;

import cn.hutool.core.util.StrUtil;

import org.opengauss.admin.common.exception.ops.OpsException;

import java.util.function.Function;

/**
 * ChainAssert
 *
 * @author: wangchao
 * @Date: 2025/3/18 16:17
 * @Description: ChainAssert
 * @since 7.0.0-RC2
 **/
public class ChainAssert<T> {
    private final T target;

    /**
     * private constructor
     *
     * @param target target object
     */
    private ChainAssert(T target) {
        this.target = target;
    }

    /**
     * create a ChainAssert instance with target object
     *
     * @param target target object
     * @param <T> target object type
     * @return ChainAssert instance
     */
    public static <T> ChainAssert<T> of(T target) {
        return new ChainAssert<>(target);
    }

    /**
     * check target object is not null
     *
     * @param message error message
     * @return ChainAssert instance
     */
    public ChainAssert<T> nonNull(String message) {
        if (target == null) {
            throw new OpsException(message);
        }
        return this;
    }

    /**
     * check target object field is not blank
     *
     * @param fieldGetter getter function for field
     * @param message error message
     * @return ChainAssert instance
     */
    public ChainAssert<T> validateField(Function<T, String> fieldGetter, String message) {
        if (target == null) {
            throw new OpsException("Target object is null");
        }
        String value = fieldGetter.apply(target);
        if (StrUtil.isBlank(value)) {
            throw new OpsException(message);
        }
        return this;
    }

    /**
     * check target object field is none null
     *
     * @param fieldGetter getter function for field
     * @param message error message
     * @return ChainAssert instance
     */
    public ChainAssert<T> validateFieldNoneNull(Function<T, Object> fieldGetter, String message) {
        if (target == null) {
            throw new OpsException("Target object is null");
        }
        Object value = fieldGetter.apply(target);
        if (value == null) {
            throw new OpsException(message);
        }
        return this;
    }

    /**
     * check condition is true
     *
     * @param isCondition isCondition
     * @param message error message
     * @return ChainAssert instance
     */
    public ChainAssert<T> isTrue(boolean isCondition, String message) {
        if (!isCondition) {
            throw new OpsException(message);
        }
        return this;
    }
}
