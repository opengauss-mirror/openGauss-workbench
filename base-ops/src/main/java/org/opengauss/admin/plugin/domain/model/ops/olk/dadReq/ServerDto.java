package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class ServerDto {
    private String ip;
    private int port;
    private String username;
    private String password;
}
