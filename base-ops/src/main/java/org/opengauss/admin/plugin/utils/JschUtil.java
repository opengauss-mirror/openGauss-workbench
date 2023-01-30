package org.opengauss.admin.plugin.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.HostFile;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.enums.ops.HostFileTypeEnum;
import com.jcraft.jsch.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhf
 * @date 2022/6/13 15:18
 **/
@Slf4j
@Component
public class JschUtil {
    @Autowired
    private WsUtil wsUtil;
    @Autowired
    private WsConnectorManager wsConnectorManager;

    /**
     * Session timeout duration
     */
    private static final int SESSION_TIMEOUT = 10000;
    /**
     * Pipe flow timeout (script execution timeout)
     */
    private static final int CHANNEL_TIMEOUT = 50000;

    private static final ConcurrentHashMap<String,Session> SESSION_CACHE = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String,String> autoResponseContext = new ConcurrentHashMap<>();
    static {
        autoResponseContext.put("yes/no", "yes");
    }

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
        log.info("host:{},port:{},username:{}",host,port,username);
        return createSession(host, port, username, password);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @return SSH Result
     * @throws IOException IO Exception
     */
    public JschResult executeCommand(String command, Session session) throws IOException, InterruptedException {
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
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession) throws IOException, InterruptedException {
        return executeCommand(command, session, wsSession, null);
    }

    /**
     * ChannelExec
     *
     * @param command Instructions to execute
     * @param session session
     * @return SSH Result
     */
    public JschResult executeCommand(String command, Session session, Map<String, String> autoResponse) throws IOException, InterruptedException {
        return executeCommand(command, session, null, autoResponse);
    }

    public JschResult executeCommandWithSerialResponse(String command, Session session, Map<String, List<String>> autoResponse, WsSession retSession) throws Exception {
        if (Objects.nonNull(retSession)){
            wsUtil.sendText(retSession,command);
        }

        log.info("Execute an order：{}", command);
        if (Objects.nonNull(autoResponse)) {
            HashMap<String, List<String>> autoClone = new HashMap<>();
            autoClone.putAll(autoResponse);
            autoResponse = autoClone;
        } else {
            autoResponse = new HashMap<>();
        }

        ChannelExec channel;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setPtyType("dump");
            channel.setPty(true);
        } catch (JSchException e) {
            log.error("Obtaining the exec channel fails：", e);
            throw new OpsException("Obtaining the exec channel fails");
        }

        channel.setCommand(command);
        try {
            channel.connect(CHANNEL_TIMEOUT);
        } catch (JSchException e) {
            log.error("Execute instruction [{}] exception：", command, e);
            throw new OpsException("Command execution exception");
        }

        return buildJschResultWithSerialResponse(channel, retSession, autoResponse);
    }

    /**
     * ChannelExec
     *
     * @param command   Instructions to execute
     * @param session   session
     * @param wsSession websocket session
     * @return SSH Result
     */
    public JschResult executeCommand(String command, Session session, WsSession wsSession, Map<String, String> autoResponse) throws IOException, InterruptedException {
        log.info("Execute an order：{}", command);
        wsUtil.sendText(wsSession,command);

        if (Objects.nonNull(autoResponse)) {
            HashMap<String, String> autoClone = new HashMap<>(JschUtil.autoResponseContext);
            autoClone.putAll(autoResponse);
            autoResponse = autoClone;
        } else {
            autoResponse = JschUtil.autoResponseContext;
        }

        ChannelExec channel;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setPtyType("dump");
            channel.setPty(true);
        } catch (JSchException e) {
            log.error("Obtaining the exec channel fails：", e);
            throw new OpsException("Obtaining the exec channel fails");
        }

        channel.setCommand(command);
        try {
            channel.connect(CHANNEL_TIMEOUT);
        } catch (JSchException e) {
            log.error("Execute instruction [{}] exception：", command, e);
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
        ChannelShell channel;
        try {
            channel = (ChannelShell)session.openChannel("shell");
            channel.connect(CHANNEL_TIMEOUT);
        } catch (JSchException e) {
            log.error("Channel establishment Exception", e);
            throw new OpsException("Channel establishment Exception");
        }
        return channel;
    }

    /**
     * Send instructions to ChannelShell
     * @param channelShell  channelShell
     * @param command   command
     */
    public void sendToChannelShell(ChannelShell channelShell, String command) {
        try {
            if (channelShell != null) {
                OutputStream outputStream = channelShell.getOutputStream();
                outputStream.write(command.getBytes());
                outputStream.flush();
            }
        }catch (Exception e) {
            log.error("Message Sending Exception", e);
            throw new OpsException("Message Sending Exception");
        }

    }

    /**
     * The SSH Channel message is sent to the Websocket session
     * @param channel   SSH Channel
     * @param wsSession WebSocket session Object
     */
    public void channelToWsSession(Channel channel, WsSession wsSession) {
        try (InputStream inputStream = channel.getInputStream()){
            //Cyclic reading
            byte[] buffer = new byte[1024];
            int i;
            //If there is no data coming, the thread will remain blocked at this point waiting for data.
            while ((i = inputStream.read(buffer)) != -1) {
                byte[] bytes = Arrays.copyOfRange(buffer, 0, i);
                wsConnectorManager.getSession(wsSession.getSessionId()).ifPresent(newSession -> {
                    try {
                        newSession.getSession().getBasicRemote().sendText(new String(bytes));
                    } catch (IOException e) {
                        log.error("Failed to send message [{}] to websocket:", new String(bytes), e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Description Failed to send a message to the websocket：", e);
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
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            JschProgressMonitor jschProgressMonitor = new JschProgressMonitor(wsSession);
            channel.put(sourceFilePath, targetPath, jschProgressMonitor, ChannelSftp.RESUME);
        } catch (Exception e) {
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
        Session session;
        try {
            session = jSch.getSession(username, host, port);
        } catch (JSchException e) {
            log.error("Connection establishment fail：", e);
            throw new OpsException("Connection establishment fail");
        }

        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking","no");
        try {
            session.connect(SESSION_TIMEOUT);
        } catch (JSchException e) {
            log.error(host + "Connection establishment fail", e);
            throw new OpsException(host + "Connection establishment fail");
        }
        return Optional.of(session);
    }

    private JschResult buildJschResult(ChannelExec channelExec, WsSession wsSession, Map<String, String> autoResponse) throws IOException, InterruptedException {
        JschResult jschResult = new JschResult();
        StringBuilder resultStrBuilder = new StringBuilder();
        //The output of the script execution is an input stream to the program
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

                String msg = new String(tmp, 0, i);

                if (Objects.nonNull(wsSession) && Objects.nonNull(wsSession.getSession()) && wsSession.getSession().isOpen()) {
                    wsSession.getSession().getBasicRemote().sendText(new String(tmp, 0, i));
                } else if (Objects.nonNull(wsSession)
                        && StrUtil.isNotEmpty(wsSession.getSessionId())) {
                    wsConnectorManager.getSession(wsSession.getSessionId()).ifPresent(newSession -> {
                        try {
                            newSession.getSession().getBasicRemote().sendText(msg);
                        } catch (IOException e) {
                            log.error("Failed to send message [{}] to websocket：", msg, e);
                        }
                    });
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
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new InterruptedException();
            } catch (Exception e) {
                log.error("The parsing command execution result is abnormal：", e);
            }

            if (CollUtil.isNotEmpty(autoResponse)) {
                autoResponse.forEach((k, v) -> {
                    if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                        try {
                            out.write((v.trim() + "\r").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                            resultStrBuilder.append(v.trim()+"\r");
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

    private JschResult buildJschResultWithSerialResponse(ChannelExec channelExec, WsSession wsSession, Map<String, List<String>> autoResponse) throws Exception {
        JschResult jschResult = new JschResult();
        StringBuilder resultStrBuilder = new StringBuilder();
        //The output of the script execution is an input stream to the program
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

                String msg = new String(tmp, 0, i);

                if (Objects.nonNull(wsSession) && Objects.nonNull(wsSession.getSession()) && wsSession.getSession().isOpen()) {
                    wsSession.getSession().getBasicRemote().sendText(new String(tmp, 0, i));
                } else if (Objects.nonNull(wsSession)
                        && StrUtil.isNotEmpty(wsSession.getSessionId())) {
                    wsConnectorManager.getSession(wsSession.getSessionId()).ifPresent(newSession -> {
                        try {
                            newSession.getSession().getBasicRemote().sendText(msg);
                        } catch (IOException e) {
                            log.error("Failed to send message [{}] to websocket：", msg, e);
                        }
                    });
                }
                resultStrBuilder.append(msg);
            }

            if (channelExec.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                //Gets the exit status, with status 0 indicating that the script was executed correctly
                in.close();
                out.close();

                int exitStatus = channelExec.getExitStatus();
                jschResult.setExitCode(exitStatus);

                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new InterruptedException();
            } catch (Exception e) {
                log.error("The parsing command execution result is abnormal：", e);
            }

            if (CollUtil.isNotEmpty(autoResponse)) {
                autoResponse.forEach((k, v) -> {
                    if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                        try {
                            if (CollUtil.isNotEmpty(v)) {
                                out.write((v.get(0) + "\r").getBytes(StandardCharsets.UTF_8));
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

    public List<HostFile> ls(Session session, String remoteDir) {
        List<HostFile> res = new ArrayList<>();

        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;

            Vector vector = channelSftp.ls(remoteDir);
            Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) iterator.next();

                res.add(HostFile.of(file.getFilename(), file.getAttrs().isDir() ? HostFileTypeEnum.DIRECTORY : HostFileTypeEnum.FILE, file.getAttrs().getSize()));

            }
        } catch (Exception e) {
            log.error("File enumeration failure", e);
        }

        res.sort(Comparator.comparing(HostFile::getType).reversed().thenComparing(HostFile::getName));

        HostFile currentDir = null;
        HostFile parentDir = null;
        for (HostFile file : res) {
            if (".".equalsIgnoreCase(file.getName())){
                currentDir = file;
            }

            if ("..".equals(file.getName())){
                parentDir = file;
            }
        }

        res.remove(currentDir);

        if (Objects.nonNull(parentDir)){
            res.remove(parentDir);
            res.add(0,parentDir);
        }

        return res;
    }

    public void download(Session session, String path, String filename, HttpServletResponse response) {
        ChannelSftp sftp = null;
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } catch (Exception e) {
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

            if (empty){
                os.write(0);
            }

            os.flush();
        } catch (Exception e) {
            log.error("{}", e);
        }finally {
            if (Objects.nonNull(os)){
                try {
                    os.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class HostSummary {
        private String host;
        private Integer port;
        private String username;
        private String password;

        public String summary() {
            return SecureUtil.md5(this.host + "_" + this.port + "_" + this.username + "_" + this.password);
        }
    }

    private class JschProgressMonitor implements SftpProgressMonitor {
        //The total number of bytes currently received
        private long count = 0;
        //Final file size
        private long max = 0;
        //The progress of
        private long percent = -1;

        private WsSession wsSession;

        public JschProgressMonitor(WsSession wsSession) {
            this.wsSession = wsSession;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            this.max = max;
            this.count = 0;
            this.percent = -1;
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
