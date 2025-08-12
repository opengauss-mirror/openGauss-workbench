/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 */

package org.opengauss.jsch.pool;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * ChannelProvider
 *
 * @author: wangchao
 * @Date: 2025/8/4 16:26
 * @since 7.0.0-RC2
 **/
public class ChannelProvider {
    /**
     * get sftp channel
     *
     * @param session session
     * @return sftp channel
     * @throws JSchException JSchException
     */
    public static ChannelSftp getSftpChannel(Session session) throws JSchException {
        if (!session.isConnected()) {
            throw new JSchException("Session is not connected");
        }
        Channel channel = session.openChannel("sftp");
        if (channel instanceof ChannelSftp sftp) {
            sftp.connect();
            return sftp;
        }
        throw new JSchException("Channel is not sftp");
    }

    /**
     * get exec channel
     *
     * @param session session
     * @return exec channel
     * @throws JSchException JSchException
     */
    public static ChannelExec getExecChannel(Session session) throws JSchException {
        if (!session.isConnected()) {
            throw new JSchException("Session is not connected");
        }
        Channel channel = session.openChannel("exec");
        if (channel instanceof ChannelExec exec) {
            return exec;
        }
        throw new JSchException("Channel is not exec");
    }

    /**
     * close channel
     *
     * @param channel channel
     */
    public static void closeChannel(Channel channel) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }
}
