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
 * ShellUtil.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/utils/ShellUtil.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.utils;

import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.exception.ShellException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @className: ShellUtil
 * @author: xielibo
 * @date: 2023-01-16 21:55
 **/
@Slf4j
public class ShellUtil {

    /**
     * Connect Timeout
     */
    private static final Integer CONNECT_TIMEOUT = 5000;

    public static void execCommand(String host, Integer port, String user, String password, String... commands) {
        Session session = JschUtil.openSession(host, port, user, password);
        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            for (String command : commands) {
                channelExec.setCommand(command);
            }
            channelExec.connect();
        } catch (JSchException e) {
            log.error("exec command error, message: {}", e.getMessage());
        } finally {
            JschUtil.close(channelExec);
            JschUtil.close(session);
        }
    }

    /**
     * exec command and get result
     *
     * @param host     connection host of the executing machine
     * @param port     connection port of the executing machine
     * @param user     connection username of the executing machine
     * @param password connection password of the executing machine
     * @param commands execute command
     * @return exec result
     */
    public static JschResult execCommandGetResult(String host, Integer port, String user, String password, String... commands) {
        Session session = null;
        ChannelExec channelExec = null;
        JschResult jschResult = new JschResult();
        InputStream in = null;
        InputStreamReader isr = null;
        try {
            StringBuilder sb = new StringBuilder(16);
            session = JschUtil.openSession(host, port, user, password, CONNECT_TIMEOUT);
            channelExec = (ChannelExec) session.openChannel("exec");
            for (String command : commands) {
                channelExec.setCommand(command + " 2>&1");
            }
            channelExec.connect();
            in = channelExec.getInputStream();
            isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
            reader.close();

            int exitCode = channelExec.getExitStatus();
            // see http://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Channel.html#getExitStatus--
            // the exit status returned by the remote command, or -1,
            // if the command not yet terminated (or this channel type has no command)
            while (exitCode < 0) {
                exitCode = channelExec.getExitStatus();
                if (exitCode >= 0) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("wait shell result exception: " + e.getMessage());
                }
            }
            jschResult.setExitCode(exitCode);
            jschResult.setResult(sb.toString());
        } catch (JSchException | JschRuntimeException | IOException e) {
            log.error("exec command error, message: {}", e.getMessage());
            jschResult.setExitCode(-1);
            jschResult.setResult(e.getMessage());
        } finally {
            JschUtil.close(channelExec);
            JschUtil.close(session);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("close input stream error, message: {}", e.getMessage());
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    log.error("close input stream reader error, message: {}", e.getMessage());
                }
            }
        }

        return jschResult;
    }

    @Deprecated
    public static void rmDir(String host, Integer port, String user, String password, String path) {
        StringBuilder sb = new StringBuilder(16);
        Session session = JschUtil.openSession(host, port, user, password);
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            deleteFolder(sftp, path);
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            log.error("exec rm dir error, message: {}", e.getMessage());
        } finally {
            JschUtil.close(sftp);
            JschUtil.close(channel);
            JschUtil.close(session);
        }
    }

    @Deprecated
    private static void deleteFolder(ChannelSftp channelSftp, String path) throws SftpException {
        SftpATTRS attrs = channelSftp.lstat(path);
        if (attrs.isDir()) {
            channelSftp.cd(path);
            for (Object fileObj : channelSftp.ls(".")) {
                if (fileObj instanceof ChannelSftp.LsEntry) {
                    ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) fileObj;
                    String fileName = file.getFilename();
                    if (!fileName.equals(".") && !fileName.equals("..")) {
                        deleteFolder(channelSftp, path + "/" + fileName);
                    }
                }
            }
            channelSftp.cd("..");
            channelSftp.rmdir(path);
        } else {
            channelSftp.rm(path);
        }
    }

    public static void rmFile(String host, Integer port, String user, String password, String filepath) {
        Session session = JschUtil.openSession(host, port, user, password);
        Channel channel = null;
        ChannelSftp sftp = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            sftp.rm(filepath);
        } catch (JSchException | SftpException e) {
            log.error("exec rm file error, message: {}", e.getMessage());
        } finally {
            JschUtil.close(sftp);
            JschUtil.close(channel);
            JschUtil.close(session);
        }
    }

    public static void uploadFile(String host, Integer port, String username, String password, String filepath, InputStream in) throws ShellException {
        ChannelSftp sftp = null;
        Session session = null;
        try {
            session = JschUtil.openSession(host, port, username, password);
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            session.setConfig(properties);

            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();

            sftp.put(in, filepath);
        } catch (JSchException | SftpException e) {
            String errMsg = String.format("Upload file to %s failed, error: %s", filepath, e.getMessage());
            log.error(errMsg);
            throw new ShellException(errMsg);
        } finally {
            JschUtil.close(sftp);
            JschUtil.close(session);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("close input stream failed: " + e.getMessage());
                }
            }
        }
    }
}
