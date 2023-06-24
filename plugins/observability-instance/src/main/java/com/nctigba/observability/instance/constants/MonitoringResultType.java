/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Monitoring data return type
 *
 * @author yangjie
 */
@RequiredArgsConstructor
@Getter
public enum MonitoringResultType {
    /**
     * table
     */
    TABLE,

    /**
     * line
     */
    LINE,
}
