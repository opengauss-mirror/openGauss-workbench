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
 * PortalHandle.java
 *
 * IDENTIFICATION
 * data-migration/src/main/java/org/opengauss/admin/plugin/handler/PortalHandle.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.plugin.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationThirdPartySoftwareConfig;
import org.opengauss.admin.plugin.enums.ToolsConfigEnum;
import org.opengauss.admin.plugin.exception.PortalInstallException;
import org.opengauss.admin.plugin.utils.ShellUtil;
import org.opengauss.admin.plugin.vo.ShellInfoVo;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @className: PortalHandle
 * @author: xielibo
 * @date: 2023-02-10 15:10
 **/
@Slf4j
public class PortalHandle {

    private static final String REPLACE_BLANK_ENTER = "\\s{2,}|\t|\r|\n";
    private static final Pattern REPLACE_P = Pattern.compile(REPLACE_BLANK_ENTER);

    public static boolean checkInstallPortal(String host, Integer port, String user, String pass, String installPath) {
        JschResult checkInstallPortalResult = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -f " + installPath + "portal/logs/portal_.log ] && cat " + installPath
                        + "portal/logs/portal_.log | grep 'Install all migration tools success'");
        return StringUtils.isNotBlank(checkInstallPortalResult.getResult().trim());
    }

    /**
     * checkInstallStatusAndUpdate
     *
     * @param installParams installParams
     * @param runPassword   runPassword
     * @return boolean
     * @author: www
     * @date: 2023/11/28 15:21
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     */
    public static boolean checkInstallStatusAndUpdate(MigrationHostPortalInstall installParams, String runPassword) {
        String portalHome = installParams.getInstallPath() + "portal/";
        JschResult getPortalInstallFileJarPath = ShellUtil.execCommandGetResult(installParams.getHost(),
                installParams.getPort(), installParams.getRunUser(), runPassword,
                "ls " + portalHome + "portalControl-*-exec.jar");
        if (getPortalInstallFileJarPath.isOk()) {
            String jarName = getPortalInstallFileJarPath.getResult().substring(
                    getPortalInstallFileJarPath.getResult().lastIndexOf("/") + 1);
            installParams.setJarName(jarName);
        } else {
            log.error("Failed to obtain the jar package from the installation directory " + portalHome + ".");
        }
        JschResult existsPortalInstallFileResult = ShellUtil.execCommandGetResult(installParams.getHost(),
                installParams.getPort(), installParams.getRunUser(), runPassword,
                "[ -f " + portalHome + installParams.getJarName() + " ] && echo 1 || echo 0");
        if (existsPortalInstallFileResult.isOk()
                && Integer.parseInt(existsPortalInstallFileResult.getResult().trim()) == 0) {
            // The installation package does not exist. Provide a prompt and write it to the log
            String cmd = String.format("mkdir -p %s && echo '%s' >> %s", installParams.getInstallPath(),
                    installParams.getJarName() + " is not exist.", installParams.getDatakitLogPath());
            JschResult result = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(),
                    installParams.getRunUser(), runPassword, cmd);
            if (!result.isOk()) {
                log.error("write install error message failed: " + result.getResult());
            }
            return false;
        }
        // Execute the portal check, determine whether the installation was successful based on the return value
        String checkInstallCommand = "java -Dpath=" + portalHome
                + " -Dorder=check_portal_status -Dskip=true -jar " + portalHome + installParams.getJarName();
        initPortalConfig(installParams.getHost(), installParams.getPort(), installParams.getRunUser(),
                runPassword, portalHome);
        log.info("portal install, command: {}", checkInstallCommand);
        JschResult installToolResult = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(),
                installParams.getRunUser(), runPassword, checkInstallCommand);
        if (!installToolResult.isOk()) {
            log.error("portal exec checkInstall command failed.");
        }
        log.info("portal exec checkInstall command result {}", installToolResult.getResult());
        return installToolResult.getResult().contains("check portal status:ok");
    }

    public static boolean installPortal(MigrationHostPortalInstall installParams) throws PortalInstallException {
        ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(),
                "rm -rf  " + installParams.getInstallPath() + "portal");
        JschResult existsPortalInstallFileResult = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(), "[ -f " + installParams.getInstallPath() + installParams.getPkgName() + " ] && echo 1 || echo 0");
        if (existsPortalInstallFileResult.isOk() && Integer.parseInt(existsPortalInstallFileResult.getResult().trim()) == 0) {
            //download portal
            String downloadCommand = "wget -P " + installParams.getInstallPath() + " " + installParams.getPkgDownloadUrl() + installParams.getPkgName();
            log.info("wget download portal,command: {}", downloadCommand);
            JschResult wgetResult = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(),
                    installParams.getRunUser(), installParams.getRunPassword(), downloadCommand);
            if (!wgetResult.isOk()) {
                log.error("download pkg failed...");
                throw new PortalInstallException("download portal package failed: " + wgetResult.getResult());
            }
        }

        String unzipShell = "tar -zxvf " + installParams.getInstallPath() + installParams.getPkgName() + " -C " + installParams.getInstallPath();
        log.info("unzip portal, {}", unzipShell);
        JschResult unzipResult = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(), unzipShell);
        if (!unzipResult.isOk()) {
            throw new PortalInstallException("unzip portal package failed: " + unzipResult.getResult());
        }

        String portalHome = installParams.getInstallPath() + "portal/";
        String thirdPartySoftwareParams =
                buildPortalThirdPartySoftwareParams(installParams.getThirdPartySoftwareConfig());
        String installCommand = "java -Dpath=" + portalHome + thirdPartySoftwareParams + " -Dorder"
                + "=install_mysql_all_migration_tools -Dskip=true -jar " + portalHome + installParams.getJarName();
        initPortalConfig(installParams.getHost(), installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(), portalHome);
        log.info("portal install, command: {}", installCommand);
        JschResult installToolResult = ShellUtil.execCommandGetResult(installParams.getHost(), installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(), installCommand);
        if (!installToolResult.isOk()) {
            throw new PortalInstallException("install portal package failed: " + installToolResult.getResult());
        }

        if (installToolResult.getResult().contains("Install all migration tools success.") && installToolResult.getResult().contains("Start kafka success")) {
            log.info("portal exec install command result {}", installToolResult.getResult());
            return true;
        }
        throw new PortalInstallException("install portal package failed: " + installToolResult.getResult());
    }

    private static String buildPortalThirdPartySoftwareParams(
            MigrationThirdPartySoftwareConfig thirdPartySoftwareConfig) {
        if (thirdPartySoftwareConfig == null) {
            return "";
        }
        return " -D"
                + "zookeeperPort=" + thirdPartySoftwareConfig.getZookeeperPort()
                + " -D"
                + "kafkaPort=" + thirdPartySoftwareConfig.getKafkaPort()
                + " -D"
                + "schemaRegistryPort=" + thirdPartySoftwareConfig.getSchemaRegistryPort()
                + " -D"
                + "zkIp=" + thirdPartySoftwareConfig.getZkIp()
                + " -D"
                + "kafkaIp=" + thirdPartySoftwareConfig.getKafkaIp()
                + " -D"
                + "schemaRegistryIp=" + thirdPartySoftwareConfig.getSchemaRegistryIp()
                + " -D"
                + "thirdPartySoftwareConfigType=" + thirdPartySoftwareConfig.getThirdPartySoftwareConfigType()
                + " -D"
                + "installDir=" + thirdPartySoftwareConfig.getInstallDir();
    }

    public static void initPortalConfig(String host, Integer port, String user, String pass, String portalHome) {
        String command = "sed -i 's#/ops/portal/#" + portalHome + "#g' " + portalHome + "config/toolspath.properties";
        log.info("init config , command: {}", command);
        ShellUtil.execCommand(host, port, user, pass, command);
    }

    /**
     * 加载工具配置文件
     *
     * @author: www
     * @date: 2023/11/28 10:54
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param installParams installParams
     * @return Map<Map<Object>>
     */
    public static Map<Integer, Map<String, Object>> loadToolsConfig(MigrationHostPortalInstall installParams)
            throws PortalInstallException {
        String portalHome = installParams.getInstallPath() + "portal/";
        String loadToolsConfigCommand =
                "java -Dpath=" + portalHome + " -Dorder=load_tools_config -Dskip=true -jar "
                        + portalHome + installParams.getJarName();
        JschResult loadToolsConfigRes = ShellUtil.execCommandGetResult(installParams.getHost(),
                installParams.getPort(), installParams.getRunUser(), installParams.getRunPassword(),
                loadToolsConfigCommand);
        if (!loadToolsConfigRes.isOk()) {
            throw new PortalInstallException("load tools config failed: " + loadToolsConfigRes.getResult());
        }
        Map<Integer, Map<String, Object>> toolsConfig = new HashMap<>();
        String result = loadToolsConfigRes.getResult();
        for (ToolsConfigEnum configEnum : ToolsConfigEnum.values()) {
            String toolConfig =
                    result.substring(result.indexOf(configEnum.getStartFromLog())
                            + configEnum.getStartFromLog().length(), result.indexOf(configEnum.getEndStrFromLog()));
            Map configMap = JSONObject.parseObject(toolConfig, Map.class);
            toolsConfig.put(configEnum.getType(), configMap);
        }
        return toolsConfig;
    }

    /**
     * verify before migration; pass in the task ID and task parameters
     *
     * @param host host info
     * @param task task
     * @param paramMap parameter
     * @param portalJarName portalJarName
     * @param command  command
     * @return check result
     */
    public static JschResult checkBeforeMigration(MigrationHostPortalInstall host, MigrationTask task,
                                                String portalJarName, Map<String, String> paramMap, String command) {
        log.info("run host info: {}", JSON.toJSONString(host));
        String portalHome = host.getInstallPath() + "portal/";
        String params = paramMap.entrySet().stream().map(p -> {
            return " -D" + p.getKey() + "=" + p.getValue();
        }).collect(Collectors.joining());
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("java -Dpath=").append(portalHome);
        commandSb.append(" -Dworkspace.id=").append(task.getId());
        commandSb.append(params);
        commandSb.append(" -Dorder.invoked.timestamp=").append(task.getOrderInvokedTimestamp());
        commandSb.append(" -Dorder=").append(command);
        commandSb.append(" -Dskip=true -jar ").append(portalHome).append(portalJarName);
        log.info("check before migration,host: {}, command: {}", host.getHost(), commandSb);
        return ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getRunUser(),
                host.getRunPassword(), commandSb.toString());
    }

    /**
     * Start the portal; pass in the task ID and task parameters
     *
     * @param host host info
     * @param task task
     * @param paramMap parameter
     * @param portalJarName portalJarName
     */
    public static void startPortal(MigrationHostPortalInstall host, MigrationTask task, String portalJarName, Map<String, String> paramMap) {
        log.info("run host info: {}", JSON.toJSONString(host));
        String portalHome = host.getInstallPath() + "portal/";
        String params = paramMap.entrySet().stream().map(p -> {
            return " -D" + p.getKey() + "=" + p.getValue();
        }).collect(Collectors.joining());
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("java -Dpath=").append(portalHome);
        commandSb.append(" -Dworkspace.id=").append(task.getId());
        commandSb.append(params);
        commandSb.append(" -Dorder.invoked.timestamp=").append(task.getOrderInvokedTimestamp());
        commandSb.append(" -Dorder=").append(task.getMigrationOperations());
        commandSb.append(" -Dskip=true -jar ").append(portalHome).append(portalJarName);
        log.info("start portal,host: {}, command: {}", host.getHost(), commandSb.toString());
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getRunUser(), host.getRunPassword(), commandSb.toString());
    }

    /**
     * finish the portal
     *
     * @param shellInfo shell information
     * @param installPath portal install path
     * @param portalJarName portal jar name
     * @param task migration task
     */
    public static void finishPortal(
            ShellInfoVo shellInfo, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(shellInfo, "java -Dpath=" + portalHome
                + " -Dworkspace.id=" + task.getId() + " -Dorder.invoked.timestamp=" + task.getOrderInvokedTimestamp()
                + " -Dorder=stop_plan -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static void stopIncrementalPortal(String host, Integer port, String user, String pass, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(host, port, user, pass, "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=stop_incremental_migration -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static void startReversePortal(String host, Integer port, String user, String pass, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(host, port, user, pass, "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=run_reverse_migration -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static String getPortalStatus(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat " + installPath + "portal/workspace/" + task.getId() + "/status/portal.txt");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static String getPortalFullProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat " + installPath + "portal/workspace/" + task.getId() + "/status/full_migration.txt");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static String getPortalIncrementalProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat " + installPath + "portal/workspace/" + task.getId() + "/status/incremental_migration.txt");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static String getPortalReverseProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat " + installPath + "portal/workspace/" + task.getId() + "/status/reverse_migration.txt");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static String getPortalDataCheckProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "grep '{.*}' " + installPath + "portal/workspace/" + task.getId() + "/status/full_migration_datacheck.txt | tail -n 1");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static List<String> getPortalLogPath(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        JschResult logsResult = ShellUtil.execCommandGetResult(host, port, user, pass, "find " + installPath + "portal/workspace/" + task.getId() + "/logs/ | xargs ls -ld | grep '^-' | awk -F ' ' '{print $9}'");
        if (logsResult.isOk() && StringUtils.isNotBlank(logsResult.getResult())) {
            String[] pathArr = logsResult.getResult().trim().split("\n");
            if (pathArr.length > 0) {
                return Arrays.stream(pathArr).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    /**
     *  get check result from file checkResult
     *
     * @param installHost host information
     * @param taskId task id
     * @return file content
     */
    public static String getPortalCheckResult(MigrationHostPortalInstall installHost, Integer taskId) {
        JschResult result = ShellUtil.execCommandGetResult(installHost.getHost(), installHost.getPort(),
                installHost.getRunUser(), installHost.getRunPassword(),
                "cat " + installHost.getInstallPath() + "portal/workspace/" + taskId + "/checkResult.json");
        return result.isOk() ? replaceAllBlank(result.getResult().trim()) : "";
    }

    public static String getTaskLogs(String host, Integer port, String user, String pass, String logPath) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat " + logPath);
        return result.isOk() ? result.getResult() : "";
    }

    public static String writeLogsToFile(String host, Integer port, String user, String pass, String logPath, String content) {
        String cmd = String.format("echo '%s' >> %s", content, logPath);
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, cmd);
        return result.isOk() ? result.getResult() : "";
    }

    public static String replaceAllBlank(String str) {
        String dest = "";
        if (StringUtils.isNotBlank(str)) {
            Matcher m = REPLACE_P.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * get basic information of a machine
     *
     * @param host connection host of the executing machine
     * @param port connection port of the executing machine
     * @param user connection username of the executing machine
     * @param pass connection password of the executing machine
     * @return info array
     */
    public static String[] getHostBaseInfo(String host, Integer port, String user, String pass) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cat /proc/cpuinfo |grep \"processor\"|wc -l && grep MemFree /proc/meminfo ");
        stringBuilder.append("| awk '{val=$2/1024}END{print val}' && df -Th | egrep -v \"(tmpfs|sr0)\" | tail -n +2|");
        stringBuilder.append("tr -s \" \" | cut -d \" \" -f5 | tr -d \"G\" | awk '{sum+=$1}END{print sum}'");
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, stringBuilder.toString());
        if (result.isOk() && StringUtils.isBlank(result.getResult())) {
            return new String[0];
        }
        String[] infos = result.getResult().trim().split("\n");
        return infos;
    }

    public static boolean directoryExists(String host, Integer port, String user, String pass, String path) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "[ -d " + path + " ] && echo 1 || echo 0");
        return Integer.parseInt(result.getResult().trim()) == 1;
    }

    /**
     * check whether the file exists
     *
     * @param host host ip
     * @param port host port
     * @param user install user
     * @param pass install user password
     * @param filePath file path
     * @return boolean
     */
    public static boolean fileExists(String host, Integer port, String user, String pass, String filePath) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -f " + filePath + " ] && echo 1 || echo 0");
        return Integer.parseInt(result.getResult().trim()) == 1;
    }

    public static boolean checkWritePermission(String host, Integer port, String user, String pass, String path) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "[ -d " + path + " ] && [ -w " + path + " ] && echo 1 || echo 0");
        return Integer.parseInt(result.getResult().trim()) == 1;
    }

    public static boolean mkdirDirectory(String host, Integer port, String user, String pass, String path) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "mkdir -p " + path + " && echo 1 || echo 0");
        return Integer.parseInt(result.getResult().trim()) == 1;
    }

    /**
     * Check if the replication permissions for the target database are configured correctly.
     *
     * @param host     connection host of the executing machine
     * @param port     connection port of the executing machine
     * @param user     connection user of the executing machine
     * @param pass     connection password of the executing machine
     * @param dataPath datapath of target database
     * @param dbUser   dbuser of target database
     * @return check result
     */
    public static boolean checkTargetNodeReplicationPermise(String host, Integer port, String user, String pass, String dataPath, String dbUser) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("grep -qEi ").append("'");
        stringBuilder.append("replication[[:space:]]+");
        stringBuilder.append("(").append(dbUser).append("|all)");
        stringBuilder.append("[[:space:]]+0\\.0\\.0\\.0/0");
        stringBuilder.append("' ");
        stringBuilder.append(dataPath).append("/pg_hba.conf").append(" && ").append("echo 1 || echo 0");
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, stringBuilder.toString());
        return Integer.parseInt(result.getResult().trim()) == 1;
    }

    /**
     * Check the configuration correctness of the target database.
     *
     * @param host     connection host of the executing machine
     * @param port     connection port of the executing machine
     * @param user     connection user of the executing machine
     * @param pass     connection password of the executing machine
     * @param dataPath datapath of target database
     * @return check result map
     */
    public static Map<String, Object> checkTargetNodeConfigCorrectness(String host, Integer port, String user, String pass, String dataPath) {
        StringBuilder sslBuilder = new StringBuilder();
        sslBuilder.append("gs_guc check -D ").append(dataPath).append(" -c 'ssl'");
        JschResult sslResult = ShellUtil.execCommandGetResult(host, port, user, pass, sslBuilder.toString());
        StringBuilder walLevelBuilder = new StringBuilder();
        walLevelBuilder.append("gs_guc check -D ").append(dataPath).append(" -c 'wal_level'");
        JschResult walLevelResult = ShellUtil.execCommandGetResult(host, port, user, pass, walLevelBuilder.toString());
        Map<String, Object> result = new HashMap<>();
        result.put("checkResult", sslResult.getResult().contains("ssl=on") && walLevelResult.getResult().contains("wal_level=logical"));
        result.put("sslValue", matchValueByKeyOnContent(sslResult.getResult(), "ssl"));
        result.put("walLevelValue", matchValueByKeyOnContent(walLevelResult.getResult(), "wal_level"));
        return result;
    }

    /**
     * match value by key on content
     *
     * @param content content
     * @param key     key
     * @return value
     */
    private static String matchValueByKeyOnContent(String content, String key) {
        String patternText = ".*" + key + "=([^\\s]*)";
        Pattern pattern = Pattern.compile(patternText, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        List<String> allSslValues = new ArrayList<>();
        while (matcher.find()) {
            String sslValue = matcher.group(1);
            allSslValues.add(sslValue);
        }
        if (allSslValues.isEmpty()) {
            return "";
        } else {
            String lastSslValue = allSslValues.get(allSslValues.size() - 1);
            return lastSslValue;
        }
    }

    /**
     * 获取portal toolsParamsDesc.properties文件配置信息
     *
     * @author: www
     * @date: 2023/11/28 10:38
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param host  host
     * @param port port
     * @param user user
     * @param pass pass
     * @param installPath installPath
     * @return Optional<Properties>
     */
    public static Optional<Properties> loadToolsParamsDesc(String host, Integer port, String user, String pass,
                                                           String installPath) {
        JschResult result = ShellUtil.execCommandGetResult(host, port, user, pass, "cat "
                + installPath + "portal"
                + "/config/toolsParamsDesc.properties");
        if (result.isOk()) {
            String paramDesc = result.getResult();
            if (paramDesc == null) {
                log.error("can not get param desc...");
            }
            Properties pps = new Properties();
            try {
                pps.load(new StringReader(paramDesc));
            } catch (IOException e) {
                log.error("load params exception :", e);
            }
            return Optional.of(pps);
        }
        return Optional.empty();
    }
}
