/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * OpsClusterEnvService.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/OpsClusterEnvService.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.common.constant.ops.SshCommandConstants;
import org.opengauss.admin.plugin.domain.model.ops.env.EnvProperty;
import org.opengauss.admin.plugin.domain.model.ops.env.HardwareEnv;
import org.opengauss.admin.plugin.domain.model.ops.env.HostEnv;
import org.opengauss.admin.plugin.domain.model.ops.env.SoftwareEnv;
import org.opengauss.admin.plugin.enums.ops.HostEnvStatusEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussSupportOSEnum;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * OpsClusterEnvService
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Slf4j
@Service
public class OpsClusterEnvService {
    private static final List<String> DEPENDENCY_PACKAGE_NAMES = List.of("libaio-devel", "flex", "bison",
            "ncurses-devel", "glibc-devel", "patch", "readline-devel");
    private static final List<String> BASE_DEPENDENCY_LIST = List.of("coreutils", "procps-ng", "openssh-clients",
            "unzip", "lsof", "tar");
    private static final List<String> OPENEULER_BASE_DEPENDENCY_LIST = List.of("coreutils", "procps-ng", "openssh",
            "unzip", "lsof", "tar");

    @Resource
    private OpsHostRemoteService opsHostRemoteService;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * host env check
     *
     * @param hostId       hostId
     * @param expectedOs   expectedOs
     * @param rootPassword rootPassword ignored
     * @return HostEnv
     */
    public HostEnv env(String hostId, OpenGaussSupportOSEnum expectedOs, String rootPassword) {
        OpsHostEntity hostEntity = opsHostRemoteService.getHost(hostId);
        return env(hostEntity, expectedOs);
    }

    /**
     * host env check
     *
     * @param hostId hostId
     * @return HostEnv
     */
    public HostEnv env(String hostId) {
        OpsHostEntity hostEntity = opsHostRemoteService.getHost(hostId);
        OpenGaussSupportOSEnum expectedOs = OpenGaussSupportOSEnum.of(hostEntity.getOs(), hostEntity.getOsVersion(),
                hostEntity.getCpuArch());
        return env(hostEntity, expectedOs);
    }

    private HostEnv env(OpsHostEntity hostEntity, OpenGaussSupportOSEnum expectedOs) {
        // use root check env
        OpsHostUserEntity userEntity = opsHostRemoteService.getHostRootUser(hostEntity.getHostId());
        HostEnv hostEnv = new HostEnv();
            CountDownLatch countDownLatch = new CountDownLatch(2);
            threadPoolTaskExecutor.submit(() -> {
                Session session = opsHostRemoteService.createHostUserSession(hostEntity, userEntity);
                hostEnv.setHardwareEnv(hardwareEnvDetect(session, expectedOs));
                opsHostRemoteService.closeSession(session);
                countDownLatch.countDown();
            });

            threadPoolTaskExecutor.submit(() -> {
                Session session = opsHostRemoteService.createHostUserSession(hostEntity, userEntity);
                hostEnv.setSoftwareEnv(softwareEnvDetect(session, expectedOs));
                opsHostRemoteService.closeSession(session);
                countDownLatch.countDown();
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error("waiting for thread to be interrupted", e);
            }
        return hostEnv;
    }


    private SoftwareEnv softwareEnvDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        SoftwareEnv softwareEnv = new SoftwareEnv();
        List<EnvProperty> envProperties = new CopyOnWriteArrayList<>();
        softwareEnv.setEnvProperties(envProperties);
        envProperties.add(dependencyPropertyDetect(session, expectedOs.getCpuArch()));
        envProperties.add(firewallPropertyDetect(session));
        envProperties.add(installUserPropertyDetect(session));
        envProperties.add(otherPropertyDetect(session, expectedOs));
        envProperties.sort(Comparator.comparingInt(EnvProperty::getSortNum));
        return softwareEnv;
    }

    private EnvProperty otherPropertyDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        EnvProperty otherProperty;
        String cpuArch = expectedOs.getCpuArch();
        if (StrUtil.equals(expectedOs.getOsId(), OpenGaussSupportOSEnum.CENTOS_X86_64.getOsId())) {
            otherProperty = getEnvProperty(session, cpuArch, SshCommandConstants.BASE_DEPENDENCY,
                    BASE_DEPENDENCY_LIST);
        } else {
            otherProperty = getEnvProperty(session, cpuArch, SshCommandConstants.OPENEULER_BASE_DEPENDENCY,
                    OPENEULER_BASE_DEPENDENCY_LIST);
        }
        otherProperty.setName("other");
        otherProperty.setSortNum(4);
        return otherProperty;
    }

    private EnvProperty installUserPropertyDetect(Session session) {
        EnvProperty installUserProperty = new EnvProperty();
        installUserProperty.setName("install user");
        installUserProperty.setSortNum(3);
        installUserProperty.setStatus(HostEnvStatusEnum.NORMAL);
        return installUserProperty;
    }

    private EnvProperty firewallPropertyDetect(Session session) {
        EnvProperty firewallProperty = new EnvProperty();
        firewallProperty.setName("firewall");
        firewallProperty.setSortNum(2);
        try {
            String firewallStatus = opsHostRemoteService.executeCommand(SshCommandConstants.FIREWALL, session, "firewall");
            if ("inactive".equals(firewallStatus)) {
                firewallProperty.setStatus(HostEnvStatusEnum.NORMAL);
            } else {
                firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
                firewallProperty.setStatusMessage("Please turn off the firewall");
            }
        } catch (Exception e) {
            log.error("Parse command response error", e);
            firewallProperty.setStatus(HostEnvStatusEnum.ERROR);
            firewallProperty.setStatusMessage("unknown error,please retry to check the firewall status");
        }
        return firewallProperty;
    }

    private EnvProperty dependencyPropertyDetect(Session session, String cpuArch) {
        EnvProperty dependencyProperty = getEnvProperty(session, cpuArch, SshCommandConstants.DEPENDENCY,
                DEPENDENCY_PACKAGE_NAMES);
        dependencyProperty.setName("software dependency");
        dependencyProperty.setSortNum(1);
        return dependencyProperty;
    }

    private EnvProperty getEnvProperty(Session session, String cpuArch, String queryCommand,
                                       List<String> dependencies) {
        EnvProperty envProperty = new EnvProperty();
        List<String> notInstalledPackages = getMissingList(session, cpuArch, queryCommand, dependencies);
        if (CollectionUtils.isEmpty(notInstalledPackages)) {
            envProperty.setStatus(HostEnvStatusEnum.NORMAL);
        } else {
            envProperty.setStatus(HostEnvStatusEnum.ERROR);
            envProperty.setStatusMessage("not installed dependencies:"
                    + StringUtils.join(notInstalledPackages, ","));
        }
        return envProperty;
    }

    /**
     * get uninstalled dependencies
     *
     * @param session      session
     * @param cpuArch      session
     * @param queryCommand queryCommand
     * @param dependencies dependencies
     * @return uninstalled dependencies
     */
    public List<String> getMissingList(Session session, String cpuArch, String queryCommand,
                                       List<String> dependencies) {
        try {
            String queryResult = opsHostRemoteService.executeCommand(queryCommand, session, "check dependencies");
            List<String> dependencyPackages = dependencies.stream().map(
                    dependency -> dependency + "." + cpuArch).collect(Collectors.toList());
            List<String> notInstalledPackages = new ArrayList<>();
            for (String dependencyPackage : dependencyPackages) {
                if (!queryResult.contains(dependencyPackage)) {
                    notInstalledPackages.add(dependencyPackage);
                }
            }
            return notInstalledPackages;
        } catch (OpsException e) {
            return Collections.singletonList(e.getMessage());
        }
    }

    private HardwareEnv hardwareEnvDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        HardwareEnv hardwareEnv = new HardwareEnv();
        List<EnvProperty> envProperties = new CopyOnWriteArrayList<>();
        hardwareEnv.setEnvProperties(envProperties);
        envProperties.add(osPropertyDetect(session, expectedOs));
        envProperties.add(osVersionPropertyDetect(session));
        envProperties.add(freeMemoryPropertyDetect(session));
        envProperties.add(cpuCoreNumPropertyDetect(session));
        envProperties.add(cpuFrequencyPropertyDetect(session));
        envProperties.add(freeHardDiskPropertyDetect(session));
        envProperties.sort(Comparator.comparingInt(EnvProperty::getSortNum));
        return hardwareEnv;
    }

    private EnvProperty freeHardDiskPropertyDetect(Session session) {
        EnvProperty freeHardDiskProperty = new EnvProperty();
        freeHardDiskProperty.setName("free hard disk space");
        freeHardDiskProperty.setSortNum(6);
        try {
            String freeHardDisk = opsHostRemoteService.executeCommand(SshCommandConstants.FREE_HARD_DISK, session, "free hard disk space");
            int freeHardDiskGB = calcDisk(freeHardDisk);
            freeHardDiskProperty.setValue(freeHardDiskGB + "G");

            freeHardDiskProperty.setStatus(HostEnvStatusEnum.NORMAL);

            int suggestedNum = 2;
            if (freeHardDiskGB < suggestedNum) {
                freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
                freeHardDiskProperty.setStatusMessage("min 2.0GB");
            }
        } catch (Exception e) {
            log.error("Parse command response error", e);
            freeHardDiskProperty.setStatus(HostEnvStatusEnum.ERROR);
            freeHardDiskProperty.setStatusMessage("unknown error,please retry to checked");
        }
        return freeHardDiskProperty;
    }

    private int calcDisk(String freeHardDisk) {
        Integer res = 0;
        String[] split = freeHardDisk.split("\n");
        for (String s : split) {
            try {
                res += Integer.parseInt(s.replace("G", " ").trim());
            } catch (Exception ignore) {

            }
        }
        return res;
    }

    private EnvProperty cpuFrequencyPropertyDetect(Session session) {
        EnvProperty cpuFrequencyProperty = new EnvProperty();
        cpuFrequencyProperty.setName("CPU frequency");
        cpuFrequencyProperty.setSortNum(5);
        try {
            String cpuFrequency = opsHostRemoteService.executeCommand(SshCommandConstants.CPU_FREQUENCY, session, "cpu frequency");
            cpuFrequencyProperty.setValue(cpuFrequency);

            double cpuCoreNum = Double.parseDouble(cpuFrequency.substring(0, cpuFrequency.length() - 3));
            cpuFrequencyProperty.setStatus(HostEnvStatusEnum.NORMAL);

            int suggestedNum = 2;
            if (cpuCoreNum < suggestedNum) {
                cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
                cpuFrequencyProperty.setStatusMessage("min 2.0GHz");
            }
        } catch (Exception e) {
            log.error("Parse command response error", e);
            cpuFrequencyProperty.setStatus(HostEnvStatusEnum.WARMING);
            cpuFrequencyProperty.setStatusMessage("unknown error,please retry to checked");
        }
        return cpuFrequencyProperty;
    }

    private EnvProperty cpuCoreNumPropertyDetect(Session session) {
        EnvProperty cpuCoreNumProperty = new EnvProperty();
        cpuCoreNumProperty.setName("Number of CPU cores");
        cpuCoreNumProperty.setSortNum(4);
        try {
            String cpuCore = opsHostRemoteService.executeCommand(SshCommandConstants.CPU_CORE_NUM, session, "number of cpu cores");
            if (StrUtil.isEmpty(cpuCore)) {
                // reTry command CPU_CORE_NUM
                cpuCore = opsHostRemoteService.executeCommand(SshCommandConstants.CPU_CORE_NUM, session, "number of cpu cores");
            }
            cpuCoreNumProperty.setValue(cpuCore);
            int cpuCoreNum = Integer.parseInt(cpuCore);
            int suggestedNum = 8;
            int minNum = 2;
            if (cpuCoreNum >= suggestedNum) {
                cpuCoreNumProperty.setStatus(HostEnvStatusEnum.NORMAL);
            } else if (cpuCoreNum >= minNum && cpuCoreNum < suggestedNum) {
                cpuCoreNumProperty.setStatus(HostEnvStatusEnum.WARMING);
                cpuCoreNumProperty.setStatusMessage("In performance tests and commercial deployment, " +
                        "it is recommended that one 16-core 2.0 GHz CPU be used.");
            } else {
                cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
                cpuCoreNumProperty.setStatusMessage("For individual developers, the minimum configuration is 2 cores ," +
                        " and the recommended configuration is 4 cores .");
            }
        } catch (Exception e) {
            log.error("Parse command response error", e);
            cpuCoreNumProperty.setStatus(HostEnvStatusEnum.ERROR);
            cpuCoreNumProperty.setStatusMessage("unknown error, retry to checked environment");
        }
        return cpuCoreNumProperty;
    }

    private EnvProperty freeMemoryPropertyDetect(Session session) {
        EnvProperty freeMemoryProperty = new EnvProperty();
        freeMemoryProperty.setName("available memory");
        freeMemoryProperty.setSortNum(3);
        try {
            String freeMemory = opsHostRemoteService.executeCommand(SshCommandConstants.FREE_MEMORY, session, "available memory");
            if (StrUtil.isEmpty(freeMemory)) {
                // reTry command FREE_MEMORY
                freeMemory = opsHostRemoteService.executeCommand(SshCommandConstants.FREE_MEMORY, session, "available memory");
            }
            freeMemoryProperty.setValue(freeMemory + "GB");
            int freeMemoryGB = Integer.parseInt(freeMemory);
            int suggestedGB = 32;
            if (freeMemoryGB >= suggestedGB) {
                freeMemoryProperty.setStatus(HostEnvStatusEnum.NORMAL);
            } else if (freeMemoryGB > 1 && freeMemoryGB < suggestedGB) {
                freeMemoryProperty.setStatus(HostEnvStatusEnum.WARMING);
                freeMemoryProperty.setStatusMessage("32GB or more is recommended");
            } else {
                freeMemoryProperty.setStatus(HostEnvStatusEnum.ERROR);
                freeMemoryProperty.setStatusMessage("2GB or more is recommended");
            }
        } catch (Exception e) {
            log.error("Parse command response error", e);
            freeMemoryProperty.setStatus(HostEnvStatusEnum.ERROR);
            freeMemoryProperty.setStatusMessage("unknown error,please retry later");
        }
        return freeMemoryProperty;
    }

    private EnvProperty osVersionPropertyDetect(Session session) {
        EnvProperty osVersionProperty = new EnvProperty();
        osVersionProperty.setName("operating system version");
        osVersionProperty.setSortNum(2);
        try {
            String osVersion = opsHostRemoteService.executeCommand(SshCommandConstants.OS_VERSION, session, "operating system version");
            osVersionProperty.setValue(osVersion);
            osVersionProperty.setStatus(HostEnvStatusEnum.NORMAL);
        } catch (Exception e) {
            log.error("Parse command response error", e);
            osVersionProperty.setValue("unknown");
            osVersionProperty.setStatus(HostEnvStatusEnum.ERROR);
            osVersionProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
        }
        return osVersionProperty;
    }

    private String getCpuArch(Session rootSession) {
        return opsHostRemoteService.executeCommand(SshCommandConstants.CPU_ARCH, rootSession,
                "get cpu architecture information").trim();
    }

    private String getOSVersion(Session rootSession) {
        String command = "cat /etc/os-release | grep VERSION_ID= | head -n 1 | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
        return opsHostRemoteService.executeCommand(command, rootSession, "get system version information").trim();
    }

    private String getOS(Session rootSession) {
        String command = "cat /etc/os-release | grep -oP '^ID=.*' | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
        return opsHostRemoteService.executeCommand(command, rootSession, "get system information").trim();
    }

    private EnvProperty osPropertyDetect(Session session, OpenGaussSupportOSEnum expectedOs) {
        EnvProperty osProperty = new EnvProperty();
        osProperty.setName("operating system");
        osProperty.setSortNum(1);

        try {
            String os = getOS(session);
            String cpuArch = getCpuArch(session);
            osProperty.setValue(os);

            if (expectedOs.match(os, cpuArch)) {
                osProperty.setStatus(HostEnvStatusEnum.NORMAL);
            } else {
                osProperty.setStatus(HostEnvStatusEnum.ERROR);
                osProperty.setStatusMessage("The operating system does not match the installation package information");
            }

            if (!"centos".equalsIgnoreCase(os.trim())) {
                osProperty.setStatus(HostEnvStatusEnum.WARMING);
                osProperty.setStatusMessage("Please check if the umask value is 0022");
            }
        } catch (Exception e) {
            log.error("Parse command response error：", e);

            osProperty.setStatus(HostEnvStatusEnum.ERROR);
            osProperty.setStatusMessage("Only supports openEuler 20.03LTS and CentOS 7.6 operating systems");
        }

        return osProperty;
    }
}
