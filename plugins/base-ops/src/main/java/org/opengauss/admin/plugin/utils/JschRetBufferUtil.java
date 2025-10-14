/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * JschRetBufferUtil.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/utils/JschRetBufferUtil.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.RetBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JschRetBufferUtil
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Component
@Scope("prototype")
public class JschRetBufferUtil {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 50000;
    private static ConcurrentHashMap<String, String> autoResponseContext = new ConcurrentHashMap<>();

    static {
        autoResponseContext.put("yes/no", "yes");
    }

    @Autowired
    private WsUtil wsUtil;

    /**
     * Acquiring a Session
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @return ssh session
     * @since 1.0
     */
    public Optional<Session> getSession(String host, Integer port, String username, String password) {
        log.info("host:{},port:{},username:{}", host, port, username);
        return createSession(host, port, username, password);
    }

    /**
     * execute command
     *
     * @param command Instructions to execute
     * @param session session
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     * @since 1.0
     */
    public JschResult executeCommand(String command, Session session)
            throws IOException, InterruptedException {
        return executeCommandWithRetBuffer(command, session, null);
    }

    /**
     * execute command with ret buffer
     *
     * @param command   Instructions to execute
     * @param session   session
     * @param retBuffer retBuffer
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     * @since 1.0
     */
    public JschResult executeCommandWithRetBuffer(String command, Session session, RetBuffer retBuffer)
            throws IOException, InterruptedException {
        return executeCommandWithRetBufferAndAutoResponse(command, session, retBuffer, null);
    }


    /**
     * execute command with ret buffer ,load environment
     *
     * @param command   Instructions to execute
     * @param env       env
     * @param session   session
     * @param retBuffer retBuffer
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     * @since 1.0
     */
    public JschResult executeCommandWithEnvAndRetBuffer(String command, String env, Session session, RetBuffer retBuffer)
            throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommandWithRetBufferAndAutoResponse(command, session, retBuffer, null);
    }


    /**
     * execute command with ret buffer and auto response ,load environment
     *
     * @param env          env
     * @param command      command
     * @param session      session
     * @param retBuffer    retBuffer
     * @param autoResponse autoResponse
     * @return result
     * @throws IOException          ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommandWithEnvAndRetBufferAndAutoResponse(String env, String command, Session session,
                                                                       RetBuffer retBuffer, Map<String, String> autoResponse) throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommandWithRetBufferAndAutoResponse(command, session, retBuffer, autoResponse);
    }


    /**
     * execute command with ret buffer and auto response
     *
     * @param command      Instructions to execute
     * @param session      session
     * @param retBuffer    retBuffer
     * @param autoResponse autoResponse
     * @return SSH Result
     * @throws IOException          ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommandWithRetBufferAndAutoResponse(String command, Session session, RetBuffer retBuffer,
                                                                 Map<String, String> autoResponse) throws IOException, InterruptedException {
        return executeCommand(command, session, retBuffer, autoResponse, true);
    }

    /**
     * execute command with ret buffer and auto response
     *
     * @param command               Instructions to execute
     * @param session               session
     * @param retBuffer             retBuffer
     * @param autoResponse          autoResponse
     * @param handleErrorBeforeExit handleErrorBeforeExit
     * @return SSH Result
     * @throws IOException          ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommand(String command, Session session, RetBuffer retBuffer,
                                     Map<String, String> autoResponse, boolean handleErrorBeforeExit) throws IOException, InterruptedException {
        log.debug("Execute an order:{}", command);
        if (Objects.nonNull(retBuffer)) {
            retBuffer.sendText(command);
        }

        if (Objects.nonNull(autoResponse)) {
            HashMap<String, String> autoClone = new HashMap<>(JschRetBufferUtil.autoResponseContext);
            autoClone.putAll(autoResponse);
            autoResponse = autoClone;
        } else {
            autoResponse = JschRetBufferUtil.autoResponseContext;
        }

        ChannelExec channel = null;
        try {
            Channel execChannel = session.openChannel("exec");
            if (execChannel instanceof ChannelExec) {
                channel = (ChannelExec) execChannel;
            }
            channel.setPtyType("dump");
            channel.setPty(!command.contains("nohup"));
        } catch (JSchException e) {
            log.error("Obtaining the exec channel fails:", e);
            throw new OpsException("Obtaining the exec channel fails");
        }

        channel.setCommand(command);
        try {
            channel.connect(CHANNEL_TIMEOUT);
        } catch (JSchException e) {
            log.error("Execute instruction [{}] exception:", command, e);
            throw new OpsException("Command execution exception");
        }

        JschResult jschResult = buildJschResult(channel, retBuffer, autoResponse);

        if (handleErrorBeforeExit && jschResult.getExitCode() != 0) {
            log.error("execute command fail, command:{}, result:{}", command, jschResult);
            throw new OpsException("execute command fail with exit code:" + jschResult.getExitCode() + ",msg:" + jschResult.getResult());
        }

        return jschResult;
    }

    /**
     * File upload
     *
     * @param session        Host SSH Session
     * @param retBuffer      retBuffer
     * @param sourceFilePath Source path
     * @param targetPath     The target path
     */
    public void upload(Session session, RetBuffer retBuffer, String sourceFilePath, String targetPath) {
        log.info("Start uploading {} to {}", sourceFilePath, targetPath);
        synchronized (session) {
            try {
                ChannelSftp channel = null;
                Channel sftpChannel = session.openChannel("sftp");
                if (sftpChannel instanceof ChannelSftp) {
                    channel = (ChannelSftp) sftpChannel;
                }
                channel.connect();
                JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(retBuffer);
                channel.put(sourceFilePath.trim(), targetPath.trim(), jschProgressMonitor, ChannelSftp.OVERWRITE);
            } catch (JSchException | SftpException e) {
                log.error("sftp upload Failure", e);
                throw new OpsException("sftp upload Failure: " + e.getMessage());
            }
        }
        log.info("Upload End");
    }

    /**
     * File upload
     *
     * @param session      Host SSH Session
     * @param retBuffer    retBuffer
     * @param sourceStream Source stream
     * @param targetPath   The target path
     */
    public synchronized void upload(Session session, RetBuffer retBuffer, InputStream sourceStream, String targetPath) {
        log.info("Start uploading stream to {}", targetPath);
        synchronized (session) {
            try {
                ChannelSftp channel = null;
                Channel sftpChannel = session.openChannel("sftp");
                if (sftpChannel instanceof ChannelSftp) {
                    channel = (ChannelSftp) sftpChannel;
                }
                channel.connect();
                JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(retBuffer);
                channel.put(sourceStream, targetPath.trim(), jschProgressMonitor, ChannelSftp.OVERWRITE);
            } catch (JSchException | SftpException e) {
                log.error("sftp upload Failure", e);
                throw new OpsException("sftp upload Failure: " + e.getMessage());
            } finally {
                try {
                    sourceStream.close();
                } catch (IOException e) {
                    log.error("close input stream failed: " + e.getMessage());
                }
            }
        }
        log.info("Upload End");
    }

    /**
     * ChannelShell Creates a session
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @return SSH session
     */
    private Optional<Session> createSession(String host, Integer port, String username, String password) {
        JSch jSch = new JSch();
        Session session;
        try {
            session = jSch.getSession(username, host, port);
        } catch (JSchException e) {
            log.error("Connection establishment fail:", e);
            throw new OpsException("Connection establishment fail");
        }

        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        try {
            session.connect(SESSION_TIMEOUT);
        } catch (JSchException e) {
            log.error(host + "Connection establishment fail", e);
            throw new OpsException(host + "Connection establishment fail");
        }
        return Optional.of(session);
    }

    private JschResult buildJschResult(ChannelExec channelExec, RetBuffer retBuffer,
                                       Map<String, String> autoResponse) throws IOException, InterruptedException {
        JschResult jschResult = new JschResult();
        StringBuilder resultStrBuilder = new StringBuilder();
        // The output of the script execution is an input stream to the program\
        try (InputStream in = channelExec.getInputStream(); OutputStream out = channelExec.getOutputStream()) {
            // Read the input stream from the remote host and get the script execution results
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }

                    String msg = new String(tmp, 0, i, StandardCharsets.UTF_8);
                    if (Objects.nonNull(retBuffer)) {
                        retBuffer.sendText(msg);
                    }
                    resultStrBuilder.append(msg);
                }

                if (channelExec.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    in.close();
                    out.close();

                    int exitStatus = channelExec.getExitStatus();
                    jschResult.setExitCode(exitStatus);

                    break;
                }
                autoResponse(autoResponse, resultStrBuilder, out);
            }

        }


        channelExec.disconnect();
        jschResult.setResult(resultStrBuilder.toString().trim());
        return jschResult;
    }


    private void autoResponse(Map<String, String> autoResponse,
                              StringBuilder resultStrBuilder, OutputStream out) {
        if (CollUtil.isNotEmpty(autoResponse)) {
            autoResponse.forEach((k, v) -> {
                if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                    try {
                        out.write((v.trim() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        log.info("autoResponse {}{} ", k, v);
                        resultStrBuilder.append(v.trim() + System.lineSeparator());
                    } catch (IOException e) {
                        log.error("Automatic response exception", e);
                    }
                }
            });
        }
    }

    private class JschProgressMonitor implements SftpProgressMonitor {
        // The total number of bytes currently received
        private long count = 0L;
        // Final file size
        private long max = 0L;
        // The progress of
        private long percent = -1L;

        private RetBuffer retBuffer;

        public JschProgressMonitor(RetBuffer retBuffer) {
            this.retBuffer = retBuffer;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            this.max = max;
            count = 0;
            percent = -1;
        }

        @Override
        public boolean count(long count) {
            this.count += count;
            if (percent >= this.count * 100 / max) {
                return true;
            }
            percent = this.count * 100 / max;
            if (percent % 10 == 0) {
                log.info("Completed " + this.count + "(" + percent + "%) out of " + max + ".");
            }
            retBuffer.sendText(percent + "%");
            return true;
        }

        @Override
        public void end() {

        }
    }
}
