/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  ScheduleTaskController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/controller/
 *  ScheduleTaskController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.controller;

import com.nctigba.observability.sql.scheduler.DynamicTaskScheduler;
import com.nctigba.observability.sql.service.impl.TimeConfigServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

/**
 * ScheduleTaskController
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@RestController
@RequestMapping("/sqlDiagnosis/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ScheduleTaskController {
    private static final Integer DEFAULT_PEROID = 30;
    private static final Integer DEFAULT_FREQUENCY = 300;

    @Autowired
    private final DynamicTaskScheduler dynamicTaskScheduler;
    @Autowired
    private final TimeConfigServiceImpl timeConfigService;

    /**
     * update frequency from frontend
     *
     * @param frequency String schedule task frequency
     * @return String update message
     */
    @PostMapping("/frequency")
    public String changeFrequency(@RequestParam String frequency) {
        int frequencySeconds = frequency2Seconds(frequency);
        dynamicTaskScheduler.setFrequency(frequencySeconds);
        timeConfigService.persistTimeConfig(null, frequencySeconds);
        return "shcedule task frequency changed to " + frequencySeconds + " s";
    }

    /**
     * update peroid from frontend
     *
     * @param peroid String
     * @return String update message
     */
    @PostMapping("/peroid")
    public String changePeroid(@RequestParam String peroid) {
        int peroidDays = peroid2Days(peroid);
        timeConfigService.persistTimeConfig(peroidDays, null);
        return "slow sql save days changed to " + peroidDays;
    }

    /**
     * get default time config to frontend
     *
     * @return Map<String, Integer> default time config
     */
    @GetMapping("/getDefault")
    public Map<String, String> getDefaultTime() {
        return timeConfigService.getTimeConfig();
    }

    private int frequency2Seconds(String frequency) {
        if (!isRightFrequency(frequency)) {
            log.error("wrong slowsql collect frequency, use default 60s.");
            return DEFAULT_FREQUENCY;
        }
        char unit = frequency.toLowerCase(Locale.ROOT).charAt(frequency.length() - 1);
        String number = frequency.substring(0, frequency.length() - 1);
        int frequencySeconds = Integer.parseInt(number);
        if (unit == 'h') {
            frequencySeconds = frequencySeconds * 3600;
        } else if (unit == 'm') {
            frequencySeconds = frequencySeconds * 60;
        } else {
            return frequencySeconds;
        }
        return frequencySeconds;
    }

    private int peroid2Days(String peroid) {
        if (!isRightPeriod(peroid)) {
            log.error("wrong slow sql lifecycle, use default 30 days.");
            return DEFAULT_PEROID;
        }
        char unit = peroid.toLowerCase(Locale.ROOT).charAt(peroid.length() - 1);
        String number = peroid.substring(0, peroid.length() - 1);
        int peroidDays = Integer.parseInt(number);
        if (unit == 'm') {
            peroidDays = peroidDays * 30;
        }
        return peroidDays;
    }

    private boolean isRightPeriod(String peroid) {
        String lowerPeroid = peroid.toLowerCase(Locale.ROOT);
        return lowerPeroid.endsWith("m") || lowerPeroid.endsWith("d");
    }

    private boolean isRightFrequency(String frequency) {
        String lowerFrequency = frequency.toLowerCase(Locale.ROOT);
        return lowerFrequency.endsWith("s") || lowerFrequency.endsWith("m") || lowerFrequency.endsWith("h");
    }
}
