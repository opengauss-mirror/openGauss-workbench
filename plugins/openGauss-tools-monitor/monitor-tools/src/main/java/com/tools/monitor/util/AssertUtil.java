/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import com.tools.monitor.exception.ParamsException;

/**
 * AssertUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class AssertUtil {
    /**
     * isTrue
     *
     * @param isFlag isFlag
     * @param msg msg
     */
    public static void isTrue(Boolean isFlag, String msg) {
        if (isFlag) {
            throw new ParamsException(msg);
        }
    }
}
