/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * OpsClusterController.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/main/java/org/opengauss/admin/web/controller/ops/OpsClusterController.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.common.core.controller.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.core.domain.model.ops.ClusterSummaryVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.core.domain.model.ops.check.CheckSummaryVO;
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

    @GetMapping("/check")
    public AjaxResult check(@RequestParam String clusterId,@RequestParam(value = "rootPassword",required = false) String rootPassword) {
        CheckSummaryVO checkSummaryVO = opsClusterService.check(clusterId,rootPassword);
        return AjaxResult.success(checkSummaryVO);
    }

    @GetMapping
    public AjaxResult threadPoolMonitor() {
        Map<String, Integer> res = opsClusterService.threadPoolMonitor();
        return AjaxResult.success(res);
    }
}
