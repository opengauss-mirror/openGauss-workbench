/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  SshSessionUtils.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/util/SshSessionUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.util;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;
import org.apache.sshd.sftp.client.fs.SftpPath;
import org.opengauss.admin.common.exception.base.BaseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.EnumSet;

@Slf4j
public class SshSessionUtils implements AutoCloseable {
    private static final int SESSION_TIMEOUT = 10000;

    private static final int CHANNEL_TIMEOUT = 1000 * 60 * 5;

    private static SshClient cli;

    private ClientSession session;

    private SshSessionUtils(String host, Integer port, String username, String password) throws IOException {
        ConnectFuture cf = getClient().connect(username, host, port);
        session = cf.verify().getSession();

        try {
            session.addPasswordIdentity(password);
            session.auth().verify(SESSION_TIMEOUT);
        } catch (IOException e) {
            throw new BaseException(username + " password error");
        }
    }

    public enum Command {
        ARCH("arch"), CD("cd {0}"), LS("ls {0}"), STAT("stat {0}"), WGET("wget {0}"), TAR("tar zxf {0}"),
        UNZIP("unzip {0}"), APPEND_FILE(""),
        CHECK_USER("cat /etc/passwd | awk -F \":\" \"'{print $1}\"|grep {0} | wc -l"),
        CREATE_USER("useradd omm && echo ''{0} ALL=(ALL) ALL'' >> /etc/sudoers"), CHANGE_PASSWORD("passwd {1}"),
        PS("ps -ef|grep {0} |grep -v grep |grep {1,number,#}|awk '''{print $2}''"), KILL("kill -9 {0}");

        private String cmd;

        Command(String cmd) {
            this.cmd = cmd;
        }

        public String parse(String... args) {
            return MessageFormat.format(cmd, args);
        }
    }

    public String execute(Command command) throws IOException {
        return execute(command.cmd, null);
    }

    public String execute(String command) throws IOException {
        return execute(command, null);
    }

    public String execute(String command, OutputStream os) throws IOException {
        log.info("exec:" + command);
        ChannelExec ec = session.createExecChannel(command);
        OutputStream os0 = os;
        if (os == null) {
            os0 = new ByteArrayOutputStream();
        }
        ec.setOut(os0);
        ec.setErr(os0);
        ec.open();
        ec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        for (int i = 0; i < 100 && ec.getExitStatus() == null; i++) {
            ThreadUtil.sleep(100L);
        }
        if (ec.getExitStatus() != null && ec.getExitStatus() != 0) {
            throw new BaseException(command + " " + System.getProperty("line.separator") + " " + os0.toString().trim());
        }
        try {
            return os0.toString().trim();
        } finally {
            if (os0 != null) {
                os0.close();
            }
        }
    }

    public void executeNoWait(String command) throws IOException {
        log.info("execC:" + command);
        ChannelShell channelShell = session.createShellChannel();
        String command0 = command + System.getProperty("line.separator") + "exit"
            + System.getProperty("line.separator");
        channelShell.setIn(new ByteArrayInputStream(command0.getBytes(Charset.defaultCharset())));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        channelShell.setOut(os);
        channelShell.setErr(os);
        channelShell.open();
        channelShell.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        log.info(os.toString());
    }

    private static final synchronized SshClient getClient() {
        if (cli == null) {
            cli = SshClient.setUpDefaultClient();
            cli.start();
        }
        return cli;
    }

    public synchronized void upload(InputStream in, String target) throws IOException {
        SftpFileSystem sf = SftpClientFactory.instance().createSftpFileSystem(session);
        SftpPath path = sf.getDefaultDir().resolve(target);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public synchronized void upload(String source, String target) throws IOException {
        log.info("from:{},to:{}", source, target);
        SftpFileSystem sf = SftpClientFactory.instance().createSftpFileSystem(session);
        SftpPath path = sf.getDefaultDir().resolve(target);
        Files.copy(Paths.get(source), path, StandardCopyOption.REPLACE_EXISTING);
    }

    public static SshSessionUtils connect(String host, Integer port, String username, String password)
        throws IOException {
        return new SshSessionUtils(host, port, username, password);
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}