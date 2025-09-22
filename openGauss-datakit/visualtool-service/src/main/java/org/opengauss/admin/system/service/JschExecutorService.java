/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * JschExecutorService.java
 *
 * IDENTIFICATION
 * openGauss-datakit/visualtool-service/src/main/java/org/opengauss/admin/system/service/JschExecutorService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.core.handler.ops.cache.WsConnectorManager;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.jsch.pool.ExecOperations;
import org.opengauss.jsch.pool.JschSessionPool;
import org.opengauss.jsch.pool.config.SessionConfig;
import org.opengauss.jsch.pool.enums.ChannelType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;

/**
 * JschExecutorService
 *
 * @author wangchao
 * @since 2024/10/29 09:26
 */
@Slf4j
@Service
public class JschExecutorService {
    /**
     * Get the os of the remote machine
     *
     * @param sshLogin ssh login info
     * @return os
     */
    public String getOs(SshLogin sshLogin) {
        return execCommand(sshLogin, SshCommandConstants.OS);
    }

    /**
     * Get the os version of the remote machine
     *
     * @param sshLogin ssh login info
     * @return os version
     */
    public String getOsVersion(SshLogin sshLogin) {
        return execCommand(sshLogin, SshCommandConstants.OS_VERSION);
    }

    /**
     * Get the os info of the remote machine
     *
     * @param session ssh session
     * @return os info
     */
    public String getOs(Session session) {
        return execCommand(session, SshCommandConstants.OS);
    }

    /**
     * Get the os version of the remote machine
     *
     * @param session ssh session
     * @return os version
     */
    public String getOsVersion(Session session) {
        return execCommand(session, SshCommandConstants.OS_VERSION);
    }

    /**
     * Get the hostname of the remote machine
     *
     * @param sshLogin ssh login info
     * @return hostname
     */
    public String getHostname(SshLogin sshLogin) {
        return execCommand(sshLogin, SshCommandConstants.HOSTNAME);
    }

    /**
     * Get the cpu architecture of the remote machine
     *
     * @param session ssh session
     * @return cpu architecture
     */
    public String getCpuArch(Session session) {
        return execCommand(session, SshCommandConstants.CPU_ARCH);
    }

    /**
     * Get the cpu architecture of the remote machine
     *
     * @param sshLogin ssh login info
     * @return cpu architecture
     */
    public String getCpuArch(SshLogin sshLogin) {
        return execCommand(sshLogin, SshCommandConstants.CPU_ARCH);
    }

    /**
     * <pre>
     * Get the cpu info of the remote machine
     *
     * eg. x86_64
     * lscpu | grep "CPU(s):\|Architecture\|MHz"
     * Architecture:          x86_64
     * CPU(s):                8
     * CPU MHz:               2799.974
     * NUMA node0 CPU(s):     0-7
     *
     * eg. aarch64
     * lscpu | grep "CPU(s):\|Architecture\|MHz"
     * Architecture:                    aarch64
     * CPU(s):                          12
     * CPU max MHz:                     2400.0000
     * CPU min MHz:                     2400.0000
     * NUMA node0 CPU(s):               0-5
     * NUMA node1 CPU(s):               6-11
     * </pre>
     *
     * @param sshLogin ssh login info
     * @return cpu info
     */
    public String getCpu(SshLogin sshLogin) {
        return execCommand(sshLogin, SshCommandConstants.CPU);
    }

    /**
     * 检查端口是否被占用
     *
     * @param sshLogin ssh登录信息
     * @param port 端口号
     * @return true 未被占用，false 被占用
     * @author wangchao
     * &#064;date  2022/10/27 15:08
     * @since 7.0.0
     */
    public boolean checkOsPortConflict(SshLogin sshLogin, int port) {
        OpsAssert.isTrue(port > 1024 && port < 65536, "port must be between 1024 and 65535");
        String command = String.format(SshCommandConstants.CHECK_OS_PORT_CONFLICT, port);
        String result = execCommand(sshLogin, command);
        OpsAssert.isTrue(StrUtil.isNotEmpty(result) && NumberUtil.isInteger(result), "port conflict error :" + result);
        return NumberUtil.parseInt(result) <= 0;
    }

    /**
     * check port conflict
     *
     * @param session session
     * @param port port
     * @return true- not conflict, false - conflict
     */
    public boolean checkOsPortConflict(Session session, int port) {
        OpsAssert.isTrue(port > 1024 && port < 65536, "port must be between 1024 and 65535");
        String command = String.format(SshCommandConstants.CHECK_OS_PORT_CONFLICT, port);
        String result = execCommand(session, command);
        OpsAssert.isTrue(StrUtil.isNotEmpty(result) && NumberUtil.isInteger(result), "port conflict error :" + result);
        return NumberUtil.parseInt(result) <= 0;
    }

    /**
     * 检查当前用户是否存在
     *
     * @param sshLogin ssh登录信息
     * @return true 存在，false 不存在
     */
    public boolean checkOsUserExist(SshLogin sshLogin) {
        Session session = null;
        try {
            session = createSession(sshLogin);
            return session.isConnected();
        } catch (OpsException ex) {
            log.error("create session error ignore {}", ex.getMessage());
        } finally {
            if (Objects.nonNull(session)) {
                session.disconnect();
            }
        }
        return false;
    }

    /**
     * 创建用户
     *
     * @param rootLogin root登录信息
     * @param username 用户名
     */
    public void createOsUser(SshLogin rootLogin, String username) {
        String command = String.format(SshCommandConstants.CREATE_OS_USER, username);
        execCommand(rootLogin, command);
    }

    /**
     * check user has sudo no password permission
     *
     * @param sshLogin ssh login info
     * @return has permission or not
     */
    public Boolean checkOsUserSudo(SshLogin sshLogin) {
        String hasSudoResult = "true";
        String withoutSudoResult = "false";
        String command = String.format(SshCommandConstants.CREATE_OS_USER_SUDO, hasSudoResult, withoutSudoResult);
        String result = execCommand(sshLogin, command);

        if (result.contains(withoutSudoResult)
                || result.contains("a password is required")
                || result.contains("must be owned")
                || result.contains("command not found")) {
            return false;
        }
        return result.contains(hasSudoResult);
    }

    /**
     * Get the cpu info of the remote machine
     *
     * @param sshLogin ssh login info
     * @param hasNetName true 包含网卡名称，false 不包含网卡名称
     * @return net info
     */
    public String[] getNetMonitor(SshLogin sshLogin, boolean hasNetName) {
        String[] res = hasNetName ? new String[3] : new String[2];
        String netCardName = execCommand(sshLogin,
            String.format(SshCommandConstants.NET_CARD_NAME, sshLogin.getHost()));
        // rx_net|$tx_ne
        String rxNet = execCommand(sshLogin, String.format(SshCommandConstants.NET_MONITOR, netCardName));
        String[] split = rxNet.split("\\|");
        if (split.length == 2) {
            if (hasNetName) {
                res[0] = netCardName;
                res[1] = split[0];
                res[2] = split[1];
            } else {
                res[0] = split[0];
                res[1] = split[1];
            }
        } else {
            Arrays.fill(res, "-1");
        }
        return res;
    }

    /**
     * 获取cpu使用率
     *
     * @param sshLogin ssh登录信息
     * @return cpu使用率
     */
    public String getCpuUsing(SshLogin sshLogin) {
        return new JschExecutor().execCommand(sshLogin, SshCommandConstants.CPU_USING);
    }

    /**
     * get memory info of the remote machine
     *
     * @param sshLogin ssh login
     * @return memory info
     */
    public String getMemory(SshLogin sshLogin) {
        return new JschExecutor().execCommand(sshLogin, SshCommandConstants.MEMORY);
    }

    /**
     * get host base info : contains cpu core number, remaining memory, available disk space
     *
     * @param sshLogin ssh login
     * @return base info
     */
    public String getHostBaseInfo(SshLogin sshLogin) {
        return new JschExecutor().execCommand(sshLogin, SshCommandConstants.HOST_BASE_INFO);
    }

    /**
     * <pre>
     * Obtain the usage information of each partition of the server disk
     * eg.  df -T --total | tail -n +2| tr -s " "
     *
     * devtmpfs devtmpfs 15893580 0 15893580 0% /dev
     * tmpfs tmpfs 15904136 1396 15902740 1% /dev/shm
     * tmpfs tmpfs 15904136 1583488 14320648 10% /run
     * tmpfs tmpfs 15904136 0 15904136 0% /sys/fs/cgroup
     * /dev/sda1 ext4 61795096 57479008 1363804 98% /
     * /dev/sdb ext4 206292968 98393248 97397576 51% /data
     * /dev/sdc ext4 103080888 35819960 62001664 37% /data2
     * tmpfs tmpfs 3180828 0 3180828 0% /run/user/0
     * tmpfs tmpfs 3180828 0 3180828 0% /run/user/54355
     * tmpfs tmpfs 3180828 0 3180828 0% /run/user/54360
     * tmpfs tmpfs 3180828 0 3180828 0% /run/user/54354
     * total - 469764048 193277100 257773256 43% -
     *
     * eg.  df -T --total | tail -n +2| tr -s " " | grep -v total
     *
     * total - 469764048 193277100 257773256 43% -
     * </pre>
     *
     * @param sshLogin ssh login
     * @return base info
     */
    public String getDiskMonitor(SshLogin sshLogin) {
        return new JschExecutor().execCommand(sshLogin, SshCommandConstants.DISK_TOTAL_MONITOR);
    }

    /**
     * 检查操作系统用户是否存在
     *
     * @param sshLogin ssh登录信息
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    public boolean checkOsUserExist(SshLogin sshLogin, String username) {
        String command = String.format(SshCommandConstants.CHECK_OS_USER_EXIST, username);
        String result = execCommand(sshLogin, command);
        return !result.isEmpty() && !result.contains(SshCommandConstants.NO_SUCH_USER);
    }

    /**
     * 执行shell命令，该方法会主动关闭会话
     *
     * @param sshLogin ssh登录信息
     * @param command shell命令
     * @return 命令执行结果
     */
    public String execCommand(SshLogin sshLogin, String command) {
        return new JschExecutor().execCommand(sshLogin, command);
    }

    /**
     * 执行shell命令,该方法不主动关闭会话
     *
     * @param session ssh会话
     * @param command shell命令
     * @return 命令执行结果
     */
    public String execCommand(Session session, String command) {
        return new JschExecutor().execCommand(session, command);
    }

    /**
     * 执行shell命令,envPath为环境变量配置文件路径，如果envPath为空，则不加载环境变量配置文件
     *
     * @param sshLogin ssh登录信息
     * @param command shell命令
     * @param envPath 环境变量配置文件路径
     * @return 命令执行结果
     */
    public String execCommand(SshLogin sshLogin, String command, String envPath) {
        String envCommand = StrUtil.isNotEmpty(envPath) ? "source " + envPath + " && " + command : command;
        return new JschExecutor().execCommand(sshLogin, envCommand);
    }

    /**
     * 执行shell命令,envPath为环境变量配置文件路径，如果envPath为空，则不加载环境变量配置文件
     *
     * @param session ssh会话
     * @param command shell命令
     * @param envPath 环境变量配置文件路径
     * @return 命令执行结果
     */
    public String execCommand(Session session, String command, String envPath) {
        String envCommand = StrUtil.isNotEmpty(envPath) ? "source " + envPath + " && " + command : command;
        return new JschExecutor().execCommand(session, envCommand);
    }

    /**
     * 打开一个ChannelShell
     *
     * @param session ssh会话
     * @return ChannelShell
     */
    public ChannelShell openChannelShell(Session session) {
        return new JschExecutor().openChannelShell(session);
    }

    /**
     * 将ChannelShell绑定到WsSession
     *
     * @param channelShell channelShell
     * @param wsSession wsSession
     */
    public void channelToWsSession(ChannelShell channelShell, WsSession wsSession) {
        new JschExecutor().channelToWsSession(channelShell, wsSession);
    }

    /**
     * 创建一个ssh会话
     *
     * @param sshLogin ssh登录信息
     * @return ssh会话
     */
    public Session createSession(SshLogin sshLogin) {
        return new JschExecutor().createSession(sshLogin);
    }

    /**
     * 执行shell命令并自动填充人机交互响应
     *
     * @param sshLogin ssh登录信息
     * @param command command
     * @param envPath 环境变量配置文件路径
     * @param response 人机交互响应
     * @return 命令执行结果
     */
    public String execCommandAutoResponse(SshLogin sshLogin, String command, String envPath,
        Map<String, String> response) {
        String envCommand = StrUtil.isNotEmpty(envPath) ? "source " + envPath + " && " + command : command;
        return new JschExecutor().execCommandAutoResponse(sshLogin, envCommand, response);
    }

    /**
     * 获取openGasuus版本号
     *
     * @param sshLogin ssh会话
     * @param envPath 环境路径
     * @return openGauss版本号
     */
    public String getOpenGaussMainVersionNum(SshLogin sshLogin, String envPath) {
        String command = SshCommandConstants.OPENGAUSS_MAIN_VERSION_NUM;
        String envCommand = StrUtil.isNotEmpty(envPath) ? "source " + envPath + " && " + command : command;
        return new JschExecutor().execCommand(sshLogin, envCommand);
    }

    /**
     * 检查java版本号；java版本信息查询返回信息流JSCH ChannelExec ErrStream
     *
     * @param sshLogin ssh登录信息
     * @return java版本号
     */
    public String checkJavaVersion(SshLogin sshLogin) {
        String command = SshCommandConstants.JAVA_VERSION;
        return new JschExecutor().execCommandErrorStream(sshLogin, command);
    }

    /**
     * 检查路径是否为空，为空返回true
     *
     * @param sshLogin ssh登录信息
     * @param path 路径
     * @return true 路径为空，false 路径不为空
     */
    public boolean checkPathEmpty(SshLogin sshLogin, String path) {
        String command = String.format(SshCommandConstants.CHECK_PATH_EMPTY, path);
        String result = new JschExecutor().execCommandErrorStream(sshLogin, command);
        if (result.contains("ls: cannot access") && result.contains("Permission denied")) {
            throw new OpsException("checkPathEmpty error: ls cannot access, permission denied");
        }
        // ls: cannot access '/data/software/openGauss': No such file or directory
        boolean isEmpty = StrUtil.contains(result, SshCommandConstants.CHECK_RESULT_PATH_EMPTY)
            || result.contains("ls: cannot access") && result.contains("No such file or directory");
        log.info("check {} path {} is empty: {}", sshLogin, path, isEmpty);
        return isEmpty;
    }

    /**
     * 检查文件是否存在，存在返回true
     *
     * @param sshLogin ssh登录信息
     * @param file 文件绝对路径
     * @return true 文件存在，false 文件不存在
     */
    public boolean checkFileExist(SshLogin sshLogin, String file) {
        String command = String.format(SshCommandConstants.CHECK_FILE_EXIST, file);
        String result = new JschExecutor().execCommandErrorStream(sshLogin, command);
        return StrUtil.contains(result, SshCommandConstants.CHECK_RESULT_FILE_EXIST);
    }

    /**
     * sftp file copy
     *
     * @param session session
     * @param targetFile target file
     * @param installPath file copy path
     * @throws OpsException JSchException
     */
    public void scp(Session session, String targetFile, String installPath) {
        ChannelSftp sftpChannel = null;
        try {
            Channel channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                sftpChannel = (ChannelSftp) channel;
                sftpChannel.connect(5000); // 5秒超时
                sftpChannel.put(targetFile, installPath, ChannelSftp.OVERWRITE);
                String jarName = new File(targetFile).getName();
                String verifyCmd = String.format("[ -f \"%s/%s\" ] && echo 'ok' || echo 'fail'", installPath, jarName);
                String verifyResult = execCommand(session, verifyCmd);
                if (!"ok".equals(verifyResult.trim())) {
                    throw new OpsException("file check failed :" + session.getHost() + " fileName:" + targetFile);
                }
            } else {
                if (channel != null) {
                    channel.disconnect();
                }
                throw new JSchException("cannot open sftp channel" + session.getHost());
            }
        } catch (JSchException | SftpException e) {
            throw new OpsException(e.getMessage());
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
        }
    }

    /**
     * sftp file copy
     *
     * @param session session
     * @param content file content
     * @param installPath install path
     */
    public void remoteWrite(Session session, String content, String installPath) {
        ChannelSftp sftpChannel = null;
        try {
            Channel channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                sftpChannel = (ChannelSftp) channel;
                sftpChannel.connect(5000);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                sftpChannel.put(inputStream, installPath, ChannelSftp.OVERWRITE);
            } else {
                if (channel != null) {
                    channel.disconnect();
                }
                throw new JSchException("cannot open sftp channel" + session.getHost());
            }
        } catch (JSchException | SftpException e) {
            throw new OpsException(e.getMessage());
        } finally {
            if (sftpChannel != null) {
                sftpChannel.disconnect();
            }
        }
    }

    /**
     * execute with session
     *
     * @param sshLogin ssh login info
     * @param action action
     */
    public void executeWithSession(SshLogin sshLogin, Consumer<Session> action) {
        Session session = null;
        try {
            session = createSession(sshLogin);
            if (!session.isConnected()) {
                session.connect(3000);
            }
            action.accept(session);
        } catch (JSchException | OpsException e) {
            log.error("SSH create session error:", e);
            throw new OpsException("AGENT_UPDATE_ERROR: execute jsch task failed " + e.getMessage());
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * Jsch 执行命令器
     */
    static class JschExecutor {
        private static final int SESSION_TIMEOUT = 10000;
        private static final int CHANNEL_TIMEOUT = 50000;

        private WsConnectorManager wsConnectorManager = null;

        /**
         * Jsch 执行命令器 构造函数
         */
        public JschExecutor() {
        }

        /**
         * Jsch 执行命令器 构造函数
         *
         * @param wsConnectorManager wsConnectorManager
         */
        public JschExecutor(WsConnectorManager wsConnectorManager) {
            this.wsConnectorManager = wsConnectorManager;
        }

        /**
         * create session
         *
         * @param sshLogin sshLogin
         * @return session session
         * @throws OpsException OpsException
         */
        public Session createSession(SshLogin sshLogin) throws OpsException {
            Session session = null;
            try {
                JSch jsch = new JSch();
                // 创建与远程服务器的会话
                session = jsch.getSession(sshLogin.getUsername(), sshLogin.getHost(), sshLogin.getPort());
                // 设置会话的密码
                session.setPassword(sshLogin.getPassword());
                // 设置一些配置，例如不检查主机密钥
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                // 连接会话
                session.connect(SESSION_TIMEOUT);
            } catch (JSchException e) {
                String errorMessage = String.format("create jsch session error: %s", sshLogin);
                throw new OpsException(errorMessage);
            }
            return session;
        }

        /**
         * open channel shell
         *
         * @param session session
         * @return ChannelShell
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
         * channel to ws session
         *
         * @param channel channel
         * @param wsSession ws session
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
                            newSession.getSession()
                                .getBasicRemote()
                                .sendText(new String(bytes, StandardCharsets.UTF_8));
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
         * exec command and session will be created and closed
         *
         * @param sshLogin sshLogin
         * @param command command
         * @return result
         */
        public String execCommand(SshLogin sshLogin, String command) {
            SessionConfig execConfig = new SessionConfig(sshLogin.getHost(), sshLogin.getUsername(),
                sshLogin.getPassword()).withChannelType(ChannelType.EXEC).withTimeout(15000);
            try {
                ExecOperations.ExecResult result = ExecOperations.executeCommand(execConfig, command);
                return result.getResult();
            } catch (Exception ex) {
                throw new OpsException(
                    "exec command failed, sshLogin:" + sshLogin + " command:" + command + " ex:" + ex.getMessage());
            }
        }

        /**
         * exec commands and session will be created and closed
         *
         * @param sshLogin sshLogin
         * @param commands commands
         * @return result
         */
        public String execCommands(SshLogin sshLogin, String commands) {
            SessionConfig execConfig = new SessionConfig(sshLogin.getHost(), sshLogin.getUsername(),
                sshLogin.getPassword()).withChannelType(ChannelType.EXEC).withTimeout(15000);
            try {
                List<ExecOperations.ExecResult> results = ExecOperations.executeCommands(execConfig, commands);
                results.stream().filter(result -> result.getExitCode() != 0).findFirst().map(result -> {
                    throw new OpsException(
                        "exec command failed, sshLogin:" + sshLogin + " command:" + commands + " ex: "
                            + result.getError().toString());
                });
                return results.stream().map(ExecOperations.ExecResult::getOutput).collect(Collectors.joining());
            } catch (Exception ex) {
                throw new OpsException(
                    "exec command failed, sshLogin:" + sshLogin + " command:" + commands + " ex:" + ex.getMessage());
            }
        }

        /**
         * exec command and fetch result from error stream
         *
         * @param sshLogin sshLogin
         * @param command command
         * @return result
         */
        public String execCommandErrorStream(SshLogin sshLogin, String command) {
            Session session = null;
            try {
                session = createSession(sshLogin);
                return execCommandErrorStream(session, command);
            } catch (OpsException e) {
                throw new OpsException(e.getMessage());
            } finally {
                // 断开会话
                if (Objects.nonNull(session)) {
                    session.disconnect();
                }
            }
        }

        /**
         * exec command by session
         *
         * @param session session
         * @param command command
         * @return result
         */
        public String execCommand(Session session, String command) {
            Channel channel = null;
            StringBuilder result = new StringBuilder();
            try {
                if (!session.isConnected()) {
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.connect(SESSION_TIMEOUT);
                }
                channel = session.openChannel("exec");
                if (channel instanceof ChannelExec) {
                    ((ChannelExec) channel).setCommand(command);
                }
                try (InputStream in = channel.getInputStream();
                    InputStream err = ((ChannelExec) channel).getErrStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(err, StandardCharsets.UTF_8))) {
                    channel.connect();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line.trim()).append(CommonConstants.LINE_SPLITTER);
                    }
                    deleteCharAtEndOfStringBuffer(result);
                    StringBuilder errorResult = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResult.append(errorLine.trim()).append(CommonConstants.LINE_SPLITTER);
                    }
                    deleteCharAtEndOfStringBuffer(errorResult);
                    int exitStatus = channel.getExitStatus();
                    if (exitStatus != 0) {
                        log.error("Command: {} execution failed with exit status: {}", command, exitStatus);
                        log.error("Error output: {}", errorResult.toString().trim());
                    }
                }
            } catch (JSchException | IOException e) {
                throw new OpsException(e.getMessage());
            } finally {
                if (Objects.nonNull(channel)) {
                    channel.disconnect();
                }
            }
            return result.toString().trim();
        }

        /**
         * exec command and fetch result from error stream
         *
         * @param session session
         * @param command command
         * @return result
         */
        public String execCommandErrorStream(Session session, String command) {
            Channel channel = null;
            StringBuilder result = new StringBuilder();
            try {
                if (!session.isConnected()) {
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.connect(SESSION_TIMEOUT);
                }
                channel = session.openChannel("exec");
                if (channel instanceof ChannelExec) {
                    ((ChannelExec) channel).setCommand(command);
                }
                try (InputStream in = channel.getInputStream();
                    InputStream err = ((ChannelExec) channel).getErrStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(err, StandardCharsets.UTF_8))) {
                    channel.connect();
                    String errorLine;
                    while ((errorLine = reader.readLine()) != null) {
                        result.append(errorLine.trim()).append(CommonConstants.LINE_SPLITTER);
                    }
                    while ((errorLine = errorReader.readLine()) != null) {
                        result.append(errorLine.trim()).append(CommonConstants.LINE_SPLITTER);
                    }
                    deleteCharAtEndOfStringBuffer(result);
                    int exitStatus = channel.getExitStatus();
                    if (exitStatus != 0) {
                        log.error("Command: {} execution failed with exit status: {}", command, exitStatus);
                    }
                }
            } catch (JSchException | IOException e) {
                throw new OpsException(e.getMessage());
            } finally {
                if (Objects.nonNull(channel)) {
                    channel.disconnect();
                }
            }
            return result.toString().trim();
        }

        private void deleteCharAtEndOfStringBuffer(StringBuilder buffer) {
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
        }

        /**
         * SSH执行命令
         *
         * @param sshLogin ssh连接信息
         * @param command 命令
         * @param response 人机交互响应
         * @return 命令执行结果
         */
        public String execCommandAutoResponse(SshLogin sshLogin, String command, Map<String, String> response) {
            ChannelExec channel = null;
            Session session = null;
            try {
                session = createSession(sshLogin);
                Channel exeChannel = session.openChannel("exec");
                if (exeChannel instanceof ChannelExec) {
                    channel = (ChannelExec) exeChannel;
                }
                channel.setPtyType("dump");
                channel.setPty(true);
                channel.setCommand(command);
                channel.connect(CHANNEL_TIMEOUT);
                return buildJschResult(channel, response);
            } catch (JSchException | IOException e) {
                log.error("execCommandAutoResponse error :", e);
                throw new OpsException("Command execution exception");
            } finally {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }
        }

        private String buildJschResult(ChannelExec channelExec, Map<String, String> autoResponse) throws IOException {
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
                    resultStrBuilder.append(new String(tmp, 0, len, StandardCharsets.UTF_8));
                }
                if (channelExec.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    in.close();
                    out.close();
                    int exitStatus = channelExec.getExitStatus();
                    if (exitStatus != 0) {
                        throw new OpsException("Remote command execution failed, exit status: " + exitStatus);
                    }
                    break;
                }
                autoResponse(autoResponse, resultStrBuilder, out);
            }
            channelExec.disconnect();
            return resultStrBuilder.toString().trim();
        }

        private void autoResponse(Map<String, String> autoResponse, StringBuilder resultStrBuilder, OutputStream out) {
            if (CollUtil.isNotEmpty(autoResponse)) {
                autoResponse.forEach((k, v) -> {
                    if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
                        try {
                            out.write(
                                (v.trim() + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8));
                            out.flush();
                            resultStrBuilder.append(v.trim() + System.getProperty("line.separator"));
                        } catch (IOException e) {
                            log.error("Automatic response exception", e);
                        }
                    }
                });
            }
        }
    }

    @PreDestroy
    private void destroy() {
        JschSessionPool.closeAllPools();
    }
}
