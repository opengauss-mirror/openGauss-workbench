package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class ShardingsDto {
    private ShardingRuleDto shardingRuleDto;
    private ZookeeperDto zookeeperDto;
    private String installPath;
    private String sourcePath;
    private ServerDto serverDto;
    private int port;
}
