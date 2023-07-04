/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum OSType {
    DEFAULT("CentOS 7.6"), KYLIN_V10("kylinv10"), OPENEULER_203LTS("openEuler 20.3LTS");
    private final String osType;

    /**
     * Get the list of supported server operating systems
     *
     * @return Return to the list of server operating systems
     */
    public static List<String> getAllOSType() {
        return Arrays.stream(OSType.values()).map(OSType::getOsType).collect(Collectors.toList());
    }
}
