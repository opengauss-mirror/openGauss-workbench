/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@NoArgsConstructor
@Data
public class DatabaseConnectionUrlDO {

    private int id;

    private String type;

    private String name;

    private String driver;

    private String url;

    private String userName;

    private String password;

    private String webUser;

}
