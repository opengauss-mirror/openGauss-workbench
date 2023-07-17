/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.entity;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * DatabaseConnectionDO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
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

    private String edition;
}
