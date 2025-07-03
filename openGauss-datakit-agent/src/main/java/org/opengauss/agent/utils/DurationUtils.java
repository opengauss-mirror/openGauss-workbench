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

package org.opengauss.agent.utils;

import org.opengauss.agent.constant.AgentConstants;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * DurationUtils
 *
 * @author: wangchao
 * @Date: 2025/6/6 16:25
 * @since 7.0.0-RC2
 **/
public class DurationUtils {
    /**
     * Convert milliseconds to Duration isoString
     *
     * @param milliseconds milliseconds
     * @return Duration isoString
     */
    public static String formatInterval(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        String iso = duration.toString(); // 示例：PT5S
        return iso.substring(2); // 移除 PT 前缀 → 5S
    }

    /**
     * Convert Duration isoString to milliseconds
     *
     * @param isoString isoString
     * @return milliseconds
     */
    public static long parseToMillis(String isoString) {
        if (isoString.startsWith(AgentConstants.Duration.PREFIX_ONLE_TIME)) {
            // 解析时间部分（小时/分/秒）
            Duration duration = Duration.parse(isoString);
            return duration.toMillis();
        }
        // 分离日期部分（年/月/周/天）和时间部分（小时/分/秒）
        String[] parts = isoString.split(AgentConstants.Duration.SPLIT_DATE_AND_TIME);
        String datePart = parts[0].substring(1); // 去掉前缀P
        String timePart = parts.length > 1 ? parts[1] : "";
        // 解析日期部分（年/月/周/天）
        Period period = Period.parse(AgentConstants.Duration.PREFIX_DATE + datePart);
        LocalDate start = LocalDate.now();
        LocalDate end = start.plus(period);
        long days = ChronoUnit.DAYS.between(start, end);
        // 解析时间部分（小时/分/秒）
        Duration duration = Duration.parse(AgentConstants.Duration.PREFIX_ONLE_TIME + timePart);
        // 合并总毫秒数
        return Duration.ofDays(days).plus(duration).toMillis();
    }

    /**
     * Convert fixed time isoString to milliseconds
     *
     * @param isoString isoString
     * @return milliseconds
     */
    public static long parseFixedTimeToMillis(String isoString) {
        // 分离日期部分（年/月/周/天）和时间部分（小时/分/秒）
        String[] parts = isoString.split(AgentConstants.Duration.SPLIT_DATE_AND_TIME);
        String datePart = parts[0];
        if (datePart.contains(AgentConstants.Duration.YEAR) || datePart.contains(AgentConstants.Duration.MONTH)) {
            throw new IllegalArgumentException("fixed time period must be days");
        }
        String timePart = parts.length > 1 ? parts[1] : "";
        // 解析 周/天
        Period period = Period.parse(datePart);
        long days = period.getDays();
        // 解析时间部分（小时/分/秒）
        Duration duration = Duration.parse(AgentConstants.Duration.PREFIX_ONLE_TIME + timePart);
        // 合并总毫秒数
        return Duration.ofDays(days).plus(duration).toMillis();
    }
}
