package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterNodeEntity;

import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/8/18 09:15
 **/
public interface IOpsClusterNodeService extends IService<OpsClusterNodeEntity> {
    List<OpsClusterNodeEntity> listClusterNodeByClusterId(String clusterId);

    long countByHostId(String hostId);

    long countByHostUserId(String hostUserId);

    Map<String, List<OpsClusterNodeEntity>> listClusterNodeByClusterIds(List<String> clusterIds);
}
