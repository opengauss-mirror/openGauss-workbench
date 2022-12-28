package org.opengauss.admin.web.controller.ops;

import io.swagger.annotations.Api;
import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.ClusterSummaryVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Operation and maintenance cluster operations
 *
 * @author lhf
 * @date 2022/8/6 17:38
 **/
@RestController
@RequestMapping("/opsCluster")
@Api(tags = "opsCluster")
public class OpsClusterController extends BaseController {

    @Autowired
    private IOpsClusterService opsClusterService;

    @GetMapping("/listCluster")
    public AjaxResult listCluster() {
        List<OpsClusterVO> clusterVOList = opsClusterService.listCluster();
        return AjaxResult.success(clusterVOList);
    }

    @GetMapping("/summary")
    public AjaxResult summary() {
        ClusterSummaryVO clusterSummaryVO = opsClusterService.summary();
        return AjaxResult.success(clusterSummaryVO);
    }

    @GetMapping("/monitor")
    public AjaxResult monitor(@RequestParam String clusterId, @RequestParam String hostId, @RequestParam String businessId) {
        opsClusterService.monitor(clusterId, hostId, businessId);
        return AjaxResult.success();
    }

    @GetMapping
    public AjaxResult threadPoolMonitor() {
        Map<String, Integer> res = opsClusterService.threadPoolMonitor();
        return AjaxResult.success(res);
    }
}
