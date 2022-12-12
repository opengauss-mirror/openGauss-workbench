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
