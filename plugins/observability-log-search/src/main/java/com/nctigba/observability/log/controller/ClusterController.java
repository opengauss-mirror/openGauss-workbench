package com.nctigba.observability.log.controller;

import java.util.List;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.log.service.ClusterManager;

@RestController
@RequestMapping("/logSearch/api/v1")
public class ClusterController {
    @Autowired
    private ClusterManager clusterManager;

    @GetMapping("/clusters")
    public List<OpsClusterVO> listCluster() {
        List<OpsClusterVO> clusters = clusterManager.getAllOpsCluster();
        return clusters;
    }
}