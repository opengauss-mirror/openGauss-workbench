package com.tools.monitor.quartz.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/**
 * CronUtils
 *
 * @author liu
 * @since 2022-10-01
 */
public class CronUtils {
    /**
     * isValid
     *
     * @param cronExpression
     * @return
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * getInvalidMessage
     *
     * @param cronExpression
     * @return
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * getNextExecution
     *
     * @param cronExpression
     * @return
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
