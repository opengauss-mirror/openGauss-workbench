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

    public static void checkAndInstallPortal(MigrationTaskHostRef host, String portalHome) {
        String checkInstallPortalResult = ShellUtil.execCommandGetResult(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "[ -d " + portalHome + " ] && echo 1 || echo 0");
        if (Integer.parseInt(checkInstallPortalResult.trim()) == 0) {
            log.info("The portal is not installed, and the installation is in progress, host: {}", host.getHost());
            installPortal(host, portalHome);
        }
    }

    public static void installPortal(MigrationTaskHostRef host, String portalHome) {
        //download portal
        String base = portalHome.substring(0, portalHome.lastIndexOf("/") + 1);
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "wget -P " + base + " http://39.108.219.254:9898/portal.zip");
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "unzip -d " + portalHome + " " + base + "portal.zip");

        log.info("portal install");
        initPortalConfig(host, portalHome);
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "java -Dpath=" + portalHome + "/ -Dorder=install_mysql_all_migration_tools -Dskip=true -jar " + portalHome + "/portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static void initPortalConfig(MigrationTaskHostRef host, String portalHome) {
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),
                "sed -i 's#/data1/lt/test/portal#/" + portalHome + "#g' " + portalHome + "/config/toolspath.properties");
    }

    /**
     * Start the portal; pass in the task ID and task parameters
     * @param host
     */
    public static void startPortal(MigrationTaskHostRef host, MigrationTask task, Map<String,String> paramMap, String portalHome) {
        log.info("run host info: {}", JSON.toJSONString(host));
        String params = paramMap.entrySet().stream().map(p -> {
            return " -D" + p.getKey() + "=" + p.getValue();
        }).collect(Collectors.joining());
        StringBuilder commandSb = new StringBuilder();
        commandSb.append("java -Dpath=").append(portalHome).append("/");
        commandSb.append(" -Dworkspace.id=").append(task.getId());
        commandSb.append(params);
        commandSb.append(" -Dorder=").append(task.getMigrationOperations());
        commandSb.append(" -Dskip=true -jar ").append(portalHome).append("/portalControl-1.0-SNAPSHOT-exec.jar");
        log.info("start portal,host: {}, command: {}", host.getHost(), commandSb.toString());
        ShellUtil.execCommand(host.getHost(), host.getPort(), host.getUser(), host.getPassword(),commandSb.toString());
    }

    public static void finishPortal(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + "/ -Dworkspace.id=" + task.getId() + " -Dorder=stop_plan -Dskip=true -jar " + portalHome + "/portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static void stopIncrementalPortal(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + "/ -Dworkspace.id=" + task.getId() + " -Dorder=stop_incremental_migration -Dskip=true -jar " + portalHome + "/portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static void startReversePortal(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        ShellUtil.execCommand(host, port, user, pass,
                "java -Dpath=" + portalHome + "/ -Dworkspace.id=" + task.getId() + " -Dorder=run_reverse_migration -Dskip=true -jar " + portalHome + "/portalControl-1.0-SNAPSHOT-exec.jar");
    }

    public static String getPortalStatus(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + portalHome + "/workspace/" + task.getId() + "/status/portal.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalFullProcess(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + portalHome + "/workspace/" + task.getId() + "/status/full_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalIncrementalProcess(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + portalHome + "/workspace/" + task.getId() + "/status/incremental_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static String getPortalReverseProcess(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        String result = ShellUtil.execCommandGetResult(host, port, user, pass,
                "cat " + portalHome + "/workspace/" + task.getId() + "/status/reverse_migration.txt");
        return result != null ? replaceAllBlank(result.trim()) : "";
    }

    public static List<String> getPortalLogPath(String host, Integer port, String user, String pass, MigrationTask task, String portalHome) {
        String logs = ShellUtil.execCommandGetResult(host, port, user, pass,
                "find " + portalHome + "/workspace/" + task.getId() + "/logs/ | xargs ls -ld | grep '^-' | awk -F ' ' '{print $9}'");
        if (StringUtils.isNotBlank(logs)) {
            String[] pathArr = logs.trim().split("\n");
            if (pathArr.length > 0) {
                return Arrays.stream(pathArr).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    public static String getTaskLogs(String host, Integer port, String user, String pass, MigrationTask task, String logPath) {
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
