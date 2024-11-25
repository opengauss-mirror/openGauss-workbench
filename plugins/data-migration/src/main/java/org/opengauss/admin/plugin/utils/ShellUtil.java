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
import org.opengauss.admin.common.utils.StringUtils;
import org.opengauss.admin.plugin.exception.ShellException;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

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
    private static final Integer MAX_RETRY_COUNT = 20;
    private static final Integer RETRY_TIME = 5000;

    /**
     * execute command
     *
     * @param shellInfo shell information
     * @param command command
     */
    public static void execCommand(ShellInfoVo shellInfo, String command) {
        execCommand(shellInfo.getIp(), shellInfo.getPort(), shellInfo.getUsername(), shellInfo.getPassword(), command);
    }

    /**
     * execute command and get result
     *
     * @param shellInfo shell information
     * @param command command
     * @return JschResult
     */
    public static JschResult execCommandGetResult(ShellInfoVo shellInfo, String command) {
        return execCommandGetResult(
                shellInfo.getIp(), shellInfo.getPort(), shellInfo.getUsername(), shellInfo.getPassword(), command);
    }

    /**
     * execute command
     *
     * @param host host
     * @param port port
     * @param user user
     * @param password password
     * @param command command
     */
    public static void execCommand(String host, Integer port, String user, String password, String command) {
        Session session = null;
        ChannelExec channelExec = null;
        try {
            session = getSession(host, port, user, password, command);
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
        } catch (JSchException | JschRuntimeException | InterruptedException e) {
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
     * @param command  execute command
     * @return exec result
     */
    public static JschResult execCommandGetResult(String host, Integer port, String user, String password,
                                                    String command) {
        Session session = null;
        ChannelExec channelExec = null;
        JschResult jschResult = new JschResult();
        InputStream in = null;
        InputStreamReader isr = null;
        try {
            session = getSession(host, port, user, password, command);
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command + " 2>&1");
            channelExec.connect();
            in = channelExec.getInputStream();
            isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            String result = getResult(isr);
            int exitCode = getExitCode(channelExec);
            jschResult.setExitCode(exitCode);
            jschResult.setResult(result);
        } catch (JSchException | JschRuntimeException | IOException | InterruptedException e) {
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

    private static int getExitCode(ChannelExec channelExec) {
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
        return exitCode;
    }

    private static String getResult(InputStreamReader isr) throws IOException {
        StringBuilder sb = new StringBuilder(16);
        try (BufferedReader reader = new BufferedReader(isr)) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
        }
        return sb.toString();
    }

    private static Session getSession(String host, Integer port, String user, String password, String command)
            throws InterruptedException, JschRuntimeException {
        int maxRetryCount = (command.contains("jar") || command.contains("checkResult")) ? MAX_RETRY_COUNT : 1;
        int count = 0;
        Session session = null;
        while (count < maxRetryCount) {
            try {
                session = JschUtil.openSession(host, port, user, password, CONNECT_TIMEOUT);
                break;
            } catch (JschRuntimeException e) {
                if (count == maxRetryCount - 1) {
                    log.error("fail to get session, count:{}", count);
                    throw e;
                }
                Thread.sleep(RETRY_TIME);
            }
            count++;
        }
        return session;
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

    /**
     * check dependencies on host and get the list of missing dependencies
     *
     * @param rootShellInfo root shell information
     * @param dependencies the list of dependencies
     * @return ArrayList the list of missing dependencies
     */
    public static List<String> checkDependencies(ShellInfoVo rootShellInfo, List<String> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            log.warn("The list of dependencies to check is null or empty.");
            return Collections.emptyList();
        }

        String command = String.format("yum list installed | egrep '%s'", String.join("|", dependencies));
        String commandResult = execCommandGetResult(rootShellInfo, command).getResult();

        if (StringUtils.isEmpty(commandResult)) {
            return dependencies;
        }

        List<String> missingDependencies = new ArrayList<>();
        for (String dependency : dependencies) {
            if (!commandResult.contains(dependency)) {
                missingDependencies.add(dependency);
            }
        }
        return missingDependencies;
    }

    /**
     * yum install dependencies
     *
     * @param rootShellInfo root shell information
     * @param dependencies the list of dependencies
     */
    public static void installDependencies(ShellInfoVo rootShellInfo, List<String> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            log.warn("The list of dependencies to install is null or empty.");
            return;
        }

        StringJoiner dependencyJoiner = new StringJoiner(" ");
        dependencies.forEach(dependencyJoiner::add);

        String command = "yum install -y " + dependencyJoiner.toString();
        JschResult jschResult = execCommandGetResult(rootShellInfo, command);

        if (jschResult.isOk()) {
            log.info("Dependencies installed successfully: {}", dependencyJoiner.toString());
        } else {
            throw new ShellException(String.format(
                    "Failed to install dependencies. Command: {%s}, Result: {%s}", command, jschResult.getResult()));
        }
    }
}
