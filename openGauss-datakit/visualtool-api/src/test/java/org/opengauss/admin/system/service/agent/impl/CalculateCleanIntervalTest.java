/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 *   openGauss is licensed under Mulan PSL v2.
 *   You can use this software according to the terms and conditions of the Mulan PSL v2.
 *   You may obtain a copy of Mulan PSL v2 at:
 *
 *   http://license.coscl.org.cn/MulanPSL2
 *
 *   THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *   EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *   MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *   See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.system.service.agent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CalculateCleanIntervalTest
 *
 * @author: wangchao
 * @Date: 2025/5/19 16:06
 * @since 7.0.0-RC2
 **/
@Slf4j
public class CalculateCleanIntervalTest {
    @DisplayName("keep period PT5M")
    @Test
    public void testKeepPeriod5M() {
        String keepPeriod = "PT5M";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(60, interval);
    }

    @DisplayName("keep period PT10M")
    @Test
    public void testKeepPeriod10M() {
        String keepPeriod = "PT10M";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(60, interval);
    }

    @DisplayName("keep period PT5M")
    @Test
    public void testKeepPeriod15M() {
        String keepPeriod = "PT15M";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(90, interval);
    }

    @DisplayName("keep period PT30M")
    @Test
    public void testKeepPeriod30M() {
        String keepPeriod = "PT30M";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(180, interval);
    }

    @DisplayName("keep period PT1H")
    @Test
    public void testKeepPeriod1H() {
        String keepPeriod = "PT1H";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(360, interval);
    }

    @DisplayName("keep period PT2H")
    @Test
    public void testKeepPeriod2H() {
        String keepPeriod = "PT2H";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(3600, interval);
    }

    @DisplayName("keep period PT10H")
    @Test
    public void testKeepPeriod10H() {
        String keepPeriod = "PT10H";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(3600, interval);
    }

    @DisplayName("keep period P1D")
    @Test
    public void testKeepPeriod1D() {
        String keepPeriod = "P1D";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(8640, interval);
    }

    @DisplayName("keep period P7D")
    @Test
    public void testKeepPeriod7D() {
        String keepPeriod = "P7D";
        long interval = calculateCleanInterval(keepPeriod);
        log.info("Clean interval for keepPeriod {}: {} seconds", keepPeriod, interval);
        assertEquals(60480, interval);
    }

    @DisplayName("calc keep period PT1H")
    @Test
    public void test1() {
        Instant currentCleanTime = Instant.now();
        ZonedDateTime zonedCutoffTime = currentCleanTime.atZone(ZoneId.of("GMT+8"))
            .withZoneSameInstant(ZoneId.of("Asia/Shanghai")); // 或 ZoneId.of("UTC+8")
        log.info("calc keep period PT1H {}: {} seconds", zonedCutoffTime, zonedCutoffTime.toInstant());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        log.info("calc keep period PT1H format {}", zonedCutoffTime.format(formatter));
    }

    private long calculateCleanInterval(String keepPeriod) {
        Duration duration = Duration.parse(keepPeriod);
        long seconds = duration.toSeconds();
        long interval = seconds / 10;  // 默认按存储周期的1/10设置清理间隔
        // 短周期任务（<=1小时）的最小清理间隔为1分钟，长周期任务保持原逻辑
        return Math.max(interval, seconds <= 3600 ? 60 : 3600);
    }
}
