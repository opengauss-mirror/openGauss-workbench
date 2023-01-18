package org.opengauss.admin.common.core.domain.model.ops.jdbc;

import lombok.Data;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;

/**
 * @author lhf
 * @date 2023/1/13 14:26
 **/
@Data
public class JdbcDbClusterNodeVO {
    private String clusterNodeId;
    private String name;
    private String ip;
    private String port;
    private String username;
    private String password;
    private String url;

    public static JdbcDbClusterNodeVO of(OpsJdbcDbClusterNodeEntity clusterNodeEntity) {
        JdbcDbClusterNodeVO jdbcDbClusterNodeVO = new JdbcDbClusterNodeVO();
        jdbcDbClusterNodeVO.setClusterNodeId(clusterNodeEntity.getClusterNodeId());
        jdbcDbClusterNodeVO.setName(clusterNodeEntity.getName());
        jdbcDbClusterNodeVO.setIp(clusterNodeEntity.getIp());
        jdbcDbClusterNodeVO.setPort(clusterNodeEntity.getPort());
        jdbcDbClusterNodeVO.setUsername(clusterNodeEntity.getUsername());
        jdbcDbClusterNodeVO.setPassword(clusterNodeEntity.getPassword());
        jdbcDbClusterNodeVO.setUrl(clusterNodeEntity.getUrl());
        return jdbcDbClusterNodeVO;
    }
}
