package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MigrationMode {
    OFFLINE(1),
    ONLINE(2);
    private Integer code;
}
