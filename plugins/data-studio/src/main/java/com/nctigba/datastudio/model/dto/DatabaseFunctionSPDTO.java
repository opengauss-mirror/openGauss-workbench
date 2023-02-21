package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseFunctionSPDTO {
    private String uuid;
    private String webUser;
    private String connectionName;
    private String schema;
    private String functionSPName;
}
