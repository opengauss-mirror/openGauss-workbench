package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@NoArgsConstructor
@Data
public class DatabaseConnectionDO {
    private String connectionid;

    private String id;

    private String type;

    private String name;

    private String driver;

    private String ip;

    private String port;

    private String dataName;

    private String userName;

    private String password;

    private String webUser;
}
