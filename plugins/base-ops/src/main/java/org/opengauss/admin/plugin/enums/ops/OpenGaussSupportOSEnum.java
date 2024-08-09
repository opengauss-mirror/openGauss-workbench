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
 * OpenGaussSupportOSEnum.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/enums/ops/OpenGaussSupportOSEnum.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opengauss.admin.common.enums.OsSupportMap;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;

/**
 * @author lhf
 * @date 2022/12/11 13:38
 **/
@Getter
@AllArgsConstructor
public enum OpenGaussSupportOSEnum {
    CENTOS_X86_64("centos", "x86_64",
            SshCommandConstants.INSTALL_DEPENDENCY),
    OPENEULER_AARCH64("openEuler", "aarch64",
            SshCommandConstants.INSTALL_DEPENDENCY_OPENEULER_ARCH64),
    OPENEULER_X86_64("openEuler", "x86_64",
            SshCommandConstants.INSTALL_DEPENDENCY_OPENEULER_X86);

    private String osId;
    private String cpuArch;
    private String dependencyCommand;

    public static OpenGaussSupportOSEnum of(String osInfo, String osVersionInfo, String cpuArchInfo) {
        OsSupportMap osMapEnum = OsSupportMap.of(osInfo, osVersionInfo, cpuArchInfo);
        if (osMapEnum == OsSupportMap.CENTOS_7_X86_64) {
            return CENTOS_X86_64;
        }

        if ("x86_64".equalsIgnoreCase(osMapEnum.getCpuArch())) {
            return OPENEULER_X86_64;
        }
        return OPENEULER_AARCH64;
    }

    public boolean match(String os,String cpuArch) {
        return this.getOsId().equalsIgnoreCase(os) && this.getCpuArch().equalsIgnoreCase(cpuArch);
    }
}
