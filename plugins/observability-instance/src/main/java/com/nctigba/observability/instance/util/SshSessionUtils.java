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
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/SshSessionUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.EnumSet;

import com.nctigba.observability.instance.enums.SshSessionPool;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.opengauss.admin.common.exception.CustomException;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshSessionUtils implements AutoCloseable {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 1000 * 60 * 5;

    public enum command {
        ARCH("arch"),
        CD("cd {0}"),
        LS("ls {0}"),
        STAT("stat {0}"),
        WGET("wget {0}"),
        TAR("tar zxf {0}"),
        UNZIP("unzip {0}"),
        APPEND_FILE(""),
        CHECK_USER("cat /etc/passwd | awk -F \":\" \"'{print $1}\"|grep {0} | wc -l"),
        CREATE_USER("useradd omm && echo ''{0} ALL=(ALL) ALL'' >> /etc/sudoers"),
        CHANGE_PASSWORD("passwd {1}"),
        PS("ps -ef|grep {0} |grep -v grep |grep {1,number,#}|awk '''{print $2}''"),
        KILL("kill -9 {0}");

        private String cmd;

        command(String cmd) {
            this.cmd = cmd;
        }

        public String parse(Object... args) {
            return MessageFormat.format(cmd, args);
        }
    }

    /**
     * check port if in use
     *
     * @param port port to check
     * @throws IOException     from
     *                         org.apache.sshd.client.session.ClientSession#createExecChannel
     * @throws CustomException when port in use throws Exception
     */
    public void testPortCanUse(int port) throws IOException {
        try {
            execute("netstat -tuln | grep " + port);
        } catch (CustomException e) {
            return;
        }
        throw new CustomException("port in use:" + port);
    }

    public boolean test(String command) throws IOException {
        try {
            execute(command);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public String execute(command command) throws IOException {
        return execute(command.cmd, null);
    }

    public String execute(String command) throws IOException {
        return execute(command, null);
    }

    public String execute(String command, OutputStream os) throws IOException {
        log.info("exec:" + command);
        var ec = session.createExecChannel(command);
        if (os == null)
            os = new ByteArrayOutputStream();
        ec.setOut(os);
        ec.setErr(os);
        ec.open();
        ec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        for (int i = 0; i < 100 && ec.getExitStatus() == null; i++)
            ThreadUtil.sleep(100L);
        if (ec.getExitStatus() != null && ec.getExitStatus() != 0)
            throw new CustomException(command + " \n " + os.toString().trim());
        return os.toString().trim();
    }

    public void executeNoWait(String command) throws IOException {
        log.info("execC:" + command);
        var c = session.createShellChannel();
        command = command + "\nexit\n";
        c.setIn(new ByteArrayInputStream(command.getBytes()));
        var os = new ByteArrayOutputStream();
        c.setOut(os);
        c.setErr(os);
        c.open();
        c.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        log.info(os.toString());
    }

    public synchronized void upload(InputStream in, String target) throws IOException {
        var sf = SftpClientFactory.instance().createSftpFileSystem(session);
        var path = sf.getDefaultDir().resolve(target);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }

    public synchronized void upload(String source, String target) throws IOException {
        log.info("from:{},to:{}", source, target);
        var sf = SftpClientFactory.instance().createSftpFileSystem(session);
        var path = sf.getDefaultDir().resolve(target);
        Files.copy(Paths.get(source), path, StandardCopyOption.REPLACE_EXISTING);
    }

    private static SshClient cli;
    private ClientSession session;

    private static final synchronized SshClient getClient() {
        if (cli == null) {
            cli = SshClient.setUpDefaultClient();
            cli.start();
        }
        return cli;
    }

    private SshSessionUtils(String host, Integer port, String username, String password) throws IOException {
        var cf = getClient().connect(username, host, port);
        session = cf.verify().getSession();
        try {
            session.addPasswordIdentity(password);
            session.auth().verify(SESSION_TIMEOUT);
        } catch (Exception e) {
            throw new RuntimeException(username + " password error");
        }
    }

    public static SshSessionUtils connect(String host, Integer port, String username, String password) throws IOException {
        return new SshSessionUtils(host, port, username, password);
    }

    /**
     * session is Open
     *
     * @return boolean
     */
    public boolean isConnected() {
        return session.isOpen();
    }

    /**
     * get session through sessionPool
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @return SshSession SshSession
     * @throws IOException IOException
     */
    public static SshSessionUtils getSession(String host, Integer port, String username, String password)
            throws IOException {
        return SshSessionPool.INSTANCE.getSession(host, port, username, password);
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}