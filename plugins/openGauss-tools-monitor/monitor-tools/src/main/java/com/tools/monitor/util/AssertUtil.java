package com.tools.monitor.util;

import com.tools.monitor.exception.ParamsException;

/**
 * AssertUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class AssertUtil {
    public static void isTrue(Boolean flag, String msg) {
        if (flag) {
            throw new ParamsException(msg);
        }
    }
}
