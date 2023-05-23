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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.plugin.domain.MigrationHostPortalInstall;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.utils.ShellUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        String checkInstallPortalResult = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -f " + installPath + "portal/logs/portal_.log ] && cat " + installPath + "portal/logs/portal_.log | grep 'Install all migration tools success'");
        return StringUtils.isNotBlank(checkInstallPortalResult.trim());
    }

    public static boolean installPortal(String host, Integer port, String user, String pass, String installPath, String portalDownUrl, String portalPkgName, String portalJarName, boolean newInstallFile) {
        ShellUtil.execCommandGetResult(host, port, user, pass,"rm -rf  " + installPath + "portal");
        String existsPortalInstallFile = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -f " + installPath + portalPkgName + " ] && echo 1 || echo 0");
        if (Integer.parseInt(existsPortalInstallFile.trim()) == 0) {
            //download portal
            String downloadCommand = "wget -P " + installPath + " " + portalDownUrl + portalPkgName;
            log.info("wget download portal,command: {}", downloadCommand);
            String wgetResult = ShellUtil.execCommandGetResult(host, port, user, pass,downloadCommand);
        }

        String unzipShell = "tar -zxvf " + installPath + portalPkgName + " -C "+ installPath;
        log.info("unzip portal, {}", unzipShell);
        ShellUtil.execCommandGetResult(host, port, user, pass,unzipShell);

        String portalHome = installPath + "portal/";
        String installCommand = "java -Dpath=" + portalHome + " -Dorder=install_mysql_all_migration_tools -Dskip=true -jar " + portalHome + portalJarName;
        initPortalConfig(host, port, user, pass, portalHome);
        log.info("portal install, command: {}", installCommand);
        String installToolResult = ShellUtil.execCommandGetResult(host, port, user, pass,installCommand);
        log.info("portal exec install command result {}", installToolResult);
        if(installToolResult.contains("Install all migration tools success.")) {
            return true;
        } else if(installToolResult.contains("Error message: ")) {
            return false;
        }
        return false;
    }

    public static void initPortalConfig(String host, Integer port, String user, String pass, String portalHome) {
        String command = "sed -i 's#/ops/portal/#" + portalHome + "#g' " + portalHome + "config/toolspath.properties";
        log.info("init config , command: {}", command);
        ShellUtil.execCommand(host, port, user, pass, command);
    }

    /**
     * Start the portal; pass in the task ID and task parameters
     * @param host
     */
    public static void startPortal(MigrationHostPortalInstall host, MigrationTask task, String portalJarName, Map<String,String> paramMap) {
        log.info("run host info: {}", JSON.toJSONString(host));
        String portalHome = host.getInstallPath() + "portal/";
        String params = paramMap.entrySet().stream().map(p -> {
            return " -D" + p.getKey() + "=" + p.getValue();
        }).collect(Collectors.joining());
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("java -Dpath=").append(portalHome);
        commandSb.append(" -Dworkspace.id=").append(task.getId());
        commandSb.append(params);
        commandSb.append(" -Dorder=").append(task.getMigrationOperations());
        commandSb.append(" -Dskip=true -jar ").append(portalHome).append(portalJarName);
        log.info("start portal,host: {}, command: {}", host.getHost(), commandSb.toString());
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getRunUser(), host.getRunPassword(),commandSb.toString());
    }

    public static void finishPortal(String host, Integer port, String user, String pass, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=stop_plan -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static void stopIncrementalPortal(String host, Integer port, String user, String pass, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=stop_incremental_migration -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static void startReversePortal(String host, Integer port, String user, String pass, String installPath, String portalJarName, MigrationTask task) {
        String portalHome = installPath + "portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=run_reverse_migration -Dskip=true -jar " + portalHome + portalJarName);
    }

    public static String getPortalStatus(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + installPath + "portal/workspace/" + task.getId() + "/status/portal.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalFullProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + installPath + "portal/workspace/" + task.getId() + "/status/full_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalIncrementalProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + installPath + "portal/workspace/" + task.getId() + "/status/incremental_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalReverseProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + installPath + "portal/workspace/" + task.getId() + "/status/reverse_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalDataCheckProcess(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "grep '{.*}' " + installPath + "portal/workspace/" + task.getId() + "/status/full_migration_datacheck.txt | tail -n 1");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static List<String> getPortalLogPath(String host, Integer port, String user, String pass, String installPath, MigrationTask task) {
        String logs = ShellUtil.execCommandGetResult(host, port, user, pass,
                "find " + installPath + "portal/workspace/" + task.getId() + "/logs/ | xargs ls -ld | grep '^-' | awk -F ' ' '{print $9}'");
        if (StringUtils.isNotBlank(logs)) {
            String[] pathArr = logs.trim().split("\n");
            if (pathArr.length > 0) {
                return Arrays.stream(pathArr).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    public static String getTaskLogs(String host, Integer port, String user, String pass, String logPath) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + logPath);
        return result != null ? result : "";
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
    public static String[] getHostBaseInfo(String host, Integer port, String user, String pass){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cat /proc/cpuinfo |grep \"processor\"|wc -l && grep MemFree /proc/meminfo ");
        stringBuilder.append("| awk '{val=$2/1024}END{print val}' && df -Th | egrep -v \"(tmpfs|sr0)\" | tail -n +2|");
        stringBuilder.append("tr -s \" \" | cut -d \" \" -f5 | tr -d \"G\" | awk '{sum+=$1}END{print sum}'");
        String result = ShellUtil.execCommandGetResult(host, port, user, pass, stringBuilder.toString());
        if (StringUtils.isBlank(result)) {
            return new String[0];
        }
        String[] infos = result.trim().split("\n");
        return infos;
    }

    public static boolean directoryExists(String host, Integer port, String user, String pass, String path){
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -d " + path + " ] && echo 1 || echo 0");
        return Integer.parseInt(result.trim()) == 1;
    }

    public static boolean checkWritePermission(String host, Integer port, String user, String pass, String path){
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "[ -d " + path + " ] && [ -w " + path + " ] && echo 1 || echo 0");
        return Integer.parseInt(result.trim()) == 1;
    }

    public static boolean mkdirDirectory(String host, Integer port, String user, String pass, String path){
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "mkdir -p " + path + " && echo 1 || echo 0");
        return Integer.parseInt(result.trim()) == 1;
    }

    /**
     * Check if the replication permissions for the target database are configured correctly.
     *
     * @param host connection host of the executing machine
     * @param port connection port of the executing machine
     * @param user connection user of the executing machine
     * @param pass connection password of the executing machine
     * @param dataPath datapath of target database
     * @param dbUser dbuser of target database
     * @return check result
     */
    public static boolean checkTargetNodeReplicationPermise(String host, Integer port, String user, String pass,
                                                            String dataPath, String dbUser) {
        String fixContent = "host replication " + dbUser + " 0.0.0.0/0 sha256";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("grep -q ").append("'").append(fixContent).append("' ");
        stringBuilder.append(dataPath).append("/pg_hba.conf").append(" && ").append("echo 1 || echo 0");
        String result = ShellUtil.execCommandGetResult(host, port, user, pass, stringBuilder.toString());
        return Integer.parseInt(result.trim()) == 1;
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
    public static Map<String, Object> checkTargetNodeConfigCorrectness(String host, Integer port, String user,
                                                                       String pass, String dataPath) {
        StringBuilder sslBuilder = new StringBuilder();
        sslBuilder.append("gs_guc check -D ").append(dataPath).append(" -c 'ssl'");
        String sslResult = ShellUtil.execCommandGetResult(host, port, user, pass, sslBuilder.toString());
        StringBuilder walLevelBuilder = new StringBuilder();
        walLevelBuilder.append("gs_guc check -D ").append(dataPath).append(" -c 'wal_level'");
        String walLevelResult = ShellUtil.execCommandGetResult(host, port, user, pass, walLevelBuilder.toString());
        Map<String, Object> result = new HashMap<>();
        result.put("checkResult", sslResult.contains("ssl=on") && walLevelResult.contains("wal_level=logical"));
        result.put("sslValue", matchValueByKeyOnContent(sslResult, "ssl"));
        result.put("walLevelValue", matchValueByKeyOnContent(walLevelResult, "wal_level"));
        return result;
    }

    /**
     * match value by key on content
     *
     * @param content content
     * @param key key
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

}
