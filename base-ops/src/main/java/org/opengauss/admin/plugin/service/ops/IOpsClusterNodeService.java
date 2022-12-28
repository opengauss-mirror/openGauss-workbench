package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;

import java.util.List;

/**
 * @author lhf
 * @date 2022/8/18 09:15
 **/
public interface IOpsClusterNodeService extends IService<OpsClusterNodeEntity> {
    List<OpsClusterNodeEntity> listClusterNodeByClusterId(String clusterId);
}
