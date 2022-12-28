package org.opengauss.admin.system.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.common.core.domain.entity.ops.OpsCheckEntity;

import java.util.List;
import java.util.Map;

/**
 * @author lhf
 * @date 2022/11/13 17:17
 **/
public interface IOpsCheckService extends IService<OpsCheckEntity> {
    OpsCheckEntity getLastResByClusterId(String clusterId);

    Map<String, OpsCheckEntity> mapLastResByClusterIds(List<String> clusterIds);
}
