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

package org.opengauss.tun.utils;

import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.tun.config.LinuxConfig;

/**
 * JschUtil
 *
 * @author liu
 * @since 2023-12-20
 */
@Slf4j
public class JschUtil {
    private static Session session;

    /**
     * obtainSession
     *
     * @return Session Session
     */
    public static Session obtainSession(LinuxConfig config) {
        JSch ssh = new JSch();
        try {
            session = ssh.getSession(config.getUserName(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException exception) {
            throw new ServiceException("SSH connection failed to connect to the server "
                    + "where OpenGaussian is located. Please check the "
                    + "server user Om user password and check if the resource "
                    + "center has server information for the OpenGaussian database");
        }
        return session;
    }

    /**
     * isPathExists
     *
     * @param session session
     * @param path    path
     * @return boolean
     */
    public static boolean isPathExists(Session session, String path) {
        try {
            String command = "test -d " + path + " && echo exists || echo does not exist";
            ChannelExec channel = new ChannelExec();
            if (session.openChannel("exec") instanceof ChannelExec) {
                channel = (ChannelExec) session.openChannel("exec");
            }
            channel.setCommand(command);
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] result = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while ((len = in.read(result)) != -1) {
                sb.append(new String(result, 0, len, "UTF-8"));
            }
            channel.disconnect();
            session.disconnect();
            String output = sb.toString().trim();
            return output.equals("exists");
        } catch (IOException | JSchException exception) {
            log.error("isPathExists {}", exception.getMessage());
        }
        return false;
    }

    /**
     * closeSession
     *
     * @param session session
     */
    public static void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * executeCommand
     *
     * @param session session
     * @param command command
     * @return List<String> list
     */
    public static String executeCommand(Session session, String command) {
        if (session == null) {
            log.error("Error reported during command execution, session is empty");
            return "";
        }
        StringBuilder result = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        try {
            ChannelExec channelExec = new ChannelExec();
            if (session.openChannel("exec") instanceof ChannelExec) {
                channelExec = (ChannelExec) session.openChannel("exec");
            }
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            InputStream in = channelExec.getInputStream();
            InputStream errIn = channelExec.getErrStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errIn, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.isNotEmpty(line)) {
                    log.info(line);
                    result.append(line).append(System.lineSeparator());
                }
            }
            while ((line = errorReader.readLine()) != null) {
                if (StrUtil.isNotEmpty(line)) {
                    log.error(line);
                    errorBuilder.append(line).append(System.lineSeparator());
                }
            }
            reader.close();
            errorReader.close();
            channelExec.disconnect();
        } catch (JSchException | IOException exception) {
            log.error("Failed to execute startup instrumentation command-->{}", exception.getMessage());
            throw new ServiceException(exception.getMessage());
        }
        String error = errorBuilder.toString();
        if (StrUtil.isNotEmpty(error)) {
            result.append("Error:").append(System.lineSeparator()).append(error);
        }
        return result.toString();
    }
}
