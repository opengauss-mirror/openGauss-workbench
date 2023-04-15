package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.common.core.domain.model.ops.ClusterSummaryVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lhf
 * @date 2022/8/6 17:37
 **/
public interface IOpsClusterService extends IService<OpsClusterEntity> {

    List<OpsClusterVO> listCluster();

    ClusterSummaryVO summary();

    void monitor(String clusterId, String hostId, String businessId);

    Map<String, Integer> threadPoolMonitor();

    long countByHostId(String hostId);

    String connectNum(Connection connection);

    String session(Connection connection);

    String lock(Connection connection);

    CheckSummaryVO check(String clusterId, String rootPassword);
}
