package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;

@Data
public class ShardingDatasourceConfig {
    private String host;
    private String port;
    private String dbName;
    private String username;
    private String password;
}
