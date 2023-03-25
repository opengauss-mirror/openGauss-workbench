package org.opengauss.admin.plugin.domain.model.ops.olk;

import lombok.Data;

@Data
public class DistributeConfig {
    private String tableName;
    private String column;
}
