/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
