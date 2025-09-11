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
 */

package org.opengauss.agent.impl;

/**
 * AgentCmdBuilder
 *
 * @author: wangchao
 * @date: 2025/4/18 11:40
 * @since 7.0.0-RC2
 **/
public class AgentCmdBuilder {
    private static final String STOP_AGENT_CMD_TEMPLATE = "pgrep -f  %s/%s | xargs -r kill -9";
    private static final String START_AGENT_CMD_TEMPLATE
        = "nohup java -Dquarkus.config.locations=%s/application.yml -jar %s/%s >/dev/null 2>&1 &";
    private static final String CHECK_DIR_EXIST_CMD_TEMPLATE = "[ -d \"%s\" ] && echo 'exist' || echo 'not_exist'";
    private static final String CREATE_DIR_CMD_TEMPLATE = "mkdir -p \"%s\"";
    private static final String CHECK_DIR_EMPTY_CMD_TEMPLATE
        = "[ \"$(ls -A %s)\" ] && echo 'not_empty' || echo 'empty'";
    private static final String CHECK_DIR_WRITABLE_CMD_TEMPLATE = "[ -w \"%s\" ] && echo 'writable' || echo 'readonly'";
    private static final String SHA256SUM_CMD_TEMPLATE = "sha256sum '%s/%s' | cut -d ' ' -f 1";
    private static final String DELETE_AGENT_DIR_CMD_TEMPLATE = "rm -rf %s";
    private static final String JAVA_VERSION_CMD_TEMPLATE = "source ~/.bashrc | java -version 2>&1 |"
        + " awk -F\\\" '/version/ {print $2}'";
    private static final String CHECK_AGENT_RUNNING_CMD_TEMPLATE = "pgrep -f \"%s\"";

    /**
     * stop Agent "pgrep -f %s/%s | xargs -r kill -9"
     *
     * @param installPath installPath
     * @param agentName agentName
     * @return command
     */
    public static String buildStopAgentCommand(String installPath, String agentName) {
        return String.format(STOP_AGENT_CMD_TEMPLATE, installPath, agentName);
    }

    /**
     * start Agent  nohup java -jar %s/%s >/dev/null 2>&1 &
     *
     * @param installPath installPath
     * @param agentName agentName
     * @return command
     */
    public static String buildStartAgentCommand(String installPath, String agentName) {
        return String.format(START_AGENT_CMD_TEMPLATE, installPath, installPath, agentName);
    }

    /**
     * check dir exist
     *
     * @param dir dir
     * @return command
     */
    public static String buildCheckDirExistCommand(String dir) {
        return String.format(CHECK_DIR_EXIST_CMD_TEMPLATE, dir);
    }

    /**
     * create dir "mkdir -p \"%s\""
     *
     * @param dir dir
     * @return command
     */
    public static String buildCreateDirCommand(String dir) {
        return String.format(CREATE_DIR_CMD_TEMPLATE, dir);
    }

    /**
     * check dir is empty [ \"$(ls -A %s)\" ] && echo 'not_empty' || echo 'empty'
     *
     * @param dir dir
     * @return command
     */
    public static String buildCheckDirEmptyCommand(String dir) {
        return String.format(CHECK_DIR_EMPTY_CMD_TEMPLATE, dir);
    }

    /**
     * check dir writable "[ -w \"%s\" ] && echo 'writable' || echo 'readonly'"
     *
     * @param dir dir
     * @return command
     */
    public static String buildCheckDirWritableCommand(String dir) {
        return String.format(CHECK_DIR_WRITABLE_CMD_TEMPLATE, dir);
    }

    /**
     * delete agent dir "rm -rf \"%s\""
     *
     * @param installPath installPath
     * @return command
     */
    public static String buildDeleteAgentDirCommand(String installPath) {
        return String.format(DELETE_AGENT_DIR_CMD_TEMPLATE, installPath);
    }

    /**
     * calculate remote file sha256sum ,
     * back command  (if cmd sha256sum invalid) :  "openssl dgst -sha256 '%s' | awk '{print $2}'"
     *
     * @param installPath installPath
     * @param agentName agentName
     * @return command
     */
    public static String buildSha256CheckCommand(String installPath, String agentName) {
        return String.format(SHA256SUM_CMD_TEMPLATE, installPath, agentName);
    }

    // 路径拼接工具方法（兼容多平台）
    private static String joinPath(String... paths) {
        return String.join("/", paths);
    }

    /**
     * check Java  java -version 2>&1 | awk -F\\\" '/version/ {print $2}'
     *
     * @return command
     */
    public static String buildCheckJavaVersionCommand() {
        return JAVA_VERSION_CMD_TEMPLATE;
    }

    /**
     * check agent is running "ps -ef | grep '%s' | grep -v grep"
     *
     * @param installPath installPath
     * @param agentName agentName
     * @return command
     */
    public static String buildCheckAgentRunningCommand(String installPath, String agentName) {
        String agentPath = joinPath(installPath, agentName);
        return String.format(CHECK_AGENT_RUNNING_CMD_TEMPLATE, agentPath);
    }
}
