package com.nctigba.observability.log.controller;

import com.nctigba.observability.log.service.ClusterManager;
import io.swagger.annotations.ApiOperation;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logSearch/api/v1")
public class ClusterController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/clusters")
    @ApiOperation("Instance Information")
    public List<OpsClusterVO> listCluster() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        return clusters;
    }
}