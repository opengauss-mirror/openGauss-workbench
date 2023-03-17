package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class ShardingRuleDto {
    private ShardingDataSourceDto dataSourceDto;
    private String tableName;
    private String column;
}
