/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.config.common.Constant;

/**
 * DateUtil
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class DateUtil {
    /**
     * calculateMinutes
     *
     * @param timeStr1 timeStr1
     * @param timeStr2 timeStr2
     * @return String
     */
    public static String calculateMinutes(String timeStr1, String timeStr2) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constant.TIME_FORMAT);
        try {
            Date date1 = sdf.parse(timeStr1);
            Date date2 = sdf.parse(timeStr2);
            long differenceInMillis = Math.abs(date1.getTime() - date2.getTime());
            long minutesDifference = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis);
            return String.valueOf(Math.max(1, minutesDifference));
        } catch (ParseException exception) {
            log.error("calculateMinutesDifference fail");
            return "1";
        }
    }

    /**
     * afterTime
     *
     * @param startTime startTime
     * @return boolean boolean
     */
    public static boolean afterTime(String startTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.TIME_FORMAT);
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
        return startDateTime.isAfter(currentDateTime);
    }

    /**
     * getTimeNow
     *
     * @return String String
     */
    public static String getTimeNow() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");
        LocalDateTime ldt = LocalDateTime.now();
        return ldt.format(dtf);
    }

    /**
     * getDate
     *
     * @return String String
     */
    public static String getDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_");
        LocalDateTime ldt = LocalDateTime.now();
        return ldt.format(dtf);
    }

    /**
     * stringToDate
     *
     * @param dateString dateString
     * @return Date
     */
    public static Date stringToDate(String dateString) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constant.TIME_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, dtf);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * dateToString
     *
     * @param date date
     * @return String String
     */
    public static String dateToString(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Constant.TIME_FORMAT);
        return localDateTime.format(dtf);
    }

    /**
     * addMinutesToDate
     *
     * @param date    date
     * @param minutes minutes
     * @return Date Date
     */
    public static Date addMinutesToDate(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * addSecondsToDate
     *
     * @param date    date
     * @param seconds seconds
     * @return Date
     */
    public static Date addSecondsToDate(Date date, int seconds) {
        // 创建一个Calendar实例
        Calendar calendar = Calendar.getInstance();
        // 将Date对象设置为指定的时间
        calendar.setTime(date);
        // 给Date对象加上指定的秒数
        calendar.add(Calendar.SECOND, seconds);
        // 获取加上指定秒数后的新Date对象
        return calendar.getTime();
    }

    /**
     * getInterval
     *
     * @param executionTime executionTime
     * @return int int
     */
    public static int getInterval(String executionTime) {
        int num = Integer.parseInt(executionTime);
        if (num < Constant.LESS_THAN) {
            return 10;
        } else if (num < Constant.MORE_THAN) {
            return 20;
        } else {
            return 30;
        }
    }
}
