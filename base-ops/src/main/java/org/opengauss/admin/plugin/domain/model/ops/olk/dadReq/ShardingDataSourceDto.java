package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class ShardingDataSourceDto {
    private String url;
    private String username;
    private String password;
}
