package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsJdbcDbClusterNodeEntity;
import org.opengauss.admin.common.core.domain.model.ops.jdbc.JdbcDbClusterNodeInputDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lhf
 * @date 2023/1/13 11:08
 **/
public interface IOpsJdbcDbClusterNodeService extends IService<OpsJdbcDbClusterNodeEntity> {
    void del(String clusterNodeId);

    OpsJdbcDbClusterNodeEntity getClusterNodeByIpAndPort(String ip, String port);

    Set<String> fuzzyQueryClusterIds(String name);

    Map<String, List<OpsJdbcDbClusterNodeEntity>> mapClusterNodesByClusterId(Set<String> clusterIds);

    void delByClusterId(String clusterId);

    void update(String clusterNodeId, JdbcDbClusterNodeInputDto clusterNodeInput);

    void add(String clusterId, JdbcDbClusterNodeInputDto clusterNodeInput);

    boolean ping(JdbcDbClusterNodeInputDto clusterNodeInput);
}
