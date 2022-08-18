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
    CENTOS_X86_64("centos","openGauss-3.0.0-CentOS-64bit.tar.bz2","https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/x86/openGauss-3.0.0-CentOS-64bit.tar.bz2"),
    OPENEULER_ARCH64("openEuler","openGauss-3.0.0-openEuler-64bit.tar.bz2","https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/arm/openGauss-3.0.0-openEuler-64bit.tar.bz2"),
    OPENEULER_X86_64("openEuler","openGauss-3.0.0-openEuler-64bit.tar.bz2","https://opengauss.obs.cn-south-1.myhuaweicloud.com/3.0.0/x86_openEuler/openGauss-3.0.0-openEuler-64bit.tar.bz2");

    private String osId;
    private String installPackageName;
    private String installPackageResourceUrl;

    public static OpenGaussSupportOSEnum of(String osInfo, String osVersionInfo, String cpuArchInfo) {
        if ("centos".equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)){
            return CENTOS_X86_64;
        }

        if ("openEuler".equalsIgnoreCase(osInfo) && "aarch64".equalsIgnoreCase(cpuArchInfo)){
            return OPENEULER_ARCH64;
        }

        if ("openEuler".equalsIgnoreCase(osInfo) && "x86_64".equalsIgnoreCase(cpuArchInfo)){
            return OPENEULER_X86_64;
        }
        return CENTOS_X86_64;
    }

    public boolean match(String os) {
        return this.getOsId().equalsIgnoreCase(os);
    }
}
