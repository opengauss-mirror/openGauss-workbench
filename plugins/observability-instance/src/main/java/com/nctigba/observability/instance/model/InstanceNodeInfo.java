/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.model;

import lombok.Data;

@Data
public class InstanceNodeInfo {

    private String id;

    private String instanceId;

    private String serverId;

    private String ip;

    private int port;

    private String dbUser;

    private String dbUserPassword;

    private String dbName;

    private String dbType;

}
