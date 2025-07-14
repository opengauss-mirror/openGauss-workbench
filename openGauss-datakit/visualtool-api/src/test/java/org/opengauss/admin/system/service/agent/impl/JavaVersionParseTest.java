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

package org.opengauss.admin.system.service.agent.impl;

import static org.junit.Assert.assertEquals;

import junit.framework.AssertionFailedError;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.utils.CommonUtils;

import java.util.Map;

/**
 * org.opengauss.admin.plugin.service.impl.MigrationTaskHostRefServiceImpl
 * #checkJavaEnv(org.opengauss.admin.system.plugin.beans.SshLogin)
 *
 * @author: wangchao
 * @Date: 2025/6/17 17:31
 * @since 7.0.0-RC2
 **/
@Slf4j
public class JavaVersionParseTest {
    @Test
    public void checkVersionList() {
        Map<String, Integer> versionMap = Map.of("1.8.0_301", 8, "11.0.12", 11, "17.0.14", 17, "21", 21, "21.0.1", 21,
            "23-ea", 23);
        versionMap.forEach((versionInfo, version) -> {
            try {
                Assertions.assertEquals(version, CommonUtils.getJavaVersionMajor(versionInfo));
                log.info("parse java version success, version: {}", versionInfo);
            } catch (AssertionFailedError e) {
                log.error("parse java version error, version: {} {}", versionInfo, e.getMessage());
            }
        });
    }

    @Test
    public void checkBishengJDKVersion() {
        String jdk8 = "openjdk version \"1.8.0_382\"\n"
            + "OpenJDK Runtime Environment (Harmony-based)(build 1.8.0_382-b05)\n"
            + "OpenJDK 64-Bit Server VM (build 25.382-b05, mixed mode)";
        String jdk11 = "openjdk version \"11.0.20\" 2024-07-16\n"
            + "OpenJDK Runtime Environment (build 11.0.20+8-Bisheng)\n"
            + "OpenJDK 64-Bit Server VM (build 11.0.20+8-Bisheng, mixed mode)";
        String jdk17 = "openjdk version \"17.0.9\" 2024-10-17\n"
            + "OpenJDK Runtime Environment (build 17.0.9+9-Bisheng)\n"
            + "OpenJDK 64-Bit Server VM (build 17.0.9+9-Bisheng, mixed mode)";
        String jdk21 = "openjdk version \"21\" 2023-09-19\n" + "OpenJDK Runtime Environment (build 21+35-Bisheng)\n"
            + "OpenJDK 64-Bit Server VM (build 21+35-Bisheng, mixed mode, sharing)";
        Map<String, Integer> versionMap = Map.of(jdk8, 8, jdk11, 11, jdk17, 17, jdk21, 21);
        versionMap.forEach((versionInfo, version) -> {
            try {
                Assertions.assertEquals(version, CommonUtils.getJavaVersionMajor(versionInfo));
                log.info("parse java version success, version: {}", versionInfo);
            } catch (AssertionFailedError e) {
                log.error("parse java version error, version: {} {}", versionInfo, e.getMessage());
            }
        });
    }

    @Test
    public void checkOracleJDKVersion() {
        String jdk8 = "java version \"1.8.0_381\"\n" + "Java(TM) SE Runtime Environment (build 1.8.0_381-b13)\n"
            + "Java HotSpot(TM) 64-Bit Server VM (build 25.381-b13, mixed mode)";
        String jdk11 = "java version \"11.0.20\" 2024-07-16 LTS\n"
            + "Java(TM) SE Runtime Environment 18.9 (build 11.0.20+8-LTS-111)\n"
            + "Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.20+8-LTS-111, mixed mode)";
        String jdk17 = "java version \"17.0.9\" 2024-10-17 LTS\n"
            + "Java(TM) SE Runtime Environment (build 17.0.9+9-LTS-224)\n"
            + "Java HotSpot(TM) 64-Bit Server VM (build 17.0.9+9-LTS-224, mixed mode)";
        String jdk21 = "java version \"21\" 2023-09-19\n" + "Java(TM) SE Runtime Environment (build 21+35-2513)\n"
            + "Java HotSpot(TM) 64-Bit Server VM (build 21+35-2513, mixed mode, sharing)";
        Map<String, Integer> versionMap = Map.of(jdk8, 8, jdk11, 11, jdk17, 17, jdk21, 21);
        versionMap.forEach((versionInfo, version) -> {
            try {
                Assertions.assertEquals(version, CommonUtils.getJavaVersionMajor(versionInfo));
                log.info("parse java version success, version: {}", versionInfo);
            } catch (AssertionFailedError e) {
                log.error("parse java version error, version: {} {}", versionInfo, e.getMessage());
            }
        });
    }

    @Test
    public void checkOpenJDKVersion() {
        String jdk8 = "openjdk version \"1.8.0_382\"\n" + "OpenJDK Runtime Environment (build 1.8.0_382-b05)\n"
            + "OpenJDK 64-Bit Server VM (build 25.382-b05, mixed mode)";
        String jdk11 = "java version \"11.0.20\" 2024-07-16 LTS\n"
            + "Java(TM) SE Runtime Environment 18.9 (build 11.0.20+8-LTS-111)\n"
            + "Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.20+8-LTS-111, mixed mode)";
        String jdk17 = "openjdk version \"17.0.9\" 2024-10-17\n"
            + "OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)\n"
            + "OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode)";
        String jdk21 = "openjdk version \"21\" 2023-09-19\n"
            + "OpenJDK Runtime Environment Temurin-21+35 (build 21+35)\n"
            + "OpenJDK 64-Bit Server VM Temurin-21+35 (build 21+35, mixed mode, sharing)";
        Map<String, Integer> versionMap = Map.of(jdk8, 8, jdk11, 11, jdk17, 17, jdk21, 21);
        versionMap.forEach((versionInfo, version) -> {
            try {
                Assertions.assertEquals(version, CommonUtils.getJavaVersionMajor(versionInfo));
                log.info("parse java version success, version: {}", versionInfo);
            } catch (AssertionFailedError e) {
                log.error("parse java version error, version: {} {}", versionInfo, e.getMessage());
            }
        });
    }
}
