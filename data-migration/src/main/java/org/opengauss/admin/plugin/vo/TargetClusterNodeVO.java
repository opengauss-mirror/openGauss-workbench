package org.opengauss.admin.plugin.vo;

import lombok.Data;

/**
 * @className: TargetClusterNodeVO
 * @author: xielibo
 * @date: 2023-03-23 12:24
 **/
@Data
public class TargetClusterNodeVO {

    private String nodeId;
    private String publicIp;
    private String privateIp;
    private String hostname;
    private String hostId;
    private Integer dbPort;
    private String dbName;
    private String dbUser;
    private String dbUserPassword;
    private Integer hostPort;
}
