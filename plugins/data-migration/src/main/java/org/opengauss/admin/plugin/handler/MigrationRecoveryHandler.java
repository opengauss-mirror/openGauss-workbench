/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
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
 * MigrationRecoveryHandler.java
 *
 * IDENTIFICATION
 * plugins/data-migration/src/main/java/org/opengauss/admin/plugin/handler/MigrationRecoveryHandler.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.handler;

import com.gitee.starblues.bootstrap.annotation.AutowiredType;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.constant.CommonConstants;
import org.opengauss.admin.common.utils.OpsAssert;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.vo.ProcessStatus;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.plugin.facade.JschExecutorFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * MigrationRecoveryHandler
 *
 * @author: wangchao
 * @Date: 2024/12/28 15:23
 * @since 7.0.0
 **/
@Slf4j
@Component
public class MigrationRecoveryHandler {
    /**
     * 使用正则表达式，格式化字符串中多个空格，为一个空格
     */
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s{2,}");
    private static final List<String> HISTORY_PORTAL_VERSION = Arrays.asList("6.0.0rc1", "6.0.0", "5.0.0", "5.1.0");
    private static final String FETCH_PROCESS_COMMAND = "ps -ux | grep java | grep %s | grep -E 'Dworkspace.id=%s|%s'";
    private static final String NAME_PORTAL_CONTROL = "portal-control";
    private static final String NAME_MIGRATION_INCREMENTAL_SOURCE = "source";
    private static final String NAME_MIGRATION_INCREMENTAL_SINK = "sink";
    private static final String NAME_MIGRATION_ONLINE_RESET = "reset";
    private static final String NAME_MIGRATION_REVERSE_SOURCE = "reverse-source";
    private static final String NAME_MIGRATION_REVERSE_SINK = "reverse-sink";
    private static final String NAME_MIGRATION_REVERSE_RESET = "reverse-reset";
    private static final String NAME_MIGRATION_CHECK = "check";
    private static final String NAME_MIGRATION_CHECK_SOURCE = "check-source";
    private static final String NAME_MIGRATION_CHECK_SINK = "check-sink";

    private static final Map<String, String> PROCESS_CMD_KEYWORD_FOR_NAME = new HashMap<>();

    static {
        PROCESS_CMD_KEYWORD_FOR_NAME.put("portalControl", NAME_PORTAL_CONTROL);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("mysql-source.properties", NAME_MIGRATION_INCREMENTAL_SOURCE);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("mysql-sink.properties", NAME_MIGRATION_INCREMENTAL_SINK);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("opengauss-source.properties", NAME_MIGRATION_REVERSE_SOURCE);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("opengauss-sink.properties", NAME_MIGRATION_REVERSE_SINK);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("application.yml", NAME_MIGRATION_CHECK);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("application-source.yml", NAME_MIGRATION_CHECK_SOURCE);
        PROCESS_CMD_KEYWORD_FOR_NAME.put("application-sink.yml", NAME_MIGRATION_CHECK_SINK);
    }

    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private JschExecutorFacade jschExecutorFacade;
    @Resource
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;

    /**
     * fetch process status list by migration task.
     *
     * @param migrationTask migration task
     * @param installPath install path
     * @return process status list
     */
    public Map<String, ProcessStatus> fetchProcessStatusListByMigrationTask(MigrationTask migrationTask,
        String installPath) {
        String condition = installPath + "workspace/" + migrationTask.getId() + "/config";
        String command = String.format(FETCH_PROCESS_COMMAND, installPath, migrationTask.getId(), condition);
        SshLogin sshLogin = buildSshLogin(migrationTask);
        List<ProcessStatus> processStatusList = fetchProcessStatusList(sshLogin, command);
        for (ProcessStatus processStatus : processStatusList) {
            processStatus.setName(parseProcessNameByExecCmd(processStatus.getCmd()));
        }
        return processStatusList.stream()
            .collect(Collectors.toMap(ProcessStatus::getName, Function.identity(), (oldValue, newValue) -> newValue));
    }

    private String parseProcessNameByExecCmd(String cmd) {
        String name = "other"; // Default name
        for (Map.Entry<String, String> entry : PROCESS_CMD_KEYWORD_FOR_NAME.entrySet()) {
            if (cmd.contains(entry.getKey())) {
                name = entry.getValue();
                break;
            }
        }
        return name;
    }

    /**
     * exec ssh command of {@value  FETCH_PROCESS_COMMAND}
     *
     * @param sshLogin sshLogin
     * @param command command
     * @return process status info
     */
    private List<ProcessStatus> fetchProcessStatusList(SshLogin sshLogin, String command) {
        List<ProcessStatus> processStatusList = new ArrayList<>();
        String result = jschExecutorFacade.execCommand(sshLogin, command);
        String[] lines = result.split(CommonConstants.LINE_SPLITTER);
        String lineFormat;
        for (String line : lines) {
            if (line.contains("bash -c ps")) {
                continue;
            }
            Matcher matcher = SPACE_PATTERN.matcher(line);
            lineFormat = matcher.replaceAll(" ");
            String[] res = lineFormat.split(" ");
            ProcessStatus processStatus = new ProcessStatus(res[0], res[1],
                StrUtil.join(" ", (Object) ArrayUtil.sub(res, 10, res.length)));
            processStatusList.add(processStatus);
        }
        return processStatusList;
    }

    private SshLogin buildSshLogin(MigrationTask migrationTask) {
        return new SshLogin(migrationTask.getRunHost(), migrationTask.getRunPort(), migrationTask.getRunUser(),
            encryptionUtils.decrypt(migrationTask.getRunPass()));
    }

    /**
     * check if the portal process is running
     *
     * @param processMap process map
     * @return true if the process is running
     */
    public boolean hasPortal(Map<String, ProcessStatus> processMap) {
        return processMap.containsKey(NAME_PORTAL_CONTROL);
    }

    /**
     * check if the incremental process is running
     *
     * @param processMap process map
     * @return true if the process is running
     */
    public boolean hasIncrementalSource(Map<String, ProcessStatus> processMap) {
        return processMap.containsKey(NAME_MIGRATION_INCREMENTAL_SOURCE);
    }

    /**
     * check if the incremental process is running
     *
     * @param processMap process map
     * @return true if the process is running
     */
    public boolean hasIncrementalSink(Map<String, ProcessStatus> processMap) {
        return processMap.containsKey(NAME_MIGRATION_INCREMENTAL_SINK);
    }

    /**
     * check if the reverse process is running
     *
     * @param processMap process map
     * @return true if the process is running
     */
    public boolean hasReverseSource(Map<String, ProcessStatus> processMap) {
        return processMap.containsKey(NAME_MIGRATION_REVERSE_SOURCE);
    }

    /**
     * check if the reverse process is running
     *
     * @param processMap process map
     * @return true if the process is running
     */
    public boolean hasReverseSink(Map<String, ProcessStatus> processMap) {
        return processMap.containsKey(NAME_MIGRATION_REVERSE_SINK);
    }

    /**
     * start the incremental process
     *
     * @param processMap process map
     * @param migrationTask migration task
     * @param portalInstall portal install
     * @param name process name
     */
    public void startProcessOfIncrementalMigration(MigrationTask migrationTask,
        MigrationHostPortalInstall portalInstall, String name) {
        SshLogin sshLogin = buildSshLogin(migrationTask);
        Integer id = migrationTask.getId();
        String installPath = portalInstall.getInstallRootPath();
        MigrationCommand migrationCommand = new MigrationCommand(installPath, portalInstall.getJarName(), id);
        OpsAssert.isTrue(migrationCommand.checkIncrementalCommand(name), "Incremental Command is not supported");
        String execResult;
        Map<String, ProcessStatus> processMap;
        if (StrUtil.equalsIgnoreCase(NAME_MIGRATION_ONLINE_RESET, name)) {
            String cmdOfStopIncrement = migrationCommand.builder(MigrationCommand.STOP_INCREMENTAL);
            execResult = jschExecutorFacade.execCommand(sshLogin, cmdOfStopIncrement);
            log.info("stop incremental process of migration task {}.", cmdOfStopIncrement);
            log.info("stop incremental process of migration task {}.", execResult);
            ThreadUtil.safeSleep(1000);
            processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
            while (processMap.size() > 1) {
                ThreadUtil.safeSleep(500);
                processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
            }
            String cmdOfRunIncrement = migrationCommand.builder(MigrationCommand.RUN_INCREMENTAL);
            execResult = jschExecutorFacade.execCommand(sshLogin, cmdOfRunIncrement);
            log.info("start incremental process of migration task {}.", cmdOfRunIncrement);
            log.info("start incremental process of migration task {}.", execResult);
        } else {
            String command = migrationCommand.builder(name);
            execResult = jschExecutorFacade.execCommand(sshLogin, command);
            log.info("start incremental process of migration task {}.", command);
            log.info("start incremental process of migration task {}.", execResult);
        }
        processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
        int checkTimes = 0;
        while (processMap.size() < 7 && checkTimes < 10) {
            ThreadUtil.safeSleep(500);
            processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
            checkTimes++;
        }
    }

    /**
     * start the reverse process
     *
     * @param migrationTask migration task
     * @param portalInstall portal install
     * @param name process name
     */
    public void startProcessOfReverseMigration(MigrationTask migrationTask, MigrationHostPortalInstall portalInstall,
        String name) {
        SshLogin sshLogin = buildSshLogin(migrationTask);
        Integer id = migrationTask.getId();
        String installPath = portalInstall.getInstallRootPath();
        MigrationCommand migrationCommand = new MigrationCommand(installPath, portalInstall.getJarName(), id);
        OpsAssert.isTrue(migrationCommand.checkReverseCommand(name), "reverse Command is not supported");
        String execResult;
        if (StrUtil.equalsIgnoreCase(NAME_MIGRATION_REVERSE_RESET, name)) {
            String cmdOfStopReverse = migrationCommand.builder(MigrationCommand.STOP_REVERSE);
            execResult = jschExecutorFacade.execCommand(sshLogin, cmdOfStopReverse);
            log.info("stop reverse process of migration task {}", cmdOfStopReverse);
            log.info("stop reverse process of migration task {}", execResult);
            Map<String, ProcessStatus> processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
            while (processMap.size() > 1) {
                ThreadUtil.safeSleep(500);
                processMap = fetchProcessStatusListByMigrationTask(migrationTask, installPath);
            }
            String cmdOfRunReverse = migrationCommand.builder(MigrationCommand.RUN_REVERSE);
            execResult = jschExecutorFacade.execCommand(sshLogin, cmdOfRunReverse);
            log.info("run reverse process of migration task {}.", cmdOfRunReverse);
            log.info("run reverse process of migration task {}.", execResult);
        } else {
            String command = migrationCommand.builder(name);
            execResult = jschExecutorFacade.execCommand(sshLogin, command);
            log.info("run reverse process of migration task {}.", command);
            log.info("run reverse process of migration task {}.", execResult);
        }
    }

    /**
     * get portal jar version
     *
     * @param jarName jar name
     * @return jar version
     */
    public String getJarVersion(String jarName) {
        return jarName.replace("portalControl-", "").replace("-exec.jar", "");
    }

    /**
     * check portal version is history version ,when it not supports breakRecovery feature
     *
     * @param version version
     * @return boolean
     */
    public boolean checkPortalVersion(String version) {
        return HISTORY_PORTAL_VERSION.contains(version);
    }
}

class MigrationCommand {
    /**
     * incremental process command RUN_INCREMENTAL_SOURCE
     */
    public static final String RUN_INCREMENTAL_SOURCE = "source";

    /**
     * incremental process command RUN_INCREMENTAL_SINK
     */
    public static final String RUN_INCREMENTAL_SINK = "sink";

    /**
     * incremental process command RUN_INCREMENTAL
     */
    public static final String RUN_INCREMENTAL = "run";

    /**
     * incremental process command STOP_INCREMENTAL
     */
    public static final String STOP_INCREMENTAL = "stop";

    /**
     * incremental process command RESET_INCREMENTAL
     */
    public static final String RESET_INCREMENTAL = "reset";

    /**
     * reverse process command RUN_REVERSE_SOURCE
     */
    public static final String RUN_REVERSE_SOURCE = "reverse-source";

    /**
     * reverse process command RUN_REVERSE_SINK
     */
    public static final String RUN_REVERSE_SINK = "reverse-sink";

    /**
     * reverse process command RUN_REVERSE
     */
    public static final String RUN_REVERSE = "reverse-run";

    /**
     * reverse process command STOP_REVERSE
     */
    public static final String STOP_REVERSE = "reverse-stop";

    /**
     * reverse process command RESET_REVERSE
     */
    public static final String RESET_REVERSE = "reverse-reset";

    /**
     * incremental process command REVERSE_COMMANDS
     */
    public static final List<String> REVERSE_COMMANDS = Arrays.asList(RUN_REVERSE_SOURCE, RUN_REVERSE_SINK, RUN_REVERSE,
        STOP_REVERSE, RESET_REVERSE);

    /**
     * incremental process command INCREMENTAL_COMMANDS
     */
    public static final List<String> INCREMENTAL_COMMANDS = Arrays.asList(RUN_INCREMENTAL_SOURCE, RUN_INCREMENTAL_SINK,
        RUN_INCREMENTAL, STOP_INCREMENTAL, RESET_INCREMENTAL);

    private static final String INCREMENTAL_MIGRATION_STOP = "stop_incremental_migration";
    private static final String INCREMENTAL_MIGRATION_RUN = "run_incremental_migration";
    private static final String INCREMENTAL_MIGRATION_SOURCE = "run_incremental_migration_source";
    private static final String INCREMENTAL_MIGRATION_SINK = "run_incremental_migration_sink";
    private static final String REVERSE_MIGRATION_STOP = "stop_reverse_migration";
    private static final String REVERSE_MIGRATION_RUN = "run_reverse_migration";
    private static final String REVERSE_MIGRATION_SOURCE = "run_reverse_migration_source";
    private static final String REVERSE_MIGRATION_SINK = "run_reverse_migration_sink";
    private static final String CMD_PORTAL_TEMP = "java -Dpath=%s -Dorder=%s -Dskip=true -Dworkspace.id=%s -jar %s";
    private static final Map<String, String> MIGRATION_PROPERTIES = new HashMap<>();

    static {
        MIGRATION_PROPERTIES.put(STOP_INCREMENTAL, INCREMENTAL_MIGRATION_STOP);
        MIGRATION_PROPERTIES.put(RUN_INCREMENTAL, INCREMENTAL_MIGRATION_RUN);
        MIGRATION_PROPERTIES.put(RUN_INCREMENTAL_SOURCE, INCREMENTAL_MIGRATION_SOURCE);
        MIGRATION_PROPERTIES.put(RUN_INCREMENTAL_SINK, INCREMENTAL_MIGRATION_SINK);
        MIGRATION_PROPERTIES.put(STOP_REVERSE, REVERSE_MIGRATION_STOP);
        MIGRATION_PROPERTIES.put(RUN_REVERSE, REVERSE_MIGRATION_RUN);
        MIGRATION_PROPERTIES.put(RUN_REVERSE_SOURCE, REVERSE_MIGRATION_SOURCE);
        MIGRATION_PROPERTIES.put(RUN_REVERSE_SINK, REVERSE_MIGRATION_SINK);
    }

    private String installPath;
    private String jarName;
    private Integer workspaceId;

    /**
     * construct migration run command
     *
     * @param installPath install path
     * @param jarName jar name
     * @param workspaceId workspace id
     */
    public MigrationCommand(String installPath, String jarName, Integer workspaceId) {
        this.installPath = installPath;
        this.jarName = jarName;
        this.workspaceId = workspaceId;
    }

    /**
     * check migration incremental command
     *
     * @param name name
     * @return boolean
     */
    public boolean checkIncrementalCommand(String name) {
        return INCREMENTAL_COMMANDS.contains(name);
    }

    /**
     * check migration reverse command
     *
     * @param name name
     * @return boolean
     */
    public boolean checkReverseCommand(String name) {
        return REVERSE_COMMANDS.contains(name);
    }

    /**
     * build migration command
     *
     * @param name name
     * @return command
     */
    public String builder(String name) {
        OpsAssert.isTrue(MIGRATION_PROPERTIES.containsKey(name), "unknown migration command");
        String order = MIGRATION_PROPERTIES.get(name);
        return String.format(CMD_PORTAL_TEMP, installPath, order, workspaceId, installPath + jarName);
    }
}
