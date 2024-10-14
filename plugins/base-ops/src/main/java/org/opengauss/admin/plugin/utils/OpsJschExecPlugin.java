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
 * OpsJschExecPlugin.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/utils/OpsJschExecPlugin.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.vo.HostBean;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * cluster task operations
 *
 * @author wangchao
 * @since 2024/6/22 9:41
 **/
@Slf4j
@Component
@Scope("prototype")
public class OpsJschExecPlugin {
    private static final int SESSION_TIMEOUT = 10000;

    /**
     * exec command and session will be created and closed
     *
     * @param host host
     * @param command command
     * @return result
     */
    public String execCommand(HostBean host, String command) {
        Session session = null;
        BufferedReader reader = null;
        ChannelExec channelExec = null;
        StringBuffer result = new StringBuffer();
        try {
            JSch jsch = new JSch();
            // 创建与远程服务器的会话
            session = jsch.getSession(host.getUsername(), host.getHost(), host.getPort());
            // 设置会话的密码
            session.setPassword(host.getPassword());
            // 设置一些配置，例如不检查主机密钥
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            // 连接会话
            session.connect(SESSION_TIMEOUT);
            // 打开执行命令的通道
            Channel exec = session.openChannel("exec");
            if (exec instanceof ChannelExec) {
                channelExec = (ChannelExec) exec;
                channelExec.setCommand(command);
                channelExec.setPtyType("dump");
                channelExec.setPty(true);
                // 连接通道
                channelExec.connect();
                // 创建读取通道输入流的缓冲读取器
                reader = new BufferedReader(
                    new InputStreamReader(channelExec.getInputStream(), StandardCharsets.UTF_8));
            } else {
                throw new OpsException("Unable to open exec channel.");
            }
            String line;
            // 逐行读取并输出结果
            while ((line = reader.readLine()) != null) {
                result.append(line.trim()).append(System.lineSeparator());
            }
            result.delete(result.length() - 2, result.length());
        } catch (JSchException | IOException e) {
            throw new OpsException(e.getMessage());
        } finally {
            // 关闭读取器
            if (Objects.nonNull(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("close reader error");
                }
            }
            // 断开通道
            if (Objects.nonNull(channelExec)) {
                channelExec.disconnect();
            }
            // 断开会话
            if (Objects.nonNull(session)) {
                session.disconnect();
            }
        }
        return result.toString();
    }
}
