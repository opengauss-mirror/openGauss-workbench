package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lhf
 * @date 2022/12/11 13:38
 **/
@Getter
@AllArgsConstructor
public enum OpenGaussSupportOSEnum {
    CENTOS_X86_64("centos","x86_64"),
    OPENEULER_AARCH64("openEuler","aarch64"),
    OPENEULER_X86_64("openEuler","x86_64");

    private String osId;
    private String cpuArch;

    public static OpenGaussSupportOSEnum of(String osInfo, String osVersionInfo, String cpuArchInfo) {
        if ("centos".equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)){
            return CENTOS_X86_64;
        }

        if ("openEuler".equalsIgnoreCase(osInfo) && "aarch64".equalsIgnoreCase(cpuArchInfo)){
            return OPENEULER_AARCH64;
        }

        if ("openEuler".equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)){
            return OPENEULER_X86_64;
        }
        return CENTOS_X86_64;
    }

    public boolean match(String os,String cpuArch) {
        return this.getOsId().equalsIgnoreCase(os) && this.getCpuArch().equalsIgnoreCase(cpuArch);
    }
}
