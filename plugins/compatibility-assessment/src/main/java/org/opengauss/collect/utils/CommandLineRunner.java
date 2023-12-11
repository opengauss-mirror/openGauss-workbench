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

import cn.hutool.core.util.StrUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

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
     * @param command  command
     * @param filePath filePath
     * @return String
     */
    public static String runCommand(String command, String filePath) {
        log.info(command);
        log.info(filePath);
        try {
            File workingDirectory = new File(filePath);
            ProcessBuilder builder = new ProcessBuilder(Arrays.asList(command.split(" ")));
            builder.directory(workingDirectory);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            String line;
            StringBuffer res = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                res.append(line).append(StrUtil.LF);
                log.info(line);
            }
            reader.close();
            return res.toString();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
        return "";
    }
}
