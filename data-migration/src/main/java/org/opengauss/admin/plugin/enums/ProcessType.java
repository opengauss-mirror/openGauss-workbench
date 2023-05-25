package org.opengauss.admin.plugin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessType {
    FULL(1, "full process"),
    INCREMENTAL(2, "incremental process"),
    REVERSE(3, "reverse process"),
    DATA_CHECK(4, "data check process");

    private Integer code;
    private String desc;
}
