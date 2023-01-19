/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.quartz.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ExceptionUtil
 *
 * @author liu
 * @since 2022-10-01
 */
public class ExceptionUtil {
    /**
     * getExceptionMessage
     *
     * @param throwable throwable
     * @return String
     */
    public static String getExceptionMessage(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
