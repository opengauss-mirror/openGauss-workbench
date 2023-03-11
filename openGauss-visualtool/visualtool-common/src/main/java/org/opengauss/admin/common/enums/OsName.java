package org.opengauss.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangyl
 * @date 203/03/08 13:38
 **/
@Getter
@AllArgsConstructor
public enum OsName {
    CENTOS("centos"),
    OPEN_EULER("openEuler"),
    ALL("all");

    private String osName;
}
