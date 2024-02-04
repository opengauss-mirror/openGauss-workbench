/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
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
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.collect.config.common;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Constant
 *
 * @author liu
 * @since 2023-09-17
 */
@Slf4j
public class Constant {
    /**
     * The file name required for compatibility evaluation
     */
    public static final String[] FILE_NAMES = {"assess/assessment.properties",
            "assess/compatibility-assessment-5.1.0.jar",
            "assess/report.html",
            "assess/start.sh"};

    /**
     * assessMap
     */
    public static final Map<String, String> ASSESS_MAP = Map.of(
            "file", "FILE_ASSESSMENT_STRATEGY",
            "collect", "COLLECT_ASSESSMENT_STRATEGY",
            "Attach_Application", "PROCCESS_ASSESSMENT_STRATEGY"
    );

    /**
     * ACT_PATH
     */
    public static final String ACT_PATH = "assess/";

    /**
     * LESS_THAN
     */
    public static final int LESS_THAN = 60;

    /**
     * MORE_THAN
     */
    public static final int MORE_THAN = 120;

    /**
     * TIME_FORMAT
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * ACT_PATH
     */
    public static final String ENV_PATH = "data/";

    /**
     * CREATE_PATH
     */
    public static final String CREATE_PATH = "if [ -d %s ]; then rm -rf %s*; result=$?; "
            + "[ $result -eq 0 ] && echo execution succeeded || echo false; else mkdir -p %s; result=$?; "
            + "[ $result -eq 0 ] "
            + "&& echo execution succeeded || echo false; fi";

    /**
     * SUCCESS_INSTALL
     */
    public static final String SUCCESS_INSTALL = "execution succeeded";

    /**
     * TIME_OUT
     */
    public static final long TIME_OUT = 30L;

    /**
     * TEMPORARY_SAVE
     */
    public static final String TEMPORARY_SAVE = "data/evaluate_history/";

    /**
     * ASSESS_DOWNLOAD
     */
    public static final String ASSESS_DOWNLOAD = "data/assess/report.html";

    /**
     * ASSESS_REPORT
     */
    public static final String ASSESS_REPORT = "report.html";

    /**
     * PILE_SUCCESS
     */
    public static final String PILE_SUCCESS = "Agent successfully loaded to the target process";

    /**
     * EXECUTE_TIME
     */
    public static final String EXECUTE_TIME = "executeTime";

    /**
     * EXECUTE_TIME
     */
    public static final String TASK = "task";

    /**
     * TASK_RUN
     */
    public static final String TASK_RUN = "running";

    /**
     * TASK_STOP
     */
    public static final String TASK_STOP = "stopping";

    /**
     * TASK_COMPLETED
     */
    public static final String TASK_COMPLETED = "completed";

    /**
     * EXECUTE_TIME
     */
    public static final String CONFIG = "config";

    /**
     * SCHEDULER_GROUP
     */
    public static final String SCHEDULER_GROUP = "default";

    /**
     * INSERTION_AGENTNAME
     */
    public static final String INSERTION_AGENTNAME = "agent.jar";

    /**
     * INSERTION_ATTACHNAME
     */
    public static final String INSERTION_ATTACHNAME = "attach.jar";

    /**
     * INSERTION_AGENTNAME
     */
    public static final String INSERTION_AGENTNAME_PATH = "stake/agent.jar";

    /**
     * INSERTION_ATTACHNAME
     */
    public static final String INSERTION_ATTACHNAME_PATH = "stake/attach.jar";

    /**
     * ASSESS_JAR
     */
    public static final String ASSESS_JAR = "compatibility-assessment-5.1.0.jar";

    /**
     * ASSESS_PROPERTIES
     */
    public static final String ASSESS_PROPERTIES = "assessment.properties";

    /**
     * ASSESS_FILE
     */
    public static final String ASSESS_FILE = "file";

    /**
     * ASSESS_COLLECT
     */
    public static final String ASSESS_COLLECT = "collect";

    /**
     * ASSESS_PID
     */
    public static final String ASSESS_PID = "Attach_Application";

    /**
     * INSERTION_AGENTNAME
     */
    public static final String INSERTION_SQL = "collection.sql";

    /**
     * INSERTION_ENVIRONMENT
     */
    public static final String INSERTION_ENVIRONMENT = "source /etc/profile";

    /**
     * INSERTION_PREFIX
     */
    public static final String INSERTION_PREFIX = "java -jar";

    /**
     * INSERTION_UPLOADPATH
     */
    public static final String INSERTION_UPLOADPATH = "/kit/file";

    /**
     * CHECK_PREFIX
     */
    public static final String CHECK_PREFIX = "if ps -p ";

    /**
     * CHECK_SUFFIX
     */
    public static final String CHECK_SUFFIX = " > /dev/null ; then [ ! -f /kit/file/collection.sql ] && mkdir"
            + " -p /kit/file && touch /kit/file/collection.sql; [ ! -f /kit/file/stack.txt ] && mkdir "
            + "-p /kit/file && touch /kit/file/stack.txt ; echo \"yes\"; else echo \"not exist\" ; fi";

    /**
     * FIND_ALL_JAVA
     */
    public static final String FIND_ALL_JAVA = "ps -ef | grep java | grep -v grep | "
            + "awk '{print \"pid:\" $2 \"  javaName:\" $NF}'";
}
