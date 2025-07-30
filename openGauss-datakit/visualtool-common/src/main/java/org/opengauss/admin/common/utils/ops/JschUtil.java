/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * JschUtil.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/utils/ops/JschUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.utils.ops;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.HostFile;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.domain.model.ops.host.SSHBody;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.enums.ops.HostFileTypeEnum;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Jsch Tools
 *
 * @author lhf
 * @date 2022/6/13 15:18
 * @since 1.0
 **/
@Slf4j
@Component
@Scope("prototype")
public class JschUtil {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 50000;

    @Autowired
    private WsConnectorManager wsConnectorManager;
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
     */
    public Optional<Session> getSession(String host, Integer port, String username, String password) {
        log.info("host:{},port:{},username:{}", host, port, username);
        return createSession(host, port, username, password);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String command, Session session) throws IOException, InterruptedException {
        return executeCommand(command, session, null, null);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @param env     env path
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String command, Session session,
                                     String env) throws IOException, InterruptedException {
        String comm = command;
        if (StrUtil.isNotEmpty(env)) {
            comm = "source " + env + " && " + command;
        }

        return executeCommand(comm, session, null, null);
    }

    /**
     * ChannelExec
     *
     * @param env          env
     * @param command      Instructions to execute
     * @param session      session
     * @param autoResponse autoResponse
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String env, String command, Session session,
                                     Map<String, String> autoResponse) throws IOException, InterruptedException {
        String comm = command;
        if (StrUtil.isNotEmpty(env)) {
            comm = "source " + env + " && " + command;
        }
        return executeCommand(comm, session, null, autoResponse);
    }

    /**
     * ChannelExec
     *
     * @param command      Instructions to execute
     * @param session      session
     * @param autoResponse autoResponse
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String command, Session session,
                                     Map<String, String> autoResponse) throws IOException, InterruptedException {
        return executeCommand(command, session, null, autoResponse);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param sshBody sshBody
     * @param autoResponse autoResponse
     * @return SSH Result
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String command, SSHBody sshBody,
                                     Map<String, String> autoResponse) throws IOException, InterruptedException {
        Optional<Session> session = getSession(sshBody.getIp(), sshBody.getSshPort(),
                sshBody.getSshUsername(), sshBody.getSshPassword());
        return executeCommand(command, session.get(), autoResponse);
    }

    /**
     * ChannelExec
     *
     * @param command      Instructions to execute
     * @param session      session
     * @param wsSession    websocket session
     * @param autoResponse autoResponse
     * @return SSH Result
     * @throws IOException          IO Exception
     * @throws InterruptedException Interrupted Exception
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession,
                                     Map<String, String> autoResponse) throws IOException, InterruptedException {
        log.debug("Execute an order {}", command);

        ChannelExec channel = null;
        try {
            Channel exeChannel = session.openChannel("exec");
            if (exeChannel instanceof ChannelExec) {
                channel = (ChannelExec) exeChannel;
            }
            channel.setPtyType("dump");
            channel.setPty(true);
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

        return buildJschResult(channel, wsSession, autoResponse);
    }

    /**
     * open ChannelShell
     *
     * @param session session
     * @return SSH Result
     */
    public ChannelShell openChannelShell(Session session) {
        ChannelShell channel = null;
        try {
            Channel channelShell = session.openChannel("shell");
            if (channelShell instanceof ChannelShell) {
                channel = (ChannelShell) channelShell;
            }
            channel.setPtySize(200, 40, 200 * 8, 40 * 8);
            channel.connect(CHANNEL_TIMEOUT);
        } catch (JSchException e) {
            log.error("Channel establishment Exception", e);
            throw new OpsException("Channel establishment Exception");
        }
        return channel;
    }

    /**
     * Send instructions to ChannelShell
     *
     * @param channelShell channelShell
     * @param command      command
     */
    public void sendToChannelShell(ChannelShell channelShell, String command) {
        try {
            if (channelShell != null) {
                OutputStream outputStream = channelShell.getOutputStream();
                outputStream.write(command.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("Message Sending Exception", e);
            throw new OpsException("Message Sending Exception");
        }
    }

    /**
     * The SSH Channel message is sent to the Websocket session
     *
     * @param channel   SSH Channel
     * @param wsSession WebSocket session Object
     */
    public void channelToWsSession(Channel channel, WsSession wsSession) {
        try (InputStream inputStream = channel.getInputStream()) {
            // Cyclic reading
            byte[] buffer = new byte[1024];
            int len;
            // If there is no data coming, the thread will remain blocked at this point waiting for data.
            while ((len = inputStream.read(buffer)) != -1) {
                byte[] bytes = Arrays.copyOfRange(buffer, 0, len);
                wsConnectorManager.getSession(wsSession.getBusinessId()).ifPresent(newSession -> {
                    try {
                        newSession.getSession().getBasicRemote().sendText(new String(bytes, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        log.error("Failed to send message [{}] to websocket:",
                                new String(bytes, StandardCharsets.UTF_8), e);
                    }
                });
            }
        } catch (IOException e) {
            log.error("Description Failed to send a message to the websocket:", e);
        } finally {
            wsSession.close();
            channel.disconnect();
        }
    }

    /**
     * File upload
     *
     * @param session        Host SSH Session
     * @param wsSession      websocket session
     * @param sourceFilePath Source path
     * @param targetPath     The target path
     */
    public synchronized void upload(Session session, WsSession wsSession, String sourceFilePath, String targetPath) {
        log.info("Start uploading {} to {}", sourceFilePath, targetPath);
        try {
            ChannelSftp channel = null;
            Channel sftpChannel = session.openChannel("sftp");
            if (sftpChannel instanceof ChannelSftp) {
                channel = (ChannelSftp) sftpChannel;
            }
            channel.connect();
            JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(wsSession);
            channel.put(sourceFilePath, targetPath, jschProgressMonitor, ChannelSftp.RESUME);
        } catch (JSchException | SftpException e) {
            log.error("sftp upload Failure", e);
            throw new OpsException("sftp upload Failure");
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
        Session session = null;
        try {
            session = jSch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(SESSION_TIMEOUT);
        } catch (JSchException e) {
            log.error("Connection establishment fail:", e);
            session = null;
        }

        return Optional.ofNullable(session);
    }

    private JschResult buildJschResult(ChannelExec channelExec, WsSession wsSession,
                                       Map<String, String> autoResponse) throws IOException, InterruptedException {
        JschResult jschResult = new JschResult();
        StringBuilder resultStrBuilder = new StringBuilder();
        // The output of the script execution is an input stream to the program
        InputStream in = channelExec.getInputStream();
        OutputStream out = channelExec.getOutputStream();
        // Read the input stream from the remote host and get the script execution results
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int len = in.read(tmp, 0, 1024);
                if (len < 0) {
                    break;
                }

                String msg = new String(tmp, 0, len, StandardCharsets.UTF_8);

                msgToWs(msg, wsSession);

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

        channelExec.disconnect();
        jschResult.setResult(resultStrBuilder.toString().trim());
        return jschResult;
    }

    private void msgToWs(String msg, WsSession wsSession) throws IOException {
        if (Objects.nonNull(wsSession) && Objects.nonNull(wsSession.getSession())
                && wsSession.getSession().isOpen()) {
            wsSession.getSession().getBasicRemote().sendText(msg);
        } else {
            if (Objects.nonNull(wsSession)
                    && StrUtil.isNotEmpty(wsSession.getBusinessId())) {
                wsConnectorManager.getSession(wsSession.getBusinessId()).ifPresent(newSession -> {
                    try {
                        newSession.getSession().getBasicRemote().sendText(msg);
                    } catch (IOException e) {
                        log.error("Failed to send message [{}] to websocket:", msg, e);
                    }
                });
            }
        }
    }

    private void autoResponse(Map<String, String> autoResponse,
                              StringBuilder resultStrBuilder, OutputStream out) {
        if (CollUtil.isNotEmpty(autoResponse)) {
            autoResponse.forEach((k, v) -> {
                if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                    try {
                        out.write((v.trim() + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        resultStrBuilder.append(v.trim() + System.getProperty("line.separator"));
                    } catch (IOException e) {
                        log.error("Automatic response exception", e);
                    }
                }
            });
        }
    }

    /**
     * ls
     *
     * @param session   session
     * @param remoteDir remoteDir
     * @return hostFiles
     */
    public List<HostFile> ls(Session session, String remoteDir) {
        List<HostFile> res = new ArrayList<>();

        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = null;
            if (channel instanceof ChannelSftp) {
                channelSftp = (ChannelSftp) channel;
            }

            Vector vector = channelSftp.ls(remoteDir);
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = null;
                Object next = iterator.next();
                if (next instanceof ChannelSftp.LsEntry) {
                    file = (ChannelSftp.LsEntry) next;
                }
                res.add(HostFile.of(file.getFilename(), file.getAttrs().isDir()
                        ? HostFileTypeEnum.DIRECTORY : HostFileTypeEnum.FILE, file.getAttrs().getSize()));
            }
        } catch (JSchException | SftpException e) {
            log.error("File enumeration failure", e);
        }

        return res;
    }

    private class JschProgressMonitor implements SftpProgressMonitor {
        private long count = 0L;
        private long max = 0L;
        private long percent = -1L;

        private WsSession wsSession;

        public JschProgressMonitor(WsSession wsSession) {
            this.wsSession = wsSession;
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

            log.info("Completed " + this.count + "(" + percent
                    + "%) out of " + max + ".");

            wsUtil.sendText(wsSession, percent + "%");

            return true;
        }

        @Override
        public void end() {

        }
    }
}
