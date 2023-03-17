package org.opengauss.admin.plugin.domain.model.ops.olk.dadReq;

import lombok.Data;

@Data
public class OlkCommandDto {
    private ShardingsDto shardingsDto;
    private OpenLooKengDto openLooKengDto;
}
