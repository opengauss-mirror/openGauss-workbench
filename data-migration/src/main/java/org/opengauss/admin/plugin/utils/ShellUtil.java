package org.opengauss.admin.plugin.utils;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @className: ShellUtil
 * @author: xielibo
 * @date: 2023-01-16 21:55
 **/
public class ShellUtil {


    /**
     * Determine whether the directory exists
     *
     * @param directory
     * @return
     */
    public static boolean isDirExist(String directory, ChannelSftp sftp) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * Create directory
     */
    public static void createDir(String createpath, ChannelSftp sftp) {
        try {
            if (isDirExist(createpath, sftp)) {
                sftp.cd(createpath);
                return;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(filePath.toString(), sftp)) {
                    sftp.cd(filePath.toString());
                } else {
                    sftp.mkdir(filePath.toString());
                    sftp.cd(filePath.toString());
                }
            }
            sftp.cd(createpath);
        } catch (SftpException e) {
            throw new RuntimeException("create directory Error：" + createpath);
        }
    }

    public static String readFile(String host, Integer port, String user, String password,String filePath) {
        Session session = JschUtil.openSession(host, port, user, password);
        ChannelSftp sftp = null;
        StringBuilder sb = new StringBuilder(16);
        InputStream inputStream = null;
        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            inputStream = sftp.get(filePath);
            InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr) ;
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                sb.append("\n").append(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JschUtil.close(sftp);
            JschUtil.close(session);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

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
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            JschUtil.close(channelExec);
            JschUtil.close(session);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
