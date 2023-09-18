package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PortalInstallType {
    ONLINE_INSTALL(0),
    OFFLINE_INSTALL(1),
    IMPORT_INSTALL(2);
    private Integer code;
}
