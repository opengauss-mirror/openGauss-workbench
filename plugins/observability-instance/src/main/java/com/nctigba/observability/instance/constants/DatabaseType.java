package com.nctigba.observability.instance.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DatabaseType {
    
    DEFAULT("openGauss"),
    
    OPENGAUSS("openGauss"),
    
    VASTBASE_G100("vastbase G100"),
    
    MYSQL("mysql"),
    
    ORACLE("oracle");
    
    private final String dbType;
}
