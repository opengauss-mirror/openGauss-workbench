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

package org.opengauss.collect.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.collect.config.common.Constant;
import org.opengauss.collect.domain.LinuxConfig;
import org.opengauss.admin.common.exception.ServiceException;

/**
 * JschUtil
 *
 * @author liu
 * @since 2023-09-17
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
            log.error("JschUtil obtainSession error-->{}", exception.getMessage());
            throw new ServiceException(exception.getMessage());
        }
        return session;
    }

    /**
     * upload
     *
     * @param session session
     */
    public static void upload(Session session, String uploadPath, Map<String, InputStream> map) {
        AssertUtil.isTrue(CollectionUtil.isEmpty(map), "upload file can not is empty");
        AssertUtil.isTrue(ObjectUtil.isEmpty(session), "JschUtil upload occur error session is null,"
                + "Failed to start uploading the instrumentation file");
        // Open the sftp channel
        ChannelSftp channelTrans = new ChannelSftp();
        try {
            if (session.openChannel("sftp") instanceof ChannelSftp) {
                channelTrans = (ChannelSftp) session.openChannel("sftp");
            }
            channelTrans.connect();
            // Check if the path exists, create if it does not exist
            try {
                // 判断目录是否存在
                channelTrans.ls(uploadPath);
                log.info("the path already exists, no need to create it");
            } catch (SftpException e) {
                // 目录不存在，创建目录
                String command = "cd / && mkdir -p " + uploadPath;
                executeCommand(session, command);
                log.info("directory created successfully");
            }
            channelTrans.cd(uploadPath);
            for (Map.Entry<String, InputStream> entry : map.entrySet()) {
                try {
                    // Check if the file already exists on the remote server
                    if (checkFileExists(channelTrans, uploadPath, entry.getKey())) {
                        log.info("File already exists on the remote server: {}", entry.getKey());
                        // Skip uploading if file already exists
                        continue;
                    }
                    log.info("Start uploading files--->{}", entry.getKey());
                    channelTrans.put(entry.getValue(), entry.getKey(), ChannelSftp.OVERWRITE);
                    log.info("End uploading files--->{}", entry.getKey());
                } catch (SftpException e) {
                    channelTrans.disconnect();
                    throw new ServiceException("upload file" + entry.getKey() + " occur error");
                }
                // Process key and value as needed
            }
        } catch (JSchException | SftpException e) {
            channelTrans.disconnect();
            throw new ServiceException(e.getMessage());
        }
        channelTrans.disconnect();
    }

    private static boolean checkFileExists(ChannelSftp channel, String remoteFilePath, String fileName)
            throws SftpException {
        // Method to check if a file exists in the remote directory
        if (fileName.equals(Constant.ASSESS_PROPERTIES)) {
            return false;
        }
        Vector<LsEntry> fileList = channel.ls(remoteFilePath);
        for (ChannelSftp.LsEntry entry : fileList) {
            if (entry.getFilename().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checkFileExistence
     *
     * @param session  session
     * @param filePath filePath
     * @return boolean
     */
    public static boolean checkFileExistence(Session session, String filePath) {
        try {
            ChannelSftp channelSftp = new ChannelSftp();
            Channel channel = session.openChannel("sftp");
            if (channel instanceof ChannelSftp) {
                channelSftp = (ChannelSftp) channel;
            }
            channelSftp.connect();
            // Throws an exception if the file does not exist
            channelSftp.lstat(filePath);
            channelSftp.disconnect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * downLoad
     *
     * @param session   session
     * @param linuxPath linuxPath
     * @param out       out
     */
    public static void downLoad(Session session, String linuxPath, OutputStream out) {
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = new ChannelSftp();
            if (channel instanceof ChannelSftp) {
                sftpChannel = (ChannelSftp) channel;
            }
            if (ObjectUtil.isNotEmpty(out)) {
                sftpChannel.get(linuxPath, out);
                sftpChannel.disconnect();
            }
            log.info("File downloaded successfully.");
        } catch (JSchException | SftpException exception) {
            log.error("File downloaded fail-->{}", exception.getMessage());
            throw new ServiceException("File downloaded fail");
        }
    }

    /**
     * downLoadLocal
     *
     * @param session   session
     * @param linuxPath linuxPath
     * @param localPath localPath
     */
    public static String downLoadLocal(Session session, String linuxPath, String localPath) {
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = new ChannelSftp();
            if (channel instanceof ChannelSftp) {
                sftpChannel = (ChannelSftp) channel;
            }
            File localDirectory = new File(localPath);
            if (!localDirectory.exists()) {
                localDirectory.mkdirs();
            }
            sftpChannel.get(linuxPath, localPath);
            sftpChannel.disconnect();
            log.info("Successfully downloaded to local" + linuxPath);
        } catch (JSchException | SftpException exception) {
            log.error("File downloaded to local-->{}", exception.getMessage());
            return "fail downloaded to local";
        }
        return "success";
    }

    /**
     * getFileNamesByPath
     *
     * @param session   session
     * @param linuxPath linuxPath
     * @param isSql     isSql
     * @return List<String> list
     */
    public static List<String> getFileNamesByPath(Session session, String linuxPath, boolean isSql) {
        List<String> fileNames = new ArrayList<>();
        try {
            Channel channel = session.openChannel("sftp");
            ChannelSftp channelSftp = new ChannelSftp();
            if (channel instanceof ChannelSftp) {
                channelSftp = (ChannelSftp) channel;
            }
            channelSftp.connect();
            // Get the list of files in the linuxPath directory
            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(linuxPath);
            // Add the names of .sql and .txt files to the list
            for (ChannelSftp.LsEntry entry : entries) {
                String filename = entry.getFilename();
                if (isSql && filename.endsWith(".attach")) {
                    fileNames.add(filename);
                }
            }
            channelSftp.disconnect();
        } catch (SftpException | JSchException exception) {
            log.error(exception.getMessage());
            throw new ServiceException(exception.getMessage());
        }
        return fileNames;
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
            log.error("error reported during command execution, session is empty");
            return "";
        }
        StringBuilder result = new StringBuilder();
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            String line;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.isNotEmpty(line)) {
                    log.info(line);
                    result.append(line).append(System.lineSeparator());
                }
            }
            reader.close();
            channelExec.disconnect();
        } catch (JSchException | IOException exception) {
            log.error("Failed to execute startup instrumentation command-->{}", exception.getMessage());
            throw new ServiceException(exception.getMessage());
        }
        return result.toString();
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
     * executeTask
     *
     * @param command command
     */
    public static String executeTask(String command, Session session, String path) {
        if (session == null) {
            throw new ServiceException("Connection to Linux failed to obtain session");
        }
        Map<String, InputStream> map = new HashMap<>();
        InputStream agentStream = JschUtil.class.getClassLoader()
                .getResourceAsStream(Constant.INSERTION_AGENTNAME_PATH);
        map.put(Constant.INSERTION_AGENTNAME, agentStream);
        InputStream attachStream = JschUtil.class.getClassLoader()
                .getResourceAsStream(Constant.INSERTION_ATTACHNAME_PATH);
        map.put(Constant.INSERTION_ATTACHNAME, attachStream);
        upload(session, path, map);
        log.info("<------------------------------------------------------------->");
        log.info("Start executing the pile insertion start command--->{}", command);
        String res = executeCommand(session, command);
        log.info("Close session");
        closeSession(session);
        return res;
    }
}
