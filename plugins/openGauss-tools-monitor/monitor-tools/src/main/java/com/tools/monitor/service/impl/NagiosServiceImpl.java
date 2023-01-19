/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.service.impl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import cn.hutool.core.util.StrUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.tools.monitor.entity.SysConfig;
import org.springframework.stereotype.Service;

/**
 * NagiosServiceImpl
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
@Service
public class NagiosServiceImpl {
    private static final String DEFAULT_PATH = "/usr/local/nagios";

    private static final String WRAP_STR = StrUtil.LF;

    private static final String TABLE = StrUtil.TAB;

    private static final String XIEG = StrUtil.SLASH;

    private static final Connection EMPY = null;

    private static final String STREMPY = null;

    private static final String START_CLIENT = "%s/bin/nrpe -c %s/etc/nrpe.cfg -d";

    private static final String CAT_NRPE = "cat %s/etc/nrpe.cfg";

    private static final String LS_SH = "cd %s/libexec/&&ls";

    private static final String NRPE_CONFIG = "command[%s]=%s/libexec/%s.sh";

    private static final String SH_CONFIG = "echo 'Memory: OK value: %s |value=%s;;;;'";

    private static final String WRITE_NRPE = "cd %s/etc/&&echo \"%s\" >> nrpe.cfg";

    private static final String CREATE_SH = "cd %s/libexec/&&touch %s.sh&&chmod 777 %s.sh";

    private static final String WRITE_SH = "cd %s/libexec/&&echo \"%s\" > %s.sh";

    private static final String START_HTTPD = "systemctl restart httpd";

    private static final String START_NAGIOS = "systemctl restart nagios";

    private static final String CAT_SERVICES = "cat %s/etc/objects/services.cfg";

    private static final String CAT_COMMANDS = "cat %s/etc/objects/commands.cfg";

    private static final String SERVICES_CONFIG = "define service{" + WRAP_STR
            + "use" + TABLE + TABLE + TABLE + "generic-service,service-pnp" + WRAP_STR + "host_name               "
            + "client" + WRAP_STR + "service_description" + TABLE + "%s" + WRAP_STR + "check_command" + TABLE + TABLE
            + "check_nrpe!%s" + WRAP_STR + TABLE + "}";

    private static final String COMMANDS_CONFIG = "define command{" + WRAP_STR + "command_name        %s" + WRAP_STR
            + "command_line        \\$USER1$/%s.sh" + WRAP_STR + "        }";

    private static final String WRITE_SERVICES = "cd %s/etc/objects/&&echo \"%s\" >> services.cfg";

    private static final String WRITE_COMMANDS = "cd %s/etc/objects/&&echo \"%s\" >> commands.cfg";

    private static Connection write = null;

    private static Connection connServer;

    private static Connection connClient;

    /**
     * setConfig
     *
     * @param map       map
     * @param sysConfig sysConfig
     */
    public void setConfig(Map<String, Object> map, SysConfig sysConfig) {
        while (true) {
            connServer = getConn(sysConfig.getServerIp(), sysConfig.getServerName(), sysConfig.getServerPassword());
            connClient = getConn(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
            if (connClient != null || connServer != null) {
                break;
            }
        }
        serverConfig(map, sysConfig, connServer);
        clientConfig(map, sysConfig, connClient);
    }

    private void serverConfig(Map<String, Object> map, SysConfig sysConfig, Connection connServer) {
        log.info("serverConfig");
        String path = DEFAULT_PATH;
        if (StrUtil.isNotBlank(sysConfig.getServerPath())) {
            path = sysConfig.getServerPath();
        }
        String command = null;
        StringBuffer buffer = new StringBuffer();
        if (true) {
            command = String.format(Locale.ROOT, CAT_SERVICES, path);
            String servicesResult = executeCmdAndGetResult(command, connServer);
            command = String.format(Locale.ROOT, CAT_COMMANDS, path);
            String commandsResult = executeCmdAndGetResult(command, connServer);
            boolean isStart = false;
            StringBuffer servicesBuf = new StringBuffer();
            StringBuffer commandsBuf = new StringBuffer();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (servicesResult != null && !servicesResult.contains(entry.getKey())) {
                    isStart = true;
                    String config = WRAP_STR
                            + String.format(Locale.ROOT, SERVICES_CONFIG, entry.getKey(), entry.getKey());
                    servicesBuf.append(config);
                }
                if (commandsResult != null && !commandsResult.contains(entry.getKey())) {
                    isStart = true;
                    String config = WRAP_STR
                            + String.format(Locale.ROOT, COMMANDS_CONFIG, entry.getKey(), entry.getKey());
                    commandsBuf.append(config);
                }
            }
            if (servicesBuf.length() > 0 && commandsBuf.length() > 0) {
                String server = String.format(Locale.ROOT, WRITE_SERVICES, path, servicesBuf);
                String client = String.format(Locale.ROOT, WRITE_COMMANDS, path, commandsBuf);
                executeCmd(server, connServer);
                executeCmd(client, connServer);
            }
            if (isStart) {
                StringBuffer start = new StringBuffer();
                start.append(START_HTTPD).append("&&").append(START_NAGIOS);
                executeCmd(start.toString(), connServer);
            }
        }
    }

    private void clientConfig(Map<String, Object> map, SysConfig sysConfig, Connection connClient) {
        log.info("clientConfig");
        String path = DEFAULT_PATH;
        if (StrUtil.isNotBlank(sysConfig.getClientPath())) {
            path = sysConfig.getClientPath();
        }
        String command = null;
        if (true) {
            command = String.format(Locale.ROOT, CAT_NRPE, path);
            String configResult = executeCmdAndGetResult(command, connClient);
            StringBuffer nrpeBuf = new StringBuffer();
            command = String.format(Locale.ROOT, LS_SH, path);
            String shResult = executeCmdAndGetResult(command, connClient);
            boolean isStart = false;
            StringBuffer targetSh = new StringBuffer();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (configResult != null && !configResult.contains(entry.getKey())) {
                    isStart = true;
                    String config = WRAP_STR
                            + String.format(Locale.ROOT, NRPE_CONFIG, entry.getKey(), path, entry.getKey());
                    nrpeBuf.append(config);
                }
                if (shResult != null && !shResult.contains(entry.getKey())) {
                    targetSh.append(String.format(Locale.ROOT, CREATE_SH, path, entry.getKey(), entry.getKey()));
                    String config = String.format(Locale.ROOT, SH_CONFIG, entry.getValue(), entry.getValue());
                    targetSh.append("&&" + String.format(Locale.ROOT, WRITE_SH, path, config, entry.getKey()));
                    targetSh.append("&&");
                }
            }
            if (nrpeBuf.length() > 0) {
                command = String.format(Locale.ROOT, WRITE_NRPE, path, nrpeBuf.toString());
                executeCmd(command, connClient);
            }
            if (targetSh.length() > 0) {
                String str = targetSh.substring(0, targetSh.length() - 2);
                executeCmd(str, connClient);
            }
            if (isStart) {
                String result = executeCmdAndGetResult("ps -ef|grep nrpe|awk '{print $2}'", connClient);
                if (StringUtils.isNotEmpty(result)) {
                    executeCmd("kill -9 $(ps -ef|grep nrpe|awk '{print $2}')", connClient);
                }
                command = String.format(Locale.ROOT, START_CLIENT, path, path);
                executeCmd(command, connClient);
                command = String.format(Locale.ROOT, "cd %s" + XIEG + "libexec" + XIEG + "&&chmod 777 *.sh", path);
                executeCmd(command, connClient);
            }
        }
    }

    /**
     * writeSh
     *
     * @param map       map
     * @param sysConfig sysConfig
     */
    public void writeSh(Map<String, Object> map, SysConfig sysConfig) {
        String path = DEFAULT_PATH;
        if (StrUtil.isNotBlank(sysConfig.getClientPath())) {
            path = sysConfig.getClientPath();
        }
        if (write == null) {
            write = getWrite(sysConfig.getClientIp(), sysConfig.getClientName(), sysConfig.getClientPassword());
        }
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String config = String.format(Locale.ROOT, SH_CONFIG, entry.getValue(), entry.getValue());
            String wr = String.format(Locale.ROOT, WRITE_SH, path, config, entry.getKey());
            buffer.append(wr);
            buffer.append("&&");
        }
        if (buffer.length() > 0) {
            String str = buffer.substring(0, buffer.length() - 2);
            executeCmd(str, write);
        }
    }

    /**
     * getConn
     *
     * @param hostIp   hostIp
     * @param userName userName
     * @param password password
     * @return Connection
     */
    public Connection getConn(String hostIp, String userName, String password) {
        boolean isFlag = false;
        try {
            Connection conn = new Connection(hostIp);
            conn.connect();
            isFlag = conn.authenticateWithPassword(userName, password);
            if (isFlag) {
                log.info("auth success!");
                return conn;
            } else {
                log.error(hostIp + "auth fail!");
                conn.close();
                return EMPY;
            }
        } catch (IOException e) {
            return EMPY;
        }
    }

    /**
     * getWrite
     *
     * @param hostIp   hostIp
     * @param userName userName
     * @param password password
     * @return Connection
     */
    public static Connection getWrite(String hostIp, String userName, String password) {
        Connection connection = null;
        boolean isFlag = false;
        try {
            connection = new Connection(hostIp);
            connection.connect();
            isFlag = connection.authenticateWithPassword(userName, password);
            if (isFlag) {
                log.info("auth success!");
                return connection;
            } else {
                log.error(hostIp + "auth fail!");
                connection.close();
                return EMPY;
            }
        } catch (IOException exception) {
            return EMPY;
        }
    }

    /**
     * executeCmdAndGetResult
     *
     * @param cmd  cmd
     * @param conn conn
     * @return String
     */
    public String executeCmdAndGetResult(String cmd, Connection conn) {
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand(cmd);
            return processOut(session.getStdout());
        } catch (IOException exception) {
            log.info("executeCmdAndGetResult fail");
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return STREMPY;
    }

    private boolean executeCmd(String cmd, Connection connect) {
        Session sess = null;
        try {
            sess = connect.openSession();
            sess.execCommand(cmd);
        } catch (IOException e) {
            log.error("executeCmd fail");
            return false;
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
        return true;
    }

    private String processOut(InputStream in) {
        InputStream out = new StreamGobbler(in);
        StringBuffer buff = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
        String line = null;
        while (true) {
            try {
                if ((line = br.readLine()) == null) {
                    break;
                }
            } catch (IOException exception) {
                log.error("processOut-->{}", exception.getMessage());
            }
            buff.append(line).append(WRAP_STR);
        }
        return buff.toString();
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            conn.close();
        }
    }
}
