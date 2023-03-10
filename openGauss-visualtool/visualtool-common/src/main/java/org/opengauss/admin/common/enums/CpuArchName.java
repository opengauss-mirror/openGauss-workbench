package org.opengauss.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangyl
 * @date 203/03/08 13:38
 **/
@Getter
@AllArgsConstructor
public enum CpuArchName {
    NOARCH("noarch"),
    X86_64("x86_64"),
    AARCH64("aarch64");

    private String cpuArchName;
}
