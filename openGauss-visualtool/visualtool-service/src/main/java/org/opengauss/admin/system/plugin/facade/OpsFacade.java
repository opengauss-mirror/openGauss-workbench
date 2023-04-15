package org.opengauss.admin.system.plugin.facade;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhf
 * @date 2022/10/10 13:35
 **/
@Service
public class OpsFacade {

    @Autowired
    private IOpsClusterService opsClusterService;

    public List<OpsClusterVO> listCluster() {
        return opsClusterService.listCluster();
    }

    public CheckSummaryVO check(String clusterId, String rootPassword){
        return opsClusterService.check(clusterId, rootPassword);
    }
}
