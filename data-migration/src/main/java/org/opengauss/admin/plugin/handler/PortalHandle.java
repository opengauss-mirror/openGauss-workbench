package org.opengauss.admin.plugin.handler;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.plugin.domain.MigrationTask;
import org.opengauss.admin.plugin.domain.MigrationTaskHostRef;
import org.opengauss.admin.plugin.utils.ShellUtil;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static boolean checkInstallPortal(MigrationTaskHostRef host) {
        String checkInstallPortalResult = ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "cat ~/portal/logs/portal_.log | grep 'Install all migration tools success'");
        return StringUtils.isNotBlank(checkInstallPortalResult.trim());
    }

    public static boolean checkAndInstallPortal(MigrationTaskHostRef host, String portalDownUrl) {
        if (!checkInstallPortal(host)) {
            log.info("The portal is not installed, and the installation is in progress, host: {}", host.getHost());
            return installPortal(host,portalDownUrl);
        }
        return true;
    }

    public static void removePortalPkg(MigrationTaskHostRef host) {
        ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "rm -rf  ~/portal*");
    }

    public static boolean installPortal(MigrationTaskHostRef host, String portalDownUrl) {
        removePortalPkg(host);
        //download portal
        log.info("wget download portal");
        String wgetResult = ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "wget -P ~ " + portalDownUrl + " -O portal.zip");

        String unzipShell = "unzip -d ~/portal ~/portal.zip";
        log.info("unzip portal, {}", unzipShell);
        ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                unzipShell);

        log.info("portal install");
        String portalHome = "/home/" + host.getUser() + "/portal/";
        initPortalConfig(host);
        String installToolResult = ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "java -Dpath=" + portalHome + " -Dorder=install_mysql_all_migration_tools -Dskip=true -jar " + portalHome + "portalControl-1.0-SNAPSHOT-exec.jar");
        log.info("portal exec install command result {}", installToolResult);
        if(installToolResult.contains("Install all migration tools success.")) {
            return true;
        } else if(installToolResult.contains("Error message: ")) {
            return false;
        }
        return false;
    }

    public static void initPortalConfig(MigrationTaskHostRef host) {
        String portalHome = "/home/" + host.getUser() + "/portal";
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "sed -i 's#/ops/portal#" + portalHome + "#g' ~/portal/config/toolspath.properties");
    }

    /**
     * Start the portal; pass in the task ID and task parameters
     * @param host
     */
    public static void startPortal(MigrationTaskHostRef host, MigrationTask task, Map<String,String> paramMap) {
        log.info("run host info: {}", JSON.toJSONString(host));
        String portalHome = "/home/" + host.getUser() + "/portal/";
        String params = paramMap.entrySet().stream().map(p -> {
            return " -D" + p.getKey() + "=" + p.getValue();
        }).collect(Collectors.joining());
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("java -Dpath=").append(portalHome);
        commandSb.append(" -Dworkspace.id=").append(task.getId());
        commandSb.append(params);
        commandSb.append(" -Dorder=").append(task.getMigrationOperations());
        commandSb.append(" -Dskip=true -jar ").append(portalHome).append("portalControl-1.0-SNAPSHOT-exec.jar");
        log.info("start portal,host: {}, command: {}", host.getHost(), commandSb.toString());
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),commandSb.toString());
    }

    public static void finishPortal(String host, Integer port, String user, String pass, MigrationTask task) {
        String portalHome = "/home/" + user + "/portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=stop_plan -Dskip=true -jar " + portalHome + "portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static void stopIncrementalPortal(String host, Integer port, String user, String pass, MigrationTask task) {
        String portalHome = "/home/" + user + "/portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=stop_incremental_migration -Dskip=true -jar " + portalHome + "portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static void startReversePortal(String host, Integer port, String user, String pass, MigrationTask task) {
        String portalHome = "/home/" + user + "/portal/";
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + " -Dworkspace.id=" + task.getId() + " -Dorder=run_reverse_migration -Dskip=true -jar " + portalHome + "portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static String getPortalStatus(String host, Integer port, String user, String pass, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat ~/portal/workspace/" + task.getId() + "/status/portal.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalFullProcess(String host, Integer port, String user, String pass, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat ~/portal/workspace/" + task.getId() + "/status/full_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalIncrementalProcess(String host, Integer port, String user, String pass, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat ~/portal/workspace/" + task.getId() + "/status/incremental_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalReverseProcess(String host, Integer port, String user, String pass, MigrationTask task) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat ~/portal/workspace/" + task.getId() + "/status/reverse_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static List<String> getPortalLogPath(String host, Integer port, String user, String pass, MigrationTask task) {
        String logs = ShellUtil.execCommandGetResult(host, port, user, pass,
                "find ~/portal/workspace/" + task.getId() + "/logs/ | xargs ls -ld | grep '^-' | awk -F ' ' '{print $9}'");
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
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String replaceAllBlank(String str) {
        String dest = "";
        if (StringUtils.isNotBlank(str)) {
            Matcher m = REPLACE_P.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

}
