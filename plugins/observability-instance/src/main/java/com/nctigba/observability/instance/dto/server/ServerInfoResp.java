package com.nctigba.observability.instance.dto.server;

import lombok.Data;

@Data
public class ServerInfoResp {

    private String id;

    private String os;

    private String ip;

    private Integer port;

    private String userName;

    private String userPassword;

    private String name;

}
