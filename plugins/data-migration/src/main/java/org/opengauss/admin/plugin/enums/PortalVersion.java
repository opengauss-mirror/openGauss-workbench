/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package org.opengauss.admin.plugin.enums;

import lombok.Getter;

/**
 * Portal version
 *
 * @since 2025/07/19
 */
@Getter
public enum PortalVersion {
    STABLE("stable"),
    EXPERIMENTAL("experimental");

    private final String code;

    private PortalVersion(String code) {
        this.code = code;
    }
}
