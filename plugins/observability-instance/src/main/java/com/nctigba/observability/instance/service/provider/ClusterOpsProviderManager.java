/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.provider;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.nctigba.observability.instance.constants.CommonConstants;
import org.opengauss.admin.common.enums.ops.OpenGaussVersionEnum;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Component
public class ClusterOpsProviderManager {
    private static final ConcurrentHashMap<String, ClusterOpsProvider> REGISTRY = new ConcurrentHashMap<>();

    public static void registry(OpenGaussVersionEnum version, ClusterOpsProvider provider) {
        registry(version, OpenGaussSupportOSEnum.CENTOS_X86_64, provider);

    }

    public static void registry(OpenGaussVersionEnum version, OpenGaussSupportOSEnum os, ClusterOpsProvider provider) {
        REGISTRY.put(os.name() + version.name(), provider);
    }

    public Optional<ClusterOpsProvider> provider(OpenGaussVersionEnum version, OpenGaussSupportOSEnum os) {
        if (os == null) {
            os = OpenGaussSupportOSEnum.CENTOS_X86_64;
        }
        return Optional.ofNullable(REGISTRY.get(os.name() + version.name()));
    }

    @Getter
    @AllArgsConstructor
    public enum OpenGaussSupportOSEnum {
        CENTOS_X86_64("centos", "openGauss-3.0.0-CentOS-64bit.tar.bz2",
                "https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/x86/openGauss-3.0.0-CentOS-64bit.tar.bz2"),
        OPENEULER_ARCH64(CommonConstants.OPEN_EULER, "openGauss-3.0.0-openEuler-64bit.tar.bz2",
                "https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/arm/openGauss-3.0.0-openEuler-64bit.tar.bz2"),
        OPENEULER_X86_64(CommonConstants.OPEN_EULER, "openGauss-3.0.0-openEuler-64bit.tar.bz2",
                "https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/x86_openEuler/openGauss-3.0.0-openEuler-64bit.tar.bz2");

        private String osId;
        private String installPackageName;
        private String installPackageResourceUrl;

        public static OpenGaussSupportOSEnum of(String osInfo, String osVersionInfo, String cpuArchInfo) {
            if ("centos".equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)) {
                return CENTOS_X86_64;
            }

            if (CommonConstants.OPEN_EULER.equalsIgnoreCase(osInfo) && "aarch64".equalsIgnoreCase(cpuArchInfo)) {
                return OPENEULER_ARCH64;
            }

            if (CommonConstants.OPEN_EULER.equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)) {
                return OPENEULER_X86_64;
            }
            return CENTOS_X86_64;
        }

        public boolean match(String os) {
            return this.getOsId().equalsIgnoreCase(os);
        }
    }
}
