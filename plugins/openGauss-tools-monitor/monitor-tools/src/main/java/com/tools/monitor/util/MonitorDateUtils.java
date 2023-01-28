/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util;

import java.text.ParseException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

/**
 * MonitorDateUtils
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class MonitorDateUtils extends org.apache.commons.lang3.time.DateUtils {
    private static final String[] MONITORPATTERNS = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * parseDate
     *
     * @param object str
     * @return Date
     */
    public static Date MonitorDate(Object object) {
        if (object == null) {
            return new Date();
        }
        try {
            return parseDate(object.toString(), MONITORPATTERNS);
        } catch (ParseException exception) {
            return new Date();
        }
    }
}
