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

package org.opengauss.admin.system.utils;

import org.springframework.util.StopWatch;

/**
 * SmartStopWatchFormatter
 *
 * @author: wangchao
 * @Date: 2025/8/1 15:23
 * @since 7.0.0-RC2
 **/
public class SmartStopWatchFormatter {
    /**
     * formart stopwatch print
     *
     * @param stopWatch stopwatch
     * @return format result
     */
    public static String format(StopWatch stopWatch) {
        if (stopWatch.getTotalTimeSeconds() < 0.1) {
            return formatMillis(stopWatch);
        }
        return formatSeconds(stopWatch);
    }

    private static String formatSeconds(StopWatch stopWatch) {
        StringBuilder sb = new StringBuilder("\n" + stopWatch.getId()).append(" stopwatch\n")
            .append("-----------------------------------------\n")
            .append(String.format("%-25s %-12s %s%n", "Task Name", "Time (s)", "% of Total"));
        double totalSeconds = stopWatch.getTotalTimeSeconds();
        for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
            double taskSeconds = task.getTimeSeconds();
            double percentage = totalSeconds > 0 ? (taskSeconds / totalSeconds) * 100 : 0;
            sb.append(String.format("%-25s %-12.3f %.1f%%%n", task.getTaskName(), taskSeconds, percentage));
        }
        sb.append("-----------------------------------------\n")
            .append(String.format("Total time: %.3f seconds%n", totalSeconds));
        return sb.toString();
    }

    private static String formatMillis(StopWatch stopWatch) {
        StringBuilder sb = new StringBuilder("\n" + stopWatch.getId()).append(" stopwatch\n")
            .append("-----------------------------------------\n")
            .append(String.format("%-25s %-12s %s%n", "Task Name", "Time (ms)", "% of Total"));
        double totalMillis = stopWatch.getTotalTimeMillis();
        for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
            double taskMillis = task.getTimeMillis();
            double percentage = totalMillis > 0 ? (taskMillis / totalMillis) * 100 : 0;
            sb.append(String.format("%-25s %-12.0f %.1f%%%n", task.getTaskName(), taskMillis, percentage));
        }
        sb.append("-----------------------------------------\n")
            .append(String.format("Total time: %.0f ms%n", totalMillis));
        return sb.toString();
    }
}
