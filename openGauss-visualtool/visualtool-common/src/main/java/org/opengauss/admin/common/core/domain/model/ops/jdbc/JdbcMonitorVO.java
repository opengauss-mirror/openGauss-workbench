package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.enums.ops.ClusterRoleEnum;

/**
 * @author lhf
 * @date 2023/2/23 21:53
 **/
@Data
public class JdbcMonitorVO {
    private ClusterRoleEnum role;
    private String connNum;
    private String qps;
    private String tps;
    private String memoryUsed;
    private String tableSpaceUsed;
    private String lockNum;
    private String sessionNum;
}
