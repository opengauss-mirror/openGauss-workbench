package com.nctigba.observability.instance.util;

import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nctigba.observability.instance.model.ResInfo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class SSHOperator {
    private final String host;
    private final Integer port;
    private final String userName;
    private final String password;
    private byte[] privateKeyFileContent;
    private boolean isUsedPrivateKey;
    private static byte[] defaultPrivateKeyFileContent = null;
    private int firstConnectTimeout;
    private int secondConnectTimeout;
    private int soTimeout;
    private int sftpConnectTimeout;
    private Session session;

    public SSHOperator(String host, Integer port, String userName, String password) {
        this.isUsedPrivateKey = false;
        this.firstConnectTimeout = 40000;
        this.secondConnectTimeout = 20000;
        this.soTimeout = 7200000;
        this.sftpConnectTimeout = 120000;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public SSHOperator(String host, Integer port, String userName, byte[] privateKeyFileContent, String password) {
        this.isUsedPrivateKey = false;
        this.firstConnectTimeout = 40000;
        this.secondConnectTimeout = 20000;
        this.soTimeout = 7200000;
        this.sftpConnectTimeout = 120000;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.privateKeyFileContent = privateKeyFileContent;
        this.isUsedPrivateKey = true;
        this.password = password;
    }

    public static byte[] getDefaultPrivateKeyFileContent() {
        return defaultPrivateKeyFileContent;
    }


    public void connect() {
        if (this.session == null) {
            try {
                createSession();
            } catch (JSchException e) {
                log.error(e.getMessage());
                close();
                return;
            }
        }
        try {
            this.session.connect(this.firstConnectTimeout);
        } catch (Exception e2) {
            if (this.secondConnectTimeout > 0) {
                try {
                    this.session.connect(this.secondConnectTimeout);
                } catch (Exception e3) {
                    log.error("ssh connect error. host={}, port={}, userName={}. message={}", this.host, this.port, this.userName, e3.getMessage());
                    close();
                }
            } else {
                log.error("ssh connect error. host={}, port={}, userName={}. message={}", this.host, this.port, this.userName, e2.getMessage());
                close();
            }
        }
    }

    public boolean isConnected() {
        if (this.session == null) {
            return false;
        }
        boolean isConnected = this.session.isConnected();
        if (!(isConnected)) {
            close();
        }
        return isConnected;
    }

    public Session getSession() {
        if (!isConnected()) {
            connect();
        }
        return this.session;
    }

    private void createSession() throws JSchException {
        if (this.session == null) {
            synchronized (this) {
                    JSch jsch = new JSch();
                    if (this.isUsedPrivateKey) {
                        jsch.addIdentity("jsch", (this.privateKeyFileContent != null) ? this.privateKeyFileContent : getDefaultPrivateKeyFileContent(), null,
                                (StringUtils.isNotEmpty(this.password) ? this.password.getBytes() : null));
                    }
                    if (this.port == null) {
                        this.session = jsch.getSession(this.userName, this.host);
                    } else {
                        this.session = jsch.getSession(this.userName, this.host, this.port.intValue());
                    }
                    if ((!(this.isUsedPrivateKey)) && (this.password != null) && (this.password.length() > 0)) {
                        this.session.setPassword(this.password);
                    }
                    Properties config = new Properties();
                    config.put("userauth.gssapi-with-mic", "no");
                    config.put("StrictHostKeyChecking", "no");
                    this.session.setConfig(config);
                    this.session.setTimeout(this.soTimeout);
            }
        }
    }

    public void close() {
        if (this.session != null) {
            this.session.disconnect();
            this.session = null;
        }
    }

    /**
     * Execute the shell command and return the execution result
     * 
     * @param commands 
     * @return String results of enforcement
     */
    @Deprecated
    public String executeCommandReturnStr(String commands) {
        ResInfo resInfo = executeCommand(commands);
        return ((resInfo == null) ? null : resInfo.getOutRes());
    }

    /**
     * Executing shell command returns execution result information
     * 
     * @param commands shell命令
     * @return ResInfo Execution result information
     */
    @Deprecated
    public ResInfo executeCommand(String commands) {
        return executeCommand(commands, 10);
    }

    /**
     * Executing shell command returns execution result information
     * 
     * @param commands Shell command
     * @param waitResultInterval Wait time after execution
     * @return ResInfo Execution result information
     */
    @Deprecated
    public ResInfo executeCommand(String commands, int waitResultInterval) {
        if (waitResultInterval < 10) {
            waitResultInterval = 10;
        }
        ResInfo resInfo = null;
        if ((commands == null) || (commands.length() == 0)) {
            return resInfo;
        }
        ChannelExec channel = null;
        byte[] tmp = new byte[1024];
        StringBuffer strBuffer = new StringBuffer();
        StringBuffer errResult = new StringBuffer();
        boolean closeSession = false;
        try {
            Session curSession = getSession();
            if (curSession == null) {
                return null;
            }
            channel = (ChannelExec) curSession.openChannel("exec");
            channel.setCommand(commands);
            channel.setInputStream(null);
            channel.setErrStream(null);
            channel.connect(this.sftpConnectTimeout);
            InputStream stdStream = channel.getInputStream();
            InputStream errStream = channel.getErrStream();
            while (true) {
                int i;
                if (errStream.available() > 0) {
                    i = errStream.read(tmp, 0, 1024);
                    if (i >= 0) {
                        errResult.append(new String(tmp, 0, i));
                    }
                }

                while (stdStream.available() > 0) {
                    i = stdStream.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    strBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    break;
                }
                if (waitResultInterval <= 0) {
                    continue;
                }
                try {
                    Thread.sleep(waitResultInterval);
                } catch (Exception ee) {
                    Thread.currentThread().interrupt();
                    ee.printStackTrace();
                }
            }
            int code = channel.getExitStatus();
            resInfo = new ResInfo(code, strBuffer.toString(), errResult.toString());
            stdStream.close();
            errStream.close();
            return resInfo;
        } catch (JSchException e) {
            log.error(e.getMessage());
            closeSession = true;
        } catch (Exception e2) {
            log.error(e2.getMessage());
            closeSession = true;
            Thread.currentThread().interrupt();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (closeSession) {
                close();
            }
        }
        return resInfo;
    }
}
