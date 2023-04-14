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

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @className: ShellUtil
 * @author: xielibo
 * @date: 2023-01-16 21:55
 **/
@Slf4j
public class ShellUtil {

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

    public static String execCommandGetResult(String host, Integer port, String user, String password, String... commands) {
        StringBuilder sb = new StringBuilder(16);
        Session session = JschUtil.openSession(host, port, user, password);
        ChannelExec channelExec = null;
        InputStream in = null;
        try {
            channelExec = (ChannelExec) session.openChannel("exec");
            for (String command : commands) {
                channelExec.setCommand(command);
            }
            channelExec.setInputStream(null);
            channelExec.setErrStream(System.err);
            channelExec.connect();
            in = channelExec.getInputStream();
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr) ;
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
        } catch (JSchException e) {
            log.error("exec command error, message: {}", e.getMessage());
        } catch (IOException e) {
            log.error("exec command error, message: {}", e.getMessage());
        } finally {
            JschUtil.close(channelExec);
            JschUtil.close(session);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("exec command error, message: {}", e.getMessage());
                }
            }
        }
        return sb.toString();
    }
}
