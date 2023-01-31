package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseCreateViewDTO {
    private String webUser;
    private String connectionName;
    private String viewName;
    private String viewType;
    private String schema;
    private String sql;
}
