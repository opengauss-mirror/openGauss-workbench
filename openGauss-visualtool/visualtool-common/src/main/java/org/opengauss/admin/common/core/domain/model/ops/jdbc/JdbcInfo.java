package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;

/**
 * @author lhf
 * @date 2023/1/13 13:45
 **/
@Data
public class JdbcInfo {
    private DbTypeEnum dbType;
    private String ip;
    private String port;
}
