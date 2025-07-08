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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.opengauss.agent.entity.OsCmdResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * OsCommandUtils
 *
 * @author: wangchao
 * @Date: 2025/7/7 09:47
 * @since 7.0.0-RC2
 **/
public class OsCommandUtils {
    private static final Set<String> ALLOWED_COMMANDS = ConcurrentHashMap.newKeySet();
    private static final Pattern SAFE_ARG_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-\\.]+$");

    /**
     * force refresh allowed command Collection
     *
     * @param allowedCommands allowed command Collection
     */
    public static void forceRefreshAllowedCommand(List<String> allowedCommands) {
        ALLOWED_COMMANDS.addAll(allowedCommands);
    }

    /**
     * get allowed command Collection
     *
     * @return allowed command Collection
     */
    public static Set<String> getAllowedCommands() {
        return ALLOWED_COMMANDS;
    }

    /**
     * os cmd execute
     *
     * @param commandLine os cmd
     * @return OsCmdResult
     */
    public static OsCmdResult execute(String commandLine) {
        if (!isSafeCommand(commandLine)) {
            return new OsCmdResult(-1, "Unsafe command detected: " + commandLine);
        }
        CommandLine cmdLine = CommandLine.parse(commandLine);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(null);
        executor.setWatchdog(new ExecuteWatchdog(60000));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        try {
            return new OsCmdResult(executor.execute(cmdLine), outputStream.toString(Charset.defaultCharset()));
        } catch (ExecuteException e) {
            return new OsCmdResult(e.getExitValue(),
                "Execution failed: " + outputStream.toString(Charset.defaultCharset()));
        } catch (IOException e) {
            return new OsCmdResult(-1, "Cmd Error: " + e.getMessage());
        } finally {
            try {
                outputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * check command is safe
     *
     * @param commandLine commandLine
     * @return isSafeCommand
     */
    protected static boolean isSafeCommand(String commandLine) {
        if (commandLine == null || commandLine.trim().isEmpty()) {
            return false;
        }
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length == 0) {
            return false;
        }
        String commandName = parts[0].contains("/") ? parts[0].substring(parts[0].lastIndexOf('/') + 1) : parts[0];
        if (!ALLOWED_COMMANDS.contains(commandName)) {
            return true;
        }
        if (parts.length > 1) {
            String args = parts[1];
            return SAFE_ARG_PATTERN.matcher(args).matches();
        }
        return true;
    }
}
