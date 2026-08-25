/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 */

package org.opengauss.jsch.pool;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cn.hutool.core.thread.ThreadUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.jsch.pool.config.SessionConfig;
import org.opengauss.jsch.pool.records.HostUserKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ExecOperations
 *
 * @author: wangchao
 * @Date: 2025/8/4 16:20
 * @since 7.0.0-RC2
 **/
@Slf4j
public class ExecOperations {
    private static final int DEFAULT_TIMEOUT = 30000;

    /**
     * semaphore per HostUserKey, used to control concurrent exec commands per host-user
     * fair mode to avoid thread starvation
     */
    private static final ConcurrentMap<HostUserKey, Semaphore> SEMAPHORE_MAP = new ConcurrentHashMap<>();

    /**
     * execute single command
     *
     * @param config session config
     * @param command command
     * @return exec result
     * @throws JSchException exec exception
     */
    public static ExecResult executeCommand(SessionConfig config, String command) throws JSchException {
        return executeCommand(config, command, DEFAULT_TIMEOUT);
    }

    /**
     * execute single command
     *
     * @param config session config
     * @param command command
     * @param commandTimeout command timeout
     * @return exec result
     * @throws JSchException exec exception
     */
    public static ExecResult executeCommand(SessionConfig config, String command, int commandTimeout)
            throws JSchException {
        boolean isPty = !command.contains("nohup");
        return executeCommand(config, command, commandTimeout, isPty);
    }

    /**
     * execute multiple commands
     *
     * @param config session config
     * @param commands commands
     * @return exec result list
     * @throws JSchException exec exception
     */
    public static List<ExecResult> executeCommands(SessionConfig config, String commands) throws JSchException {
        return executeCommands(config, commands.split(";"), DEFAULT_TIMEOUT);
    }

    /**
     * execute multiple commands
     *
     * @param config session config
     * @param commands commands
     * @param timeout timeout
     * @return exec result list
     * @throws JSchException exec exception
     */
    public static List<ExecResult> executeCommands(SessionConfig config, String[] commands, int timeout)
        throws JSchException {
        List<ExecResult> results = new ArrayList<>();
        for (String command : commands) {
            if (!command.trim().isEmpty()) {
                boolean isPty = !command.contains("nohup");
                results.add(executeCommand(config, command, timeout, isPty));
            }
        }
        return results;
    }

    /**
     * get or create semaphore for host-user key
     *
     * @param config session config
     * @return semaphore
     */
    private static Semaphore getOrCreateSemaphore(SessionConfig config) {
        HostUserKey key = config.getKey();
        return SEMAPHORE_MAP.computeIfAbsent(key,
            k -> new Semaphore(config.getMaxConcurrent(), true));
    }

    /**
     * release and remove semaphore for host-user key, called when pool is closed
     *
     * @param key host user key
     */
    public static void releaseSemaphore(HostUserKey key) {
        SEMAPHORE_MAP.remove(key);
    }

    /**
     * execute single command
     *
     * @param config session config
     * @param command command
     * @param timeout timeout
     * @param isPty isPty
     * @return exec result
     * @throws JSchException exec exception
     */
    public static ExecResult executeCommand(SessionConfig config, String command, int timeout, boolean isPty)
        throws JSchException {
        Session session = null;
        ChannelExec channel = null;
        ExecResult result = new ExecResult();
        boolean isAcquired = false;
        Semaphore semaphore = getOrCreateSemaphore(config);
        try {
            if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Failed to acquire semaphore within timeout: " + timeout + "ms");
            }
            isAcquired = true;
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getExecChannel(session);
            channel.setPty(isPty);
            channel.setCommand(command);
            channel.connect();
            InputStream in = channel.getInputStream();
            InputStream err = channel.getExtInputStream();
            Thread outThread = readStream(in, result.getOutput());
            Thread errThread = readStream(err, result.getError());
            long startTime = System.currentTimeMillis();
            while (!channel.isClosed()) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    throw new TimeoutException("Command execution timeout");
                }
                ThreadUtil.safeSleep(100);
            }
            outThread.join(1000);
            errThread.join(1000);
            result.setExitCode(channel.getExitStatus());
            return result;
        } catch (TimeoutException e) {
            throw new JSchException("Command execution timeout");
        } catch (IOException | InterruptedException e) {
            throw new JSchException("Command execution failed", e);
        } finally {
            if (isAcquired) {
                semaphore.release();
            }
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }

    /**
     * read stream asynchronously
     *
     * @param in input stream
     * @param output output stream
     * @return thread read stream thread
     */
    private static Thread readStream(InputStream in, StringBuilder output) {
        Thread thread = ThreadUtil.newThread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException ex) {
                log.warn("exec operations read stream asynchronously has some error : {}", ex.getMessage());
            }
        }, "read-stream-thread");
        thread.start();
        return thread;
    }

    /**
     * exec result
     */
    @Data
    public static class ExecResult {
        private final StringBuilder output = new StringBuilder();
        private final StringBuilder error = new StringBuilder();
        private int exitCode = -1;

        /**
         * check exec result is success
         *
         * @return true if success, false otherwise
         */
        public boolean isSuccess() {
            return exitCode == 0;
        }

        /**
         * get exec result
         *
         * @return exec result
         */
        public String getResult() {
            String result = output.toString().trim();
            if (result.endsWith("\n")) {
                return result.substring(0, result.length() - 1);
            } else {
                return result;
            }
        }

        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "ExecResult[exitCode=%d, output='%s', error='%s']", exitCode,
                output.toString().trim(), error.toString().trim());
        }
    }
}
