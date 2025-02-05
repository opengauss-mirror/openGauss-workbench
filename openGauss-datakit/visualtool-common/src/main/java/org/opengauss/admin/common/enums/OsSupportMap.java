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
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/OsSupportMap.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opengauss.admin.common.core.dto.OsSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 将各类衍生os映射为centos或者openEuler
 *
 * @author chenyuchen2024
 * @date 24/02/22 21:12
 **/
@Getter
@AllArgsConstructor
public enum OsSupportMap {
    CENTOS_7_X86_64("centos", "x86_64", "7.6"),
    OPENEULER_20_X86_64("openEuler", "x86_64", "20.03"),
    OPENEULER_20_AARCH64("openEuler", "aarch64", "20.03"),
    OPENEULER_22_X86_64("openEuler", "x86_64", "22.03"),
    OPENEULER_22_AARCH64("openEuler", "aarch64", "22.03"),
    OPENEULER_24_X86_64("openEuler", "x86_64", "24.03"),
    OPENEULER_24_AARCH64("openEuler", "aarch64", "24.03");

    private final String os;
    private final String cpuArch;
    private final String osVersion;

    private static OsSet osBasedSet;
    private static Map<String, OsSupportMap> openEulerX86Mapping = new HashMap<>();
    private static Map<String, OsSupportMap> openEulerAarch64Mapping = new HashMap<>();

    static {
        HashSet<String> centosBasedSet = new HashSet<>();
        centosBasedSet.add("centos");
        HashSet<String> openEulerBasedSet = new HashSet<>();
        openEulerBasedSet.add("openeuler");
        openEulerBasedSet.add("kylin");
        osBasedSet = new OsSet(centosBasedSet, openEulerBasedSet);

        initializeOpenEulerMapping();
    }

    private static void initializeOpenEulerMapping() {
        openEulerX86Mapping.put("20.03", OPENEULER_20_X86_64);
        openEulerX86Mapping.put("22.03", OPENEULER_22_X86_64);
        openEulerX86Mapping.put("24.03", OPENEULER_24_X86_64);

        openEulerAarch64Mapping.put("20.03", OPENEULER_20_AARCH64);
        openEulerAarch64Mapping.put("22.03", OPENEULER_22_AARCH64);
        openEulerAarch64Mapping.put("24.03", OPENEULER_24_AARCH64);
    }

    public static OsSupportMap of(String osInfo, String osVersionInfo, String cpuArchInfo) {
        if (osBasedSet.isCentos(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)) {
            return CENTOS_7_X86_64;
        }

        if (osBasedSet.isOpenEuler(osInfo.toLowerCase())) {
            // openEuler系os默认映射成openEuler20
            if ("x86_64".equalsIgnoreCase(cpuArchInfo)) {
                return openEulerX86Mapping.getOrDefault(osVersionInfo, OPENEULER_20_X86_64);
            }
            if ("aarch64".equalsIgnoreCase(cpuArchInfo)) {
                return openEulerAarch64Mapping.getOrDefault(osVersionInfo, OPENEULER_20_AARCH64);
            }
        }

        return CENTOS_7_X86_64;
    }

    public static OsSet getSupportOsNameList() {
        return osBasedSet;
    }
}
