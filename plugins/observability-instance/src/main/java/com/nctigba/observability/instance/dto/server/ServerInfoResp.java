package com.nctigba.observability.instance.dto.server;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ServerInfoResp {

    @ApiModelProperty("Server ID")
    private String id;

    @ApiModelProperty(notes = "operating system")
    private String os;

    @ApiModelProperty(notes = "server address")
    private String ip;

    @ApiModelProperty(notes = "Server connection port")
    private Integer port;

    @ApiModelProperty(notes = "Server Connection User")
    private String userName;

    @ApiModelProperty(notes = "Server Connection User Password")
    private String userPassword;

    @ApiModelProperty(notes = "Server name")
    private String name;

}
