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

package org.opengauss.tun.utils;

import cn.hutool.core.util.StrUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.tun.common.FixedTuning;

/**
 * CommandLineRunner
 *
 * @author liu
 * @since 2023-12-07
 */
@Slf4j
public class CommandLineRunner {
    /**
     * runCommand
     *
     * @param command   command
     * @param filePath  filePath
     * @param writePath writePath
     * @param timeOut   timeOut
     * @return boolean
     */
    public static boolean runCommand(String command, String filePath, String writePath, long timeOut) {
        String timeNow = DateUtil.getTimeNow();
        String execute = command;
        // Unable to print password information for data bureau
        if (command.contains("--pgsql-password")) {
            execute = "sysbench --db-driver=pgsql ........";
        }
        String content = String.join(StrUtil.LF, timeNow, execute, filePath);
        appendToFile(content, writePath);
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.directory(new File(filePath));
            Process process = builder.start();
            String output = readFromStream(process.getInputStream());
            String error = readFromStream(process.getErrorStream());
            process.waitFor(timeOut, TimeUnit.MINUTES);
            StringBuilder result = new StringBuilder();
            result.append(output).append(StrUtil.LF).append(error);
            appendToFile(result.toString(), writePath);
            String res = result.toString();
            return res.contains(FixedTuning.SUCCESS_INSTALL) || res.equals(FixedTuning.BOOL_TRUE);
        } catch (IOException | InterruptedException exception) {
            log.error("Command execution failed: {}", exception.getMessage());
            return false;
        }
    }

    private static String readFromStream(InputStream stream) throws IOException {
        StringJoiner joiner = new StringJoiner(StrUtil.LF);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            reader.lines().forEach(joiner::add);
        }
        return joiner.toString();
    }

    /**
     * appendToFile
     *
     * @param content   content
     * @param writePath writePath
     * @return boolean
     */
    public static boolean appendToFile(String content, String writePath) {
        if (StrUtil.isEmpty(content) || StrUtil.isEmpty(writePath)) {
            return false;
        }
        try (FileWriter fileWriter = new FileWriter(writePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            log.info("Content appended to file successfully.");
            return true;
        } catch (IOException e) {
            log.error("An error occurred while appending the content to file.");
            return false;
        }
    }
}
