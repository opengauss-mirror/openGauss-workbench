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

import com.jcraft.jsch.Session;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.opengauss.admin.common.core.domain.entity.agent.AgentInstallEntity;
import org.opengauss.admin.common.exception.ops.AgentTaskException;
import org.opengauss.admin.system.plugin.beans.SshLogin;
import org.opengauss.admin.system.service.JschExecutorService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Resource;

/**
 * AgentEnvironmentService
 *
 * @author: wangchao
 * @date: 2025/5/8 11:40
 * @since 7.0.0-RC2
 **/
@Slf4j
@Service
public class AgentEnvironmentService {
    @Resource
    private JschExecutorService jschExecutorService;
    @Resource
    private AgentSshLoginService agentSshLoginService;

    /**
     * <pre/>
     * check agent install environment
     * the environment check is checked by ssh command, the check items are:
     * 1. checked java version
     * 2. checked agent port
     * 3. checked agent install path,
     * check path exists empty writable
     * 4. checked agent running status
     * </pre>
     *
     * @param agent agent
     * @return check result
     */
    public Map<String, String> checkEnvironment(AgentInstallEntity agent) {
        Map<String, String> checkRes = new HashMap<>();
        if (Objects.isNull(agent)) {
            checkRes.put("agent", "agent is null");
            return checkRes;
        }
        SshLogin sshLogin = agentSshLoginService.getSshLogin(agent);
        jschExecutorService.executeWithSession(sshLogin, (session -> {
            List<EnvironmentChecker> checkers = Arrays.asList(new JavaVersionChecker(),
                new AgentPortChecker(agent.getAgentPort()), new InstallPathChecker(agent.getInstallPath()),
                new AgentRunningChecker(agent.getInstallPath(), agent.getAgentName()));
            checkers.forEach(checker -> {
                try {
                    checker.check(session, checkRes);
                } catch (AgentTaskException e) {
                    log.error("agent check failed for {}: {}", checker.getClass().getSimpleName(), e.getMessage());
                    checkRes.put(checker.getCheckKey(), "check error: " + e.getMessage());
                }
            });
        }));
        return checkRes;
    }

    /**
     * Agent running environment checker
     */
    private interface EnvironmentChecker {
        /**
         * check environment
         *
         * @param session session
         * @param checkRes checkRes
         */
        void check(Session session, Map<String, String> checkRes);

        /**
         * get check key
         *
         * @return check key
         */
        String getCheckKey();
    }

    /**
     * Agent port checker for agent environment
     */
    @AllArgsConstructor
    private class AgentPortChecker implements EnvironmentChecker {
        private static final String CHECK_KEY = "agent_port";

        private final Integer port;

        @Override
        public void check(Session session, Map<String, String> checkRes) {
            boolean isConflict = jschExecutorService.checkOsPortConflict(session, port);
            if (isConflict) {
                checkRes.put(getCheckKey(), "agent port " + port + " is in used");
            } else {
                checkRes.put(getCheckKey(), "agent port " + port + " is available");
            }
        }

        @Override
        public String getCheckKey() {
            return CHECK_KEY;
        }
    }

    /**
     * Java version checker for agent environment
     */
    private class JavaVersionChecker implements EnvironmentChecker {
        private static final String CHECK_KEY = "java_version";

        @Override
        public void check(Session session, Map<String, String> checkRes) {
            String javaVersionCmd = AgentCmdBuilder.buildCheckJavaVersionCommand();
            String versionOutput = jschExecutorService.execCommand(session, javaVersionCmd);
            if (versionOutput == null || versionOutput.isEmpty()) {
                checkRes.put(getCheckKey(), "Java Environment not found");
                return;
            }
            int majorVersion = Integer.parseInt(versionOutput.split("\\.")[0]);
            if (majorVersion < 17) {
                checkRes.put(getCheckKey(), majorVersion + " Java version required 17+ , current is: " + versionOutput);
            } else {
                checkRes.put(getCheckKey(), "java version meets the requirement : " + versionOutput);
            }
        }

        @Override
        public String getCheckKey() {
            return CHECK_KEY;
        }
    }

    /**
     * Agent install path checker for agent environment
     */
    @AllArgsConstructor
    private class InstallPathChecker implements EnvironmentChecker {
        private static final String CHECK_KEY = "install_path";
        private static final String SUB_KEY_EXIST = "exist";
        private static final String SUB_KEY_EMPTY = "empty";
        private static final String SUB_KEY_WRITABLE = "writable";

        private final String installPath;

        @Override
        public void check(Session session, Map<String, String> checkRes) {
            String checkExistCmd = AgentCmdBuilder.buildCheckDirExistCommand(installPath);
            String existFlag = jschExecutorService.execCommand(session, checkExistCmd).trim();
            if ("not_exist".equals(existFlag)) {
                checkRes.put(getSubItem(SUB_KEY_EXIST), "install path " + installPath + " is not exist");
            } else {
                checkRes.put(getSubItem(SUB_KEY_EXIST), "install path " + installPath + " is exist");
            }
            String checkEmptyCmd = AgentCmdBuilder.buildCreateDirCommand(installPath);
            String emptyFlag = jschExecutorService.execCommand(session, checkEmptyCmd).trim();
            if ("not_empty".equals(emptyFlag)) {
                checkRes.put(getSubItem(SUB_KEY_EMPTY), "install path " + installPath + " is not empty");
            } else {
                checkRes.put(getSubItem(SUB_KEY_EMPTY), "install path " + installPath + " is empty");
            }
            String checkwritableCmd = AgentCmdBuilder.buildCheckDirWritableCommand(installPath);
            String permissionFlag = jschExecutorService.execCommand(session, checkwritableCmd).trim();
            if ("writable".equals(permissionFlag)) {
                checkRes.put(getSubItem(SUB_KEY_WRITABLE), "install path " + installPath + " is writable");
            } else {
                checkRes.put(getSubItem(SUB_KEY_WRITABLE), "install path " + installPath + " is not writable");
            }
        }

        private String getSubItem(String checkKey) {
            return getCheckKey() + "." + checkKey;
        }

        @Override
        public String getCheckKey() {
            return CHECK_KEY; // 主键，子项通过后缀区分;
        }
    }

    /**
     * Agent running checker for agent environment
     */
    @AllArgsConstructor
    private class AgentRunningChecker implements EnvironmentChecker {
        private static final String CHECK_KEY = "agent_status";

        private final String installPath;
        private final String agentName;

        @Override
        public void check(Session session, Map<String, String> checkRes) {
            String checkRunningCmd = AgentCmdBuilder.buildCheckAgentRunningCommand(installPath, agentName);
            String runningFlag = jschExecutorService.execCommand(session, checkRunningCmd).trim();
            if (StrUtil.isEmpty(runningFlag)) {
                checkRes.put(getCheckKey(), "agent is not running");
            } else {
                checkRes.put(getCheckKey(), "agent is running");
            }
        }

        @Override
        public String getCheckKey() {
            return CHECK_KEY;
        }
    }
}
