package com.nctigba.observability.instance.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MonitoringType {
    
    DEFAULT("prometheus");

    private final String monitoringType;
}
