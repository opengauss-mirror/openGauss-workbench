package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatabaseSelectViewQuery {
    private String webUser;
    private String connectionName;
    private String viewName;
    private String viewType;
    private String schema;
}
