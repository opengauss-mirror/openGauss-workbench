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
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/SshSessionUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.opengauss.admin.common.exception.CustomException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.EnumSet;

@Slf4j
public class SshSessionUtils implements AutoCloseable {
    private static final int SESSION_TIMEOUT = 10000;

    private static final int CHANNEL_TIMEOUT = 50000;

    private static SshClient cli;

    private ClientSession session;

    private SshSessionUtils(String host, Integer port, String username, String password) {
        try {
            var cf = getClient().connect(username, host, port);
            session = cf.verify().getSession();
            session.addPasswordIdentity(password);
            session.auth().verify(SESSION_TIMEOUT);
        } catch (IOException e) {
            throw new CustomException(username + " password error");
        }
    }

    private static final synchronized SshClient getClient() {
        if (cli == null) {
            cli = SshClient.setUpDefaultClient();
            cli.start();
        }
        return cli;
    }

    /**
     * command
     *
     * @author luomeng
     * @since 2024/1/30
     */
    public enum Command {
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

        Command(String cmd) {
            this.cmd = cmd;
        }

        /**
         * Parse
         *
         * @param args Object
         * @return String
         */
        public String parse(Object... args) {
            return MessageFormat.format(cmd, args);
        }
    }

    /**
     * Test command
     *
     * @param command String
     * @return boolean
     * @throws IOException Exception info
     */
    public boolean test(String command) throws IOException {
        try {
            execute(command);
            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    /**
     * Execute command
     *
     * @param command String
     * @return String
     * @throws IOException Exception info
     */
    public String execute(Command command) throws IOException {
        return execute(command.cmd, null);
    }

    /**
     * Execute command
     *
     * @param command String
     * @return String
     * @throws IOException Exception info
     */
    public String execute(String command) throws IOException {
        return execute(command, null);
    }

    /**
     * Execute command
     *
     * @param command String
     * @param os      OutputStream
     * @return String
     * @throws IOException Exception info
     */
    public String execute(String command, OutputStream os) throws IOException {
        var ec = session.createExecChannel(command);
        OutputStream outputStream = (os != null) ? os : new ByteArrayOutputStream();
        ec.setOut(outputStream);
        ec.setErr(outputStream);
        ec.open();
        ec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        if (ec.getExitStatus() != null && ec.getExitStatus() != 0) {
            throw new CustomException(
                    command + " " + System.getProperty("line.separator") + " " + outputStream.toString().trim());
        }
        return outputStream.toString().trim();
    }

    /**
     * Execute command no wait
     *
     * @param command String
     * @throws IOException Exception info
     */
    public void executeNoWait(String command) throws IOException {
        log.info("execC:" + command);
        var c = session.createShellChannel();
        String cmd = command + System.getProperty("line.separator") + "exit" + System.getProperty("line.separator");
        c.setIn(new ByteArrayInputStream(cmd.getBytes(StandardCharsets.UTF_8)));
        var os = new ByteArrayOutputStream();
        c.setOut(os);
        c.setErr(os);
        c.open();
        c.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        log.info(os.toString());
    }

    /**
     * Upload data
     *
     * @param in     InputStream
     * @param target String
     * @throws IOException Exception info
     */
    public synchronized void upload(InputStream in, String target) throws IOException {
        var sf = SftpClientFactory.instance().createSftpFileSystem(session);
        var path = sf.getDefaultDir().resolve(target);
        Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Upload
     *
     * @param source String
     * @param target String
     * @throws IOException Exception info
     */
    public synchronized void upload(String source, String target) throws IOException {
        var sf = SftpClientFactory.instance().createSftpFileSystem(session);
        var path = sf.getDefaultDir().resolve(target);
        Files.copy(Paths.get(source), path, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Get arch
     *
     * @param host     String
     * @param port     String
     * @param username String
     * @param password String
     * @return String
     * @throws IOException Exception info
     */
    public static SshSessionUtils connect(String host, Integer port, String username,
            String password) throws IOException {
        return new SshSessionUtils(host, port, username, password);
    }

    @Override
    public void close() throws IOException {
        session.close();
    }
}