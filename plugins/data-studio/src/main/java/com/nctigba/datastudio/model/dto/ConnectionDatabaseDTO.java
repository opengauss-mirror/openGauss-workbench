package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ConnectionDatabaseDTO {

    private Integer id;
    private String databaseName;
    private String webUser;
}
