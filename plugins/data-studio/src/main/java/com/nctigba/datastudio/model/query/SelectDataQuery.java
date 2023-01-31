package com.nctigba.datastudio.model.query;

import lombok.Data;

@Data
public class SelectDataQuery {
    private String webUser;

    private String connName;

    private String schema;

    private String tableName;
}
