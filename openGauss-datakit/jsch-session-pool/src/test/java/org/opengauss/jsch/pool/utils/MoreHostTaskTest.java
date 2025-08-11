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

package org.opengauss.jsch.pool.utils;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opengauss.jsch.pool.ExecOperations;
import org.opengauss.jsch.pool.JschSessionPool;
import org.opengauss.jsch.pool.config.SessionConfig;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MoreHostTaskTest
 *
 * @author: wangchao
 * @Date: 2025/8/4 17:36
 * @since 7.0.0-RC2
 **/
@Slf4j
public class MoreHostTaskTest {
    private static final String DISK_TOTAL_MONITOR = "df -T --total | tail -n +2| tr -s \" \" | grep total";
    private static final String HOST_BASE_INFO =
        "cat /proc/cpuinfo |grep \"processor\"|wc -l && grep MemFree /proc/meminfo "
            + "| awk '{val=$2/1024}END{print val}' && df -Th | egrep -v \"(tmpfs|sr0)\" | tail -n +2| "
            + "tr -s \" \" | cut -d \" \" -f5 | tr -d \"G\" | awk '{sum+=$1}END{print sum}'";
    private static final String CPU = "lscpu | grep \"CPU(s):\\|Architecture\\|MHz\"";
    private static final String OS
        = "cat /etc/os-release | grep ID= | head -n 1 | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
    private static final String OS_VERSION
        = "cat /etc/os-release | grep VERSION_ID= | head -n 1|awk -F '=' '{print $2}' | sed 's/\\\"//g'";

    private static final String MEMORY = "free -g |head -n 2| tail -n 1  | tr -s \" \"";
    private static final String JAVA_VERSION = "java -version";
    private static final String HOSTNAME = "hostname";
    private static final String CPU_USING =
        "awk 'NR==1 {u=$2; n=$3; s=$4; i=$5; w=$6; x=$7; y=$8; z=$9} END {total=u+n+s+i+w+x+y+z; idle=i; printf \"%"
            + ".1f\\n\", 100 - (idle*100/total)}' /proc/stat";
    private static final String NET_CARD_NAME
        = "export PATH=$PATH:/sbin:/usr/sbin && ip -o addr show | grep %s | awk '{print $2}'";

    private static Map<String, SessionConfig> configs = new ConcurrentHashMap<>();
    private static List<String> commands = new LinkedList<>();

    @BeforeAll
    static void preEnv() {
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        configs.put("192.168.0.xxx", new SessionConfig("192.168.0.xxx", "root", "xxx@123"));
        commands.add(CPU);
        commands.add(DISK_TOTAL_MONITOR);
        commands.add(HOST_BASE_INFO);
        commands.add(OS_VERSION);
        commands.add(OS);
        commands.add(JAVA_VERSION);
        commands.add(HOSTNAME);
        commands.add(MEMORY);
        commands.add(NET_CARD_NAME);
        commands.add(CPU_USING);
    }

    @Test
    void test() {
        configs.forEach((host, config) -> {
            commands.forEach(cmd -> {
                try {
                    ExecOperations.ExecResult result = ExecOperations.executeCommand(config, cmd);
                    log.info("command exec result :{}  {}  {}", config.getHost(), cmd, result);
                } catch (Exception e) {
                    log.error("command exec error :{}  {}  {}", config.getHost(), cmd, e);
                }
            });
        });
        // 关闭所有连接池（应用退出时调用）
        JschSessionPool.closeAllPools();
    }

    @Test
    void testAysnc() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<Runnable> tasks = new LinkedList<>();
        configs.forEach((host, config) -> {
            commands.forEach(cmd -> {
                tasks.add(() -> {
                    try {
                        ExecOperations.ExecResult result = ExecOperations.executeCommand(config, cmd);
                        log.info("command exec result :{}  {}  {}", config.getHost(), cmd, result);
                    } catch (Exception e) {
                        log.error("command exec error :{}  {} ", config.getHost(), cmd, e);
                    }
                });
            });
        });
        // 2. 打乱任务顺序
        Collections.shuffle(tasks);
        // 3. 提交打乱后的任务
        tasks.forEach(executor::submit);
        // 关闭线程池（同之前逻辑）
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        log.info("thread pool stats:{}", JschSessionPool.getPoolStats());
        // 最后关闭所有连接池
        JschSessionPool.closeAllPools();
    }
}
