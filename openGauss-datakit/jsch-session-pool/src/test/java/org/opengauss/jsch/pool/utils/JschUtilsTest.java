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

import org.junit.jupiter.api.Test;
import org.opengauss.jsch.pool.ExecOperations;
import org.opengauss.jsch.pool.JschSessionPool;
import org.opengauss.jsch.pool.config.SessionConfig;
import org.opengauss.jsch.pool.enums.ChannelType;

import java.util.List;

/**
 * JschUtilsTest
 *
 * @author: wangchao
 * @Date: 2025/8/4 15:53
 * @since 7.0.0-RC2
 **/
@Slf4j
public class JschUtilsTest {
    @Test
    void test() throws Exception {
        SessionConfig execConfig = new SessionConfig("192.168.0.xxx", "root", "xxx@123").withChannelType(
            ChannelType.EXEC).withTimeout(15000);
        // 执行单个命令
        try {
            ExecOperations.ExecResult result = ExecOperations.executeCommand(execConfig, "ls -l /var/log");
            log.info("command exec result :{}", result);
            log.info("exit code: {}", result.getExitCode());
            log.info("output: \n{}", result.getOutput());
            log.info("error: \n{}", result.getError());
        } catch (Exception e) {
            log.error("execute single command failed", e);
        }
        // 执行多个命令
        try {
            List<ExecOperations.ExecResult> results = ExecOperations.executeCommands(execConfig,
                "date; free -m; df -h");
            for (int i = 0; i < results.size(); i++) {
                log.info("command #{} result: {}", (i + 1), results.get(i));
            }
        } catch (Exception e) {
            log.error("execute multiple commands failed", e);
        }
        // 关闭所有连接池（应用退出时调用）
        JschSessionPool.closeAllPools();
    }
}
