/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.dto.server;

import lombok.Data;

@Data
public class ServerInfoReq {

    private String id;

    private String os;

    private String ip;

    private Integer port;

    private String userName;

    private String userPassword;

}
