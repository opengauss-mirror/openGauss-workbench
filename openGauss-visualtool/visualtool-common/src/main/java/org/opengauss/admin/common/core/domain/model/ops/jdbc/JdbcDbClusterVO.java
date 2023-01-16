package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterEntity;
import org.opengauss.admin.common.enums.ops.DbTypeEnum;
import org.opengauss.admin.common.enums.ops.DeployTypeEnum;

import java.util.List;

/**
 * @author lhf
 * @date 2023/1/13 14:26
 **/
@Data
public class JdbcDbClusterVO {
    private String clusterId;
    private String name;
    private DeployTypeEnum deployType;
    private DbTypeEnum dbType;
    private List<JdbcDbClusterNodeVO> nodes;

    public static JdbcDbClusterVO of(OpsJdbcDbClusterEntity record, List<JdbcDbClusterNodeVO> nodes) {
        JdbcDbClusterVO jdbcDbClusterVO = new JdbcDbClusterVO();
        jdbcDbClusterVO.setClusterId(record.getClusterId());
        jdbcDbClusterVO.setName(record.getName());
        jdbcDbClusterVO.setDeployType(record.getDeployType());
        jdbcDbClusterVO.setDbType(record.getDbType());
        jdbcDbClusterVO.setNodes(nodes);
        return jdbcDbClusterVO;
    }
}
