/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 */

package org.opengauss.jsch.pool;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import org.opengauss.jsch.pool.config.SessionConfig;
import org.opengauss.jsch.pool.exception.JschPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SftpOperations
 *
 * @author: wangchao
 * @Date: 2025/8/4 14:08
 * @since 7.0.0-RC2
 **/
public class SftpOperations {
    private static final Logger logger = LoggerFactory.getLogger(SftpOperations.class);
    private static final int DEFAULT_PORT = 22;

    /**
     * upload file with progress monitor
     *
     * @param config session config
     * @param localPath local file path
     * @param remotePath remote file path
     * @param callback progress callback
     * @throws JschPoolException upload exception
     */
    public static void upload(SessionConfig config, String localPath, String remotePath, ProgressCallback callback)
        throws JschPoolException {
        upload(config, new File(localPath), remotePath, callback);
    }

    /**
     * download file with progress monitor
     *
     * @param config session config
     * @param remotePath remote file path
     * @param localPath local file path
     * @param callback progress callback
     * @throws JschPoolException download exception
     */
    public static void download(SessionConfig config, String remotePath, String localPath, ProgressCallback callback)
        throws JschPoolException {
        download(config, remotePath, new File(localPath), callback);
    }

    /**
     * download file with progress monitor
     *
     * @param config session config
     * @param remotePath remote file path
     * @param localFile local file object
     * @param callback progress callback
     * @throws JschPoolException download exception
     */
    public static void download(SessionConfig config, String remotePath, File localFile, ProgressCallback callback)
        throws JschPoolException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getSftpChannel(session);
            Path normalizedLocalPath;
            try {
                normalizedLocalPath = Paths.get(localFile.getAbsolutePath()).normalize().toAbsolutePath();
            } catch (InvalidPathException e) {
                throw new JschPoolException("Invalid local file path", e);
            }
            File safeLocalFile = normalizedLocalPath.toFile();
            File parentDir = safeLocalFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            String normalizedRemotePath = remotePath.replace("\\", "/");
            if (normalizedRemotePath.contains("../") || normalizedRemotePath.contains("/..")) {
                throw new JschPoolException("Remote path contains directory traversal attempts");
            }
            SftpATTRS attrs = channel.stat(normalizedRemotePath);
            long fileSize = attrs.getSize();
            SftpProgressMonitor monitor = new CallbackProgressMonitor(callback, fileSize);
            channel.get(normalizedRemotePath, safeLocalFile.getAbsolutePath(), monitor, ChannelSftp.RESUME);
            if (callback != null) {
                callback.completed();
            }
        } catch (JSchException | SftpException e) {
            if (callback != null) {
                callback.failed(e);
            }
            throw new JschPoolException("download file failed", e);
        } finally {
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }

    /**
     * upload file with progress monitor
     *
     * @param config session config
     * @param localFile local file object
     * @param remotePath remote file path
     * @param callback progress callback
     * @throws JschPoolException upload exception
     */
    public static void upload(SessionConfig config, File localFile, String remotePath, ProgressCallback callback)
        throws JschPoolException {
        Session session = null;
        ChannelSftp channel = null;
        FileInputStream fis = null;
        try {
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getSftpChannel(session);
            fis = new FileInputStream(localFile);
            SftpProgressMonitor monitor = new CallbackProgressMonitor(callback, localFile.length());
            createParentDirs(channel, remotePath);
            channel.put(fis, remotePath, monitor, ChannelSftp.OVERWRITE);
            if (callback != null) {
                callback.completed();
            }
        } catch (JSchException | SftpException | FileNotFoundException e) {
            if (callback != null) {
                callback.failed(e);
            }
            throw new JschPoolException("upload file failed", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("close file input stream failed", e);
                }
            }
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }

    /**
     * create remote directory (recursively create parent directories)
     *
     * @param channel sftp channel
     * @param remotePath remote file path
     * @throws SftpException create directory exception
     */
    private static void createParentDirs(ChannelSftp channel, String remotePath) throws SftpException {
        String parent = new File(remotePath).getParent().replace("\\", "/");
        if (parent != null && !parent.equals("/")) {
            try {
                channel.stat(parent);
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    createParentDirs(channel, parent);
                }
            }
            channel.mkdir(parent);
        }
    }

    /**
     * progress monitor adapter
     */
    private static class CallbackProgressMonitor implements SftpProgressMonitor {
        private final ProgressCallback callback;
        private final long totalSize;
        private final AtomicLong transferred = new AtomicLong(0);

        /**
         * constructor
         *
         * @param callback progress callback
         * @param totalSize total size
         */
        public CallbackProgressMonitor(ProgressCallback callback, long totalSize) {
            this.callback = callback;
            this.totalSize = totalSize;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
        }

        @Override
        public boolean count(long count) {
            long current = transferred.addAndGet(count);
            callback.progress(current, totalSize);
            return true;
        }

        @Override
        public void end() {
        }
    }

    /**
     * delete file
     *
     * @param config session config
     * @param remotePath remote file path
     * @throws JschPoolException delete file exception
     */
    public static void deleteFile(SessionConfig config, String remotePath) throws JschPoolException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getSftpChannel(session);
            channel.rm(remotePath);
        } catch (JSchException | SftpException e) {
            throw new JschPoolException("delete file failed", e);
        } finally {
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }

    /**
     * create directory
     *
     * @param config session config
     * @param remotePath remote directory path
     * @throws JschPoolException create directory exception
     */
    public static void createDirectory(SessionConfig config, String remotePath) throws JschPoolException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getSftpChannel(session);
            createParentDirs(channel, remotePath);
            channel.mkdir(remotePath);
        } catch (JSchException | SftpException e) {
            throw new JschPoolException("create directory failed", e);
        } finally {
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }

    /**
     * check file exists
     *
     * @param config session config
     * @param remotePath remote file path
     * @return true:exists false:not exists
     * @throws JschPoolException check file exists exception
     */
    public static boolean exists(SessionConfig config, String remotePath) throws JschPoolException {
        Session session = null;
        ChannelSftp channel = null;
        try {
            session = JschSessionPool.borrowSession(config);
            channel = ChannelProvider.getSftpChannel(session);
            channel.stat(remotePath);
            return true;
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            }
            throw new JschPoolException("check file exists failed", e);
        } catch (JSchException ex) {
            throw new JschPoolException("check file exists failed", ex);
        } finally {
            ChannelProvider.closeChannel(channel);
            if (session != null) {
                JschSessionPool.returnSession(config, session);
            }
        }
    }
}
