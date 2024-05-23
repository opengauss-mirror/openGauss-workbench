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
 *  CommonConstants.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/constants/CommonConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.constants;

public class CommonConstants {
    private CommonConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String HYPHEN = "-";
    public static final String FIELDS = "fields";
    public static final String LOG_TYPE = "log_type";
    public static final String LOG_LEVEL = "log_level";
    public static final String MESSAGE = "message";
    public static final String CLUSTER_ID = "clusterId";
    public static final String NODE_ID = "nodeId";
    public static final String TIMESTAMP = "@timestamp";

    /**
     * PRE_TAGS
     */
    public static final String PRE_TAGS = "<span style=\"background-color: yellow\">";

    /**
     * POST_TAGS
     */
    public static final String POST_TAGS = "</span>";

    /**
     * Check if the port exists
     */
    public static final String PORT_IS_EXIST = "source /etc/profile && lsof -ti :%s && echo 'true' || echo 'false'";

    /**
     * Get pid by port
     */
    public static final String PORT_PID = "ss -tulpn | grep ':%s\\s' | awk '{print $7}' | awk -F \",\" '{print $2}' "
            + "| awk -F \"=\" '{print $2}'";

    /**
     * Get pid by tid
     */
    public static final String PARENT_PID = "ps -o ppid= -p %s";

    /**
     * Check if the directory exists
     */
    public static final String DIRECTORY_IS_EXIST = "[ -e %s ] && echo 'true' || echo 'false'";

    /**
     * Check if the folder is empty
     */
    public static final String DIRECTORY_IS_EMPTY = "find %s -mindepth 1 -print -quit 2>/dev/null";

    /**
     * Check if the file exists
     */
    public static final String FILE_IS_EXIST = "[ -f %s ] && echo 'true' || echo 'false'";

    /**
     * Mkdir file
     */
    public static final String MKDIR_FILE = "mkdir -p %s";

    /**
     * Remove file
     */
    public static final String RM_FILE = "rm -rf %s";

    /**
     * Rm f file
     */
    public static final String RM_F_FILE = "rm -f %s";

    /**
     * Tar xvf file
     */
    public static final String TAR_XVF_FILE = "tar -xvf %s";

    /**
     * Cp file
     */
    public static final String CP_FILE = "cp -fr %s %s";

    /**
     * Max_map_count
     */
    public static final String MAX_MAP_COUNT = "/usr/sbin/sysctl -n vm.max_map_count";

    /**
     * Ulimit
     */
    public static final String U_LIMIT = "ulimit -n";

    /**
     * Get path by pid
     */
    public static final String PID_PATH = "ls -l /proc/%s |grep cwd|awk '{print $11}'";

    /**
     * Check filebeat health status
     */
    public static final String FILEBEAT_HEALTH_STATUS = "grep -qE \"Connection to backoff\\(elasticsearch\\"
            + "(http://[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+\\)\\) established\" filebeat.log "
            + "&& echo \"true\" || echo \"false\" ";

    /**
     * Timed task cycle
     */
    public static final long MONITOR_CYCLE = 60L;

    /**
     * Millisecond
     */
    public static final long MILLISECOND = 1000L;

    /**
     * Status timeout time
     */
    public static final int TIMEOUT = 3;
}
