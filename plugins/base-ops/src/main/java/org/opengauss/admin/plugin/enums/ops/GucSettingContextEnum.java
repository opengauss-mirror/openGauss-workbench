package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GucSettingContextEnum {
    POSTMASTER("postmaster"),
    USERSET("user"),
    INTERNAL("internal"),
    BACKEND("backend"),
    SUSET("superuser"),
    SIGHUP("sighup");

    private String code;
}
