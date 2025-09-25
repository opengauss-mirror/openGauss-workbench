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
 * base-ops/src/main/java/org/opengauss/admin/plugin/utils/JschUtil.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.HostFile;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.HostFileTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jsch Tools
 *
 * @author lhf
 * @since 1.0
 * @date 2022/6/13 15:18
 **/
@Slf4j
@Component
public class JschUtil {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 50000;
    private static ConcurrentHashMap<String, String> autoResponseContext = new ConcurrentHashMap<>();

    static {
        autoResponseContext.put("yes/no", "yes");
    }

    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private WsConnectorManager wsConnectorManager;

    /**
     * Acquiring a Session
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @return ssh session
     *
     * @since 1.0
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
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     *
     * @since 1.0
     */
    public JschResult executeCommand(String command, Session session)
            throws IOException, InterruptedException {
        return executeCommand(command, session, null, null);
    }


    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @param env     env path
     * @return SSH Result
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     *
     * @since 1.0
     */
    public JschResult executeCommand(String command, Session session, String env)
            throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommand(command, session, null, null);
    }

    /**
     * ChannelExec
     *
     * @param command   Instructions to execute
     * @param session   session
     * @param wsSession websocket session
     * @return SSH Result
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     *
     * @since 1.0
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession)
            throws IOException, InterruptedException {
        return executeCommand(command, session, wsSession, null);
    }


    /**
     * ChannelExec
     *
     * @param command   Instructions to execute
     * @param env       env
     * @param session   session
     * @param wsSession websocket session
     * @return SSH Result
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     *
     * @since 1.0
     */
    public JschResult executeCommand(String command, String env, Session session, WsSession wsSession)
            throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommand(command, session, wsSession, null);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @param autoResponse response
     * @return SSH Result
     * @throws IOException IO Exception
     * @throws InterruptedException Interrupted Exception
     * @since 1.0
     */
    public JschResult executeCommand(String command, Session session, Map<String, String> autoResponse)
            throws IOException, InterruptedException {
        return executeCommand(command, session, null, autoResponse);
    }

    /**
     * executeCommandWithSerialResponse
     *
     * @param env env
     * @param command command
     * @param session session
     * @param autoResponse autoResponse
     * @param retSession retSession
     * @return result
     * @throws Exception e
     */
    public JschResult executeCommandWithSerialResponse(String env, String command, Session session,
        Map<String, List<String>> autoResponse, WsSession retSession) throws Exception {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }
        return executeCommandWithSerialResponse(command, session, autoResponse, retSession);
    }

    /**
     * executeCommandWithSerialResponse
     *
     * @param command command
     * @param session session
     * @param autoResponse autoResponse
     * @param retSession retSession
     * @return result
     * @throws Exception e
     */
    public JschResult executeCommandWithSerialResponse(String command, Session session, Map<String,
            List<String>> autoResponse, WsSession retSession) throws Exception {
        if (Objects.nonNull(retSession)) {
            wsUtil.sendText(retSession, command);
        }

        log.debug("Execute an order:{}", command);
        if (Objects.nonNull(autoResponse)) {
            HashMap<String, List<String>> autoClone = new HashMap<>();
            autoClone.putAll(autoResponse);
            autoResponse = autoClone;
        } else {
            autoResponse = new HashMap<>();
        }

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

        return buildJschResultWithSerialResponse(channel, retSession, autoResponse);
    }

    /**
     * executeCommand
     *
     * @param env env
     * @param command command
     * @param session session
     * @param wsSession wsSession
     * @param autoResponse  autoResponse
     * @return result
     * @throws IOException ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommand(String env, String command, Session session,
        WsSession wsSession, Map<String, String> autoResponse) throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommand(command, session, wsSession, autoResponse);
    }

    public JschResult executeCommand(String env, String command, Session session,
                                     WsSession wsSession, boolean handleErrorBeforeExit) throws IOException, InterruptedException {
        if (StrUtil.isNotEmpty(env)) {
            command = "source " + env + " && " + command;
        }

        return executeCommand(command, session, wsSession, null, handleErrorBeforeExit);
    }

    /**
     * ChannelExec
     *
     * @param command   Instructions to execute
     * @param session   session
     * @param wsSession websocket session
     * @param autoResponse  autoResponse
     * @return SSH Result
     *
     * @throws IOException ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession,
                                     Map<String, String> autoResponse) throws IOException, InterruptedException {
        return executeCommand(command, session, wsSession, autoResponse, true);
    }

    /**
     * ChannelExec
     *
     * @param command   Instructions to execute
     * @param session   session
     * @param wsSession websocket session
     * @param autoResponse  autoResponse
     * @param handleErrorBeforeExit handleErrorBeforeExit
     * @return SSH Result
     *
     * @throws IOException ioe
     * @throws InterruptedException ie
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession,
        Map<String, String> autoResponse, boolean handleErrorBeforeExit) throws IOException, InterruptedException {
        log.debug("Execute an order:{}", command);
        wsUtil.sendText(wsSession, command);

        if (Objects.nonNull(autoResponse)) {
            HashMap<String, String> autoClone = new HashMap<>(JschUtil.autoResponseContext);
            autoClone.putAll(autoResponse);
            autoResponse = autoClone;
        } else {
            autoResponse = JschUtil.autoResponseContext;
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

        JschResult jschResult = buildJschResult(channel, wsSession, autoResponse);

        if (handleErrorBeforeExit && jschResult.getExitCode() != 0) {
            log.error("execute command fail, command:{}, result:{}", command, jschResult);
            throw new OpsException("execute command fail with exit code:" + jschResult.getExitCode() + ",msg:"+ jschResult.getResult());
        }

        return jschResult;
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
                wsConnectorManager.getSession(wsSession.getSessionId()).ifPresent(newSession -> {
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
    public void upload(Session session, WsSession wsSession, String sourceFilePath, String targetPath) {
        log.info("Start uploading {} to {}", sourceFilePath, targetPath);
        synchronized (session) {
            try {
                Path sourcePath = resolveSourcePath(sourceFilePath);
                if (!Files.exists(sourcePath)) {
                    log.error("Source file not found: {}", sourcePath);
                    wsUtil.sendText(wsSession, "Source file not found: " + sourcePath);
                    return;
                }
                ChannelSftp channel = null;
                Channel sftpChannel = session.openChannel("sftp");
                if (sftpChannel instanceof ChannelSftp) {
                    channel = (ChannelSftp) sftpChannel;
                }
                channel.connect();
                JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(wsSession);
                channel.put(sourcePath.toString(), targetPath.trim(), jschProgressMonitor, ChannelSftp.OVERWRITE);
            } catch (JSchException | SftpException e) {
                log.error("sftp upload Failure", e);
                throw new OpsException("sftp upload Failure: " + e.getMessage());
            }
        }
        log.info("Upload End");
    }

    private Path resolveSourcePath(String sourceFilePath) {
        Path path = Paths.get(sourceFilePath);
        if (!path.isAbsolute()) {
            Path baseDir = Paths.get(System.getProperty("user.dir"));
            return baseDir.resolve(path).normalize();
        }
        return path.normalize();
    }

    /**
     * File upload
     *
     * @param session        Host SSH Session
     * @param wsSession      websocket session
     * @param sourceStream   Source stream
     * @param targetPath     The target path
     */
    public synchronized void upload(Session session, WsSession wsSession, InputStream sourceStream, String targetPath) {
        log.info("Start uploading stream to {}", targetPath);
        synchronized (session) {
            try {
                ChannelSftp channel = null;
                Channel sftpChannel = session.openChannel("sftp");
                if (sftpChannel instanceof ChannelSftp) {
                    channel = (ChannelSftp) sftpChannel;
                }
                channel.connect();
                JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(wsSession);
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
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }

                String msg = new String(tmp, 0, i, StandardCharsets.UTF_8);

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
                    && StrUtil.isNotEmpty(wsSession.getSessionId())) {
                wsConnectorManager.getSession(wsSession.getSessionId()).ifPresent(newSession -> {
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

    private JschResult buildJschResultWithSerialResponse(ChannelExec channelExec,
        WsSession wsSession, Map<String, List<String>> autoResponse) throws Exception {
        JschResult jschResult = new JschResult();
        StringBuilder resultStrBuilder = new StringBuilder();
        InputStream in = channelExec.getInputStream();
        OutputStream out = channelExec.getOutputStream();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                String msg = new String(tmp, 0, i, StandardCharsets.UTF_8);
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
            if (CollUtil.isNotEmpty(autoResponse)) {
                autoResponse.forEach((k, v) -> {
                    if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                        try {
                            if (CollUtil.isNotEmpty(v)) {
                                out.write((v.get(0) + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
                                v.remove(0);
                                out.flush();
                            }
                        } catch (IOException e) {
                            log.error("Automatic response exception", e);
                        }
                    }
                });
            }
        }
        channelExec.disconnect();
        jschResult.setResult(resultStrBuilder.toString().trim());
        return jschResult;
    }

    /**
     * list remote dir
     *
     * @param session   session
     * @param remoteDir remoteDir
     * @return  host files
     *
     * @since 1.0
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

        res.sort(Comparator.comparing(HostFile::getType).reversed().thenComparing(HostFile::getName));

        HostFile currentDir = null;
        HostFile parentDir = null;
        for (HostFile file : res) {
            if (".".equalsIgnoreCase(file.getName())) {
                currentDir = file;
            }

            if ("..".equals(file.getName())) {
                parentDir = file;
            }
        }

        res.remove(currentDir);

        if (Objects.nonNull(parentDir)) {
            res.remove(parentDir);
            res.add(0, parentDir);
        }

        return res;
    }

    public void download(Session session, String path, String filename, HttpServletResponse response) {
        ChannelSftp sftp = null;
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            throw new OpsException("Abnormal connection");
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);

        OutputStream os = null;
        try (InputStream inputStream = sftp.get(path + "/" + filename);
             BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            byte[] buff = new byte[1024];
            os = response.getOutputStream();
            int i = 0;
            boolean empty = true;
            while ((i = bis.read(buff)) != -1) {
                empty = false;
                os.write(buff, 0, i);
            }

            if (empty) {
                os.write(0);
            }

            os.flush();
        } catch (SftpException | IOException e) {
            log.error("{}", e);
        } finally {
            if (Objects.nonNull(os)) {
                try {
                    os.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

    /**
     * @param rootSession root user session
     * @param dependencies the list of dependencies
     * @return java.util.ArrayList<java.lang.String> the list of missing dependencies
     */
    public ArrayList<String> checkDependencies(Session rootSession, ArrayList<String> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            log.warn("The list of dependencies to check is null or empty.");
            return dependencies;
        }

        ArrayList<String> missingDependencies = new ArrayList<>();

        String command = String.format("yum list installed | egrep '%s'",
                String.join("|", dependencies));

        JschResult jschResult = null;
        try {
            jschResult = executeCommand(command, rootSession);
        } catch (OpsException e) {
            log.info("None of the checked dependencies exists.");
            return dependencies;
        } catch (IOException | InterruptedException e) {
            log.error("The check dependencies command fails to be executed. Details: ", e);
            return dependencies;
        }

        String commandOutput = jschResult.getResult();
        for (String dependency : dependencies) {
            if (!commandOutput.contains(dependency)) {
                missingDependencies.add(dependency);
            }
        }

        return missingDependencies;
    }

    /**
     * @param rootSession root user session
     * @param dependencies the list of dependencies
     */
    public void installDependencies(Session rootSession, ArrayList<String> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            log.warn("The list of dependencies to install is null or empty.");
            return;
        }

        String command = "yum install -y " + String.join(" ", dependencies);

        try {
            executeCommand(command, rootSession);
        } catch (OpsException e) {
            log.error("Some dependencies are not installed successfully. Details: " + e);
        } catch (IOException | InterruptedException e) {
            log.error("The install dependencies command fails to be executed. Details: " + e);
        }
    }

    private class JschProgressMonitor implements SftpProgressMonitor {
        // The total number of bytes currently received
        private long count = 0L;
        // Final file size
        private long max = 0L;
        // The progress of
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

            if (percent % 10 == 0) {
                log.info("Completed " + this.count + "(" + percent
                        + "%) out of " + max + ".");
            }
            wsUtil.sendText(wsSession, percent + "%");

            return true;
        }

        @Override
        public void end() {

        }
    }
}
