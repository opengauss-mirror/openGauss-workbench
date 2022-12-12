package com.nctigba.observability.instance.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InstanceNodeInfo {

    @ApiModelProperty("Instance node id")
    private String id;

    @ApiModelProperty("Instance ID")
    private String instanceId;

    @ApiModelProperty("Server ID")
    private String serverId;

    @ApiModelProperty(notes = "Node ip", required = true)
    private String ip;

    @ApiModelProperty(notes = "Node port", required = true)
    private int port;

    @ApiModelProperty(notes = "Database Connection User", required = true)
    private String dbUser;

    @ApiModelProperty(notes = "Database connection user password", required = true)
    private String dbUserPassword;

    @ApiModelProperty(notes = "Database name", required = true)
    private String dbName;

    @ApiModelProperty(notes = "Database Type")
    private String dbType;

}
