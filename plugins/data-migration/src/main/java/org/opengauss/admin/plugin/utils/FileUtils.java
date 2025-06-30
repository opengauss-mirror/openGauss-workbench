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
 * FileUtils.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/utils/FileUtils.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @className: FileUtils
 * @author: xielibo
 * @date: 2023-03-23 16:38
 **/
public class FileUtils {

    public static String percentEncode(String s) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
        String percentEncodedFileName = percentEncode(realFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
                .append(percentEncodedFileName)
                .append(";")
                .append("filename*=")
                .append("utf-8''")
                .append(percentEncodedFileName);

        response.setHeader("Content-disposition", contentDispositionValue.toString());
    }

    /**
     * is remote file exists
     *
     * @param filePath file path
     * @param shellInfo shell info
     * @return true/false
     */
    public static boolean isRemoteFileExists(String filePath, ShellInfoVo shellInfo) {
        String command = String.format("[ -e %s ] && echo 0 || echo 1", filePath);
        JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, command);
        return jschResult.isOk() && Integer.parseInt(jschResult.getResult().trim()) == 0;
    }

    /**
     * cat remote file contents
     *
     * @param filePath file path
     * @param shellInfo shell info
     * @return file contents
     */
    public static String catRemoteFileContents(String filePath, ShellInfoVo shellInfo) {
        String command = String.format("cat %s", filePath);
        JschResult jschResult = ShellUtil.execCommandGetResult(shellInfo, command);
        return jschResult.isOk() ? jschResult.getResult().trim() : "";
    }

    /**
     * get remote file last modified time
     *
     * @param shellInfo shell info
     * @param remoteFilePath remote file path
     * @return last modified time
     * @throws JSchException jsch exception
     * @throws SftpException sftp exception
     */
    public static Long getRemoteFileLastModified(ShellInfoVo shellInfo, String remoteFilePath)
            throws JSchException, SftpException {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(shellInfo.getUsername(), shellInfo.getIp(), shellInfo.getPort());
            session.setPassword(shellInfo.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                channelSftp = (ChannelSftp) channel;
            }
            channelSftp.connect();

            SftpATTRS attrs = channelSftp.stat(remoteFilePath);
            return (long) attrs.getMTime() * 1000;
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
